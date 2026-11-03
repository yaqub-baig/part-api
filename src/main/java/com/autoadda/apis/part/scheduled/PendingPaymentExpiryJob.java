package com.autoadda.apis.part.scheduled;

import com.autoadda.apis.part.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Cancels ONLINE orders that were not paid within the payment window and puts their stock back. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingPaymentExpiryJob {

    private final OrderService orderService;

    @Scheduled(fixedDelayString = "${payment.expiry-job.delay-ms:60000}")
    public void expireOverduePayments() {
        for ( Integer orderId : orderService.findOverduePendingPaymentOrderIds ( ) ) {
            try {
                orderService.expireOrder ( orderId ); // own transaction per order
            } catch ( Exception e ) {
                log.error ( "Failed to expire order {}", orderId, e );
            }
        }
    }
}
