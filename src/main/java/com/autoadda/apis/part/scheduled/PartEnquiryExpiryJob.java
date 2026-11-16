package com.autoadda.apis.part.scheduled;

import com.autoadda.apis.part.service.PartEnquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Hourly: closes part enquiries that are past their deadline and have no quote the customer can still accept. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartEnquiryExpiryJob {

    private final PartEnquiryService partEnquiryService;

    @Scheduled(cron = "0 15 * * * *")
    public void expire() {
        try {
            partEnquiryService.expireEnquiries();
        } catch (RuntimeException e) {
            log.error("Part enquiry expiry failed", e);
        }
    }
}
