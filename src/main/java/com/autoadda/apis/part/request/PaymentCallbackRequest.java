package com.autoadda.apis.part.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Webhook payload sent by the payment gateway. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCallbackRequest {
    private String transactionId;
    private String orderReference;
    private String status; // SUCCESS / PAID / COMPLETED, FAILED / FAILURE, CANCELLED
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String signature;
    private String timestamp;
    private String errorCode;
    private String errorMessage;
}
