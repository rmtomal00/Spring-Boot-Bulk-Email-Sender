package com.orbaic.email.cacheData;

import com.orbaic.email.models.settings.settingModel.MailDefaultSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ConcurrencyHashCache {
    private final ConcurrentHashMap<String, MailDefaultSetting> settings = new  ConcurrentHashMap<>();

    public void setCacheData(MailDefaultSetting defaultSetting){
        settings.remove("default");
        settings.put("default", defaultSetting);
    }

    public MailDefaultSetting getCacheData(){
        return settings.get("default");
    }

}
