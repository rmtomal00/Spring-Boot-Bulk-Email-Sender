package com.orbaic.email.schedule;

import com.orbaic.email.cacheData.ConcurrencyHashCache;
import com.orbaic.email.schedule.services.PrepareMailServe;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Scheduler {

    final PrepareMailServe serve;

    @Scheduled(fixedRate = 60000)
    public void mailServe() {
        System.out.println("Scheduler started");
        serve.serveEmail();
    }
}
