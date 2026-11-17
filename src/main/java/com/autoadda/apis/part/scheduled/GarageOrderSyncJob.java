package com.autoadda.apis.part.scheduled;

import com.autoadda.apis.part.service.GarageOrderSync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Hourly: resends garage job card updates that garage-api missed (it was down). */
@Slf4j
@Component
@RequiredArgsConstructor
public class GarageOrderSyncJob {

    private final GarageOrderSync garageOrderSync;

    @Scheduled(cron = "0 35 * * * *")
    public void retry() {
        try {
            garageOrderSync.retryPending();
        } catch (RuntimeException e) {
            log.error("Garage job sync retry failed", e);
        }
    }
}
