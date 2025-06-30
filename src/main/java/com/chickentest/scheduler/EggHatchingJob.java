package com.chickentest.scheduler;

import com.chickentest.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EggHatchingJob {

    @Autowired
    private FarmService farmService;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void performEggHatching() {
        farmService.hatchEggsInternal();
    }
}
