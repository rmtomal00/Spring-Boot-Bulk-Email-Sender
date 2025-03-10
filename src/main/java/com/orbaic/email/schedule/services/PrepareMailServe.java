package com.orbaic.email.schedule.services;

import com.orbaic.email.cacheData.ConcurrencyHashCache;
import com.orbaic.email.config.MailConfig;
import com.orbaic.email.models.emailDataManage.prepareEmailTask.PrepareEmailTaskModel;
import com.orbaic.email.models.settings.settingModel.MailDefaultSetting;
import com.orbaic.email.repositories.AppSettingsRepository;
import com.orbaic.email.repositories.PrepareEmailRepository;
import com.orbaic.email.services.AppSettingsService;
import com.orbaic.email.starterServices.staterHelperService.DefaultSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.awt.print.Pageable;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PrepareMailServe {
    final ConcurrencyHashCache cacheData;
    final PrepareEmailRepository prepareEmailRepository;
    final AppSettingsService  appSettingsService;
    final MailConfig mailConfig;
    final DefaultSetting defaultSetting;

    public void serveEmail() {
        MailDefaultSetting settings = cacheData.getCacheData();
        if (settings == null) {
            System.out.println("Error to get data from cache: Schedule, services, PrepareMailServe, serveEmail");
            defaultSetting.setMailDefaultSetting();
            return;
        }
        if (!settings.getStatus()){
            return;
        }
        List<PrepareEmailTaskModel> list = prepareEmailRepository.getFirstLimit(PageRequest.of(0, 10));
        if (list.isEmpty()) {
            updateStatus(settings);
            return;
        }
        for (PrepareEmailTaskModel prepareEmailTaskModel : list) {
            mailConfig.sendMail(prepareEmailTaskModel.getTo(), prepareEmailTaskModel.getSubject(), prepareEmailTaskModel.getBody(), true);
        }
        prepareEmailRepository.deleteAll(list);
    }

    void updateStatus(MailDefaultSetting settings) {
        settings.setStatus(false);
        appSettingsService.changeStatus(settings);
        cacheData.setCacheData(settings);
        System.out.println(settings);
    }
}
