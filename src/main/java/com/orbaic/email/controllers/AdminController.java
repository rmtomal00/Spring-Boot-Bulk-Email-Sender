package com.orbaic.email.controllers;

import com.orbaic.email.dto.*;
import com.orbaic.email.jwt.JwtHelperManager;
import com.orbaic.email.middleware.UserDetailsProvider;
import com.orbaic.email.responseModel.CustomResponse;
import com.orbaic.email.services.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    final CustomResponse customResponse;
    final AdminService adminService;
    final UserDetailsProvider provider;
    private final JwtHelperManager jwtHelperManager;
    private final RestTemplate restTemplate;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("e: ", e);
        return customResponse.errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }
    @PostMapping("/set-admin")
    public ResponseEntity<?> setAdmin(@Valid @RequestBody SetAdminDto setAdminDto, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            return customResponse.errorResponse(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), HttpStatus.BAD_REQUEST.value());
        }
        adminService.setUserAsAdmin(setAdminDto);
        return customResponse.successWithoutDataRes("User has been set successfully");
    }

    @PostMapping("/upload-data")
    public ResponseEntity<?> uploadData(@Valid @ModelAttribute UploadDataDto file, BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            return customResponse.errorResponse(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), HttpStatus.BAD_REQUEST.value());
        }

        if (file.getFile() == null || file.getFile().getSize() == 0) {
            return customResponse.errorResponse("File is empty", HttpStatus.BAD_REQUEST.value());
        }

        if (file.getFile().getContentType() == null || file.getFile().getContentType().isEmpty()) {
            return customResponse.errorResponse("File content type is empty", HttpStatus.BAD_REQUEST.value());
        }

        if (file.getFile().getOriginalFilename() == null || !file.getFile().getOriginalFilename().endsWith(".html")) {
            return customResponse.errorResponse("File name is empty or not a html file", HttpStatus.BAD_REQUEST.value());
        }

        String content = new String(file.getFile().getBytes(),  StandardCharsets.UTF_8);
        if (content.isBlank()){
            return customResponse.errorResponse("File content is empty", HttpStatus.BAD_REQUEST.value());
        }
        adminService.setEmailContent(file.getTitle(), content);
        return customResponse.successWithoutDataRes("File has been uploaded successfully");
    }

    @GetMapping("/show-all-templates")
    public ResponseEntity<?> showTemplates() {
        return customResponse.successWithData(adminService.getListOfTemplates());
    }

    @DeleteMapping("/delete-template")
    public ResponseEntity<?> deleteTemplate(@RequestParam("id") Integer id) {
        if (id == null || id < 1) {
            return customResponse.errorResponse("Id is empty", HttpStatus.BAD_REQUEST.value());
        }
        Map<String, Object> result = adminService.deleteTemplateById(id);
        return customResponse.successWithData(result);
    }

    @PostMapping("/send-test-mail")
    public ResponseEntity<?> sendTestMail(@Valid @RequestBody SendTestMailDto testMail, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return customResponse.errorResponse(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), HttpStatus.BAD_REQUEST.value());
        }
        adminService.sendTestMail(testMail);
        return customResponse.successWithoutDataRes("Test Mail has been sent successfully");
    }

    @PostMapping("/send-task-csv")
    public ResponseEntity<?> sendTaskWithCsv(@Valid @ModelAttribute SendTaskCsvDto taskCsvDto, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            return customResponse.errorResponse(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), HttpStatus.BAD_REQUEST.value());
        }
        if (taskCsvDto.getCsvFile().isEmpty()){
            throw new Exception("CSV file can't be empty");
        }
        if (taskCsvDto.getCsvFile().getOriginalFilename() == null || taskCsvDto.getCsvFile().getOriginalFilename().isEmpty()) {
            throw new Exception("File content type is empty");
        }
        if (!taskCsvDto.getCsvFile().getOriginalFilename().endsWith(".csv")) {
            throw new Exception("File name is not a CSV file");
        }
        adminService.csvToPrepareData(taskCsvDto, provider.getUser().getEmail());
        return customResponse.successWithoutDataRes("Task has been sent successfully. When mail will prepare you will get an email. Please wait now till process is finished");
    }

    @PostMapping("/send-task-orbaic-api")
    public ResponseEntity<?> sendTaskWithOrbaicApi(@Valid @RequestBody SendTaskOrbaicApiDto orbaicApiDto, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            throw new Exception(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        String email = provider.getUser().getEmail();
        List list = adminService.getDataFromApiAsList("https://app.orbaic.com/api/v1/get-list-of-tasks/", orbaicApiDto.getOffset(), orbaicApiDto.getLimit());
        if (list == null || list.isEmpty()) {
            throw new Exception("No data found from server");
        }
        adminService.apiDataToPrepare(list, email, orbaicApiDto.getBodyId(), orbaicApiDto.getSubject());
        return customResponse.successWithoutDataRes("Task has been sent successfully. When mail will prepare you will get an email. Please wait now till process is finished");
    }

    @PostMapping("/send-task-with-date")
    public ResponseEntity<?> sendTaskWithDate(@Valid @RequestBody SendTaskWithDateDto sendTaskWithDateDto, BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            throw new Exception(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        long time = LocalDate.parse(sendTaskWithDateDto.getStartDate())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        long endTime = LocalDate.parse(sendTaskWithDateDto.getEndDate())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        endTime = endTime < 0 ? 10000 : endTime;
        String email = provider.getUser().getEmail();
        List list = adminService.getDataFromApiAsListWithDate("https://app.orbaic.com/api/v1/get-list-of-tasks-with-date/", sendTaskWithDateDto.getSkip(), sendTaskWithDateDto.getLimit(), time, endTime);
        if (list == null || list.isEmpty()) {
            throw new Exception("No data found from server");
        }
        adminService.apiDataToPrepare(list, email, sendTaskWithDateDto.getBodyId(), sendTaskWithDateDto.getSubject());
        return customResponse.successWithoutDataRes("Task has been sent successfully. When mail will prepare you will get an email. Please wait now till process is finished");
        //return customResponse.successWithData(Map.of("start", time, "end", endTime));
    }

    @PostMapping("/set-up-settings")
    public ResponseEntity<?> setUpSettings(@Valid @RequestBody SetUpDto setup, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            throw new Exception(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        if (setup.getSendingLimit() != null && (setup.getSendingLimit() < 10 || setup.getSendingLimit() > 100)){
            throw new Exception("SendingLimit must be between 10 and 100");
        }
        if (setup.getSendingLimit() == null && setup.getSendingEnabled() == null){
            throw new Exception("SendingLimit and SendingEnabled is both null");
        }

        adminService.modifySetting(setup.getSendingLimit(), setup.getSendingEnabled());
        return customResponse.successWithoutDataRes("Setting has been sent successfully");
    }

    @GetMapping("/get-server-status")
    public ResponseEntity<?> getServerStatus() {
        return customResponse.successWithData(adminService.getStatus());
    }
}
