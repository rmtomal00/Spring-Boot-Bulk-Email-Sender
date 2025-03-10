package com.orbaic.email.services;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.orbaic.email.config.MailConfig;
import com.orbaic.email.dto.SendTaskCsvDto;
import com.orbaic.email.dto.SendTestMailDto;
import com.orbaic.email.dto.SetAdminDto;
import com.orbaic.email.jwt.JwtHelperManager;
import com.orbaic.email.middleware.UserDetailsProvider;
import com.orbaic.email.models.emailDataManage.emailTempate.EmailTemplatesModel;
import com.orbaic.email.models.emailDataManage.prepareEmailTask.PrepareEmailTaskModel;
import com.orbaic.email.models.settings.settingModel.MailDefaultSetting;
import com.orbaic.email.repositories.EmailTemplateRepository;
import com.orbaic.email.repositories.PrepareEmailRepository;
import com.orbaic.email.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    final UserRepository userRepository;
    final EmailTemplateRepository emailTemplateRepository;
    private final MailConfig mailConfig;
    private final PrepareEmailRepository prepareEmailRepository;
    final UserDetailsProvider provider;
    private final JwtHelperManager jwtHelperManager;
    private final RestTemplate restTemplate;
    private final AppSettingsService appSettingsService;

    public void setUserAsAdmin(@Valid SetAdminDto setAdminDto) throws Exception {
        var user = userRepository.findByEmail(setAdminDto.getEmail());
        if (user.isEmpty()){
            throw new Exception("User not found");
        }
        user.get().setRole(setAdminDto.getRole().name());
        userRepository.save(user.get());
    }

    public void setEmailContent(@NotEmpty(message = "Title can't be empty") String title, String content) {
        var emailContent = EmailTemplatesModel.builder()
                .templateName(title)
                .templateValue(content)
                .build();
        emailTemplateRepository.save(emailContent);
    }

    public Object getListOfTemplates() {
        return emailTemplateRepository.findAllTemplates();
    }

    public Map<String, Object> deleteTemplateById(Integer id) {
        if (!emailTemplateRepository.existsById(id)) {
            throw new EntityNotFoundException("Email template not found by this id");
        }
        emailTemplateRepository.deleteById(id);
        return Map.of("message", "Email template has been deleted by id "+id);
    }

    public void sendTestMail(@Valid SendTestMailDto testMail) {
        if (!emailTemplateRepository.existsById(testMail.getBodyId())){
            throw new EntityNotFoundException("Email template not found by this id");
        }
        String content = emailTemplateRepository.findById(testMail.getBodyId()).get().getTemplateValue();
        mailConfig.sendMail(testMail.getEmail(), testMail.getSubject(), content, true);
    }

    @Async("backgroundHandle")
    public void csvToPrepareData(@Valid SendTaskCsvDto taskCsvDto, String email) throws IOException, CsvException {
        MultipartFile file = taskCsvDto.getCsvFile();
        Reader reader = new InputStreamReader(file.getInputStream());
        CSVReader csvReader = new CSVReader(reader);
        csvReader.readNext();
        if (!emailTemplateRepository.existsById(taskCsvDto.getBodyId())) {
            mailConfig.sendMail(email, "Email preparation is fail", "Fail due to invalid bodyId. I can't find data with this bodyId: "+taskCsvDto.getBodyId(), false);
            throw new EntityNotFoundException("Email template not found by this id");
        }
        String content = emailTemplateRepository.findById(taskCsvDto.getBodyId()).get().getTemplateValue();
        List<PrepareEmailTaskModel> list = new ArrayList<>();
        List<String[]> datas = csvReader.readAll();
        csvReader.readAll().clear();
        csvReader.close();
        reader.close();
        for (String[] row : datas){
            if (row[1] == null) {
                continue;
            }
            var data = PrepareEmailTaskModel.builder()
                    .subject(taskCsvDto.getSubject())
                    .body(content)
                    .to(row[1])
                    .build();
            list.add(data);
            if (list.size() >= 100) {
                prepareEmailRepository.saveAll(list);
                list.clear();
            }
        }
        prepareEmailRepository.saveAll(list);
        list.clear();
        mailConfig.sendMail(email, "Email preparation is compete", String.format("Task will start soon. Total mail prepare: %d", datas.size()), false);
        datas.clear();
        appSettingsService.changeStatus(true, null);
    }

    @Async("backgroundHandle")
    public void apiDataToPrepare(List list, String email, @NotNull(message = "BodyId can't be empty and should be int") Integer bodyId, @NotEmpty(message = "Subject can't be empty") String subject) {
        if (!emailTemplateRepository.existsById(bodyId)) {
            mailConfig.sendMail(email, "Email preparation is fail", "Fail due to invalid bodyId. I can't find data with this bodyId: "+bodyId, false);
            throw new EntityNotFoundException("Email template not found by this id");
        }
        String content = emailTemplateRepository.findById(bodyId).get().getTemplateValue();
        List<PrepareEmailTaskModel> listP = new ArrayList<>();
        for (Object rs : list){
            if (rs == null) {
                continue;
            }
            var data = PrepareEmailTaskModel.builder()
                    .subject(subject)
                    .body(content)
                    .to(String.valueOf(rs))
                    .build();
            listP.add(data);
            if (listP.size() >= 100) {
                prepareEmailRepository.saveAll(listP);
                listP.clear();
            }
        }
        prepareEmailRepository.saveAll(listP);
        listP.clear();
        mailConfig.sendMail(email, "Email preparation is compete", String.format("Task will start soon. Total mail prepare: %d", list.size()), false);
        list.clear();
        appSettingsService.changeStatus(true, null);
    }

    public List getDataFromApiAsList(String link, @NotNull(message = "Offset can't be empty and should be int") Integer offset, @NotNull(message = "Limit can't be empty and should be int") Integer limit) throws Exception {
        String s = jwtHelperManager.generateToken(Map.of("skip", offset, "limit", limit), "orbaic_email", 10*60*1000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                URI.create(String.format(link+"%s", s)),
                request,
                Map.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            System.out.println(response.getBody());
            throw new Exception("Main server problem");
        }
        return  (List) response.getBody().get("result");
    }

    public List getDataFromApiAsListWithDate(String link, @NotNull(message = "Offset can't be empty and should be int") Integer offset, @NotNull(message = "Limit can't be empty and should be int") Integer limit, Long startAt, Long endAt) throws Exception {
        String s = jwtHelperManager.generateToken(Map.of("skip", offset, "limit", limit, "startDate", startAt, "endDate", endAt), "orbaic_email", 10*60*1000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                URI.create(String.format(link+"%s", s)),
                request,
                Map.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            System.out.println(response.getBody());
            throw new Exception("Main server problem");
        }
        return  (List) response.getBody().get("result");
    }

    public void modifySetting(Integer sendingLimit, Boolean sendingEnabled) {
        appSettingsService.changeStatus(sendingEnabled, sendingLimit);
    }
    public Map<String, Object> getStatus(){
        long totalMail = prepareEmailRepository.count();
        Object serverStatus = appSettingsService.getStatus();
        return Map.of("enqueue", totalMail, "serverStatus", serverStatus);
    }
}
