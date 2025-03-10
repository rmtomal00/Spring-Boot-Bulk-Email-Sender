package com.orbaic.email.starterServices;

import com.orbaic.email.starterServices.staterHelperService.DefaultSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitialDataService implements CommandLineRunner {
    private final DefaultSetting defaultSetting;
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting Email Service Application");
        defaultSetting.setMailDefaultSetting();
    }
}
