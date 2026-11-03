package com.autoadda.apis.part.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** What the frontend needs to send the customer to the gateway. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInitiationResponse {
    private String orderReference;
    private String transactionId;
    private String qrCodeImage; // Base64 encoded QR code
    private String paymentUrl;  // Direct URL to the payment page
    private BigDecimal amount;
    private String currency;
    private Integer expiryMinutes;
    private LocalDateTime expiresAt;
}
