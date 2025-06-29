package com.chickentest.scheduler;

import com.chickentest.service.FarmService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EggHatchScheduler {

    private final FarmService farmService;

    public EggHatchScheduler(FarmService farmService) {
        this.farmService = farmService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void hatchEggsJob() {
        farmService.hatchEggs();
    }
}
