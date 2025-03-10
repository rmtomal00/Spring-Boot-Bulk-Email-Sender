package com.orbaic.email.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbaic.email.cacheData.ConcurrencyHashCache;
import com.orbaic.email.models.settings.AppSettings;
import com.orbaic.email.models.settings.settingModel.MailDefaultSetting;
import com.orbaic.email.repositories.AppSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppSettingsService {
    final AppSettingsRepository appSettingsRepository;
    private final ObjectMapper objectMapper;
    final ConcurrencyHashCache concurrencyHashCache;

    public void changeStatus(MailDefaultSetting mailDefaultSetting){
        var setting = AppSettings.builder()
                .settingName("mailDefaultSetting")
                .settingValue(mailDefaultSetting)
                .build();
        var appSettings = appSettingsRepository.findBySettingName(setting.getSettingName());
        if (appSettings.isEmpty()) {
            System.out.println("error to modify data: AppSettingsServices, changeStatus");
            return;
        }
        appSettings.get().setSettingValue(mailDefaultSetting);
        appSettingsRepository.save(appSettings.get());
    }

    public void changeStatus(Boolean status, Integer limit){

        var appSettings = appSettingsRepository.findBySettingName("mailDefaultSetting");
        if (appSettings.isEmpty()) {
            System.out.println("error to modify data: AppSettingsServices, changeStatus");
            return;
        }
        MailDefaultSetting defaultSetting = objectMapper.convertValue(appSettings.get().getSettingValue(), MailDefaultSetting.class);
        defaultSetting.setStatus(status != null ? status : defaultSetting.getStatus());
        defaultSetting.setLimit(limit != null ? limit : defaultSetting.getLimit());
        appSettings.get().setSettingValue(defaultSetting);
        appSettingsRepository.save(appSettings.get());
        concurrencyHashCache.setCacheData(defaultSetting);
    }

    public Object getStatus() {
        return concurrencyHashCache.getCacheData();
    }
}
