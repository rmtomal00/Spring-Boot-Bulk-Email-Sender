package com.orbaic.email.starterServices.staterHelperService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbaic.email.cacheData.ConcurrencyHashCache;
import com.orbaic.email.models.settings.AppSettings;
import com.orbaic.email.models.settings.settingModel.MailDefaultSetting;
import com.orbaic.email.repositories.AppSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultSetting {
    private final AppSettingsRepository  appSettingsRepository;
    private final ConcurrencyHashCache cacheData;
    private final ObjectMapper objectMapper;

    public void setMailDefaultSetting() {

        MailDefaultSetting data = MailDefaultSetting.builder()
                .limit(10)
                .status(false)
                .build();
        var settings = appSettingsRepository.findBySettingName("mailDefaultSetting");
        if (settings.isEmpty()) {
            appSettingsRepository.save(AppSettings.builder().settingName("mailDefaultSetting").settingValue(data).build());
            cacheData.setCacheData(data);
            return;
        }
        data = objectMapper.convertValue(settings.get().getSettingValue(), MailDefaultSetting.class);
        cacheData.setCacheData(data);

    }
}
