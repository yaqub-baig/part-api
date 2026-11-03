package com.autoadda.apis.part.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Settings for the almarwan.net payment gateway (same provider identity-api uses for subscriptions).
 */
@Configuration
@ConfigurationProperties(prefix = "payment.gateway")
@Getter
@Setter
public class PaymentGatewayConfig {

    private String baseUrl;
    private String createEndpoint = "/create";
    /** Public URL of {@code GET /payments/callback}; the gateway sends the browser back here. */
    private String callbackUrl;
    /** Shared secret used to verify webhook signatures. */
    private String callbackSecret;
    /** Reject webhooks with a missing/invalid signature. Only disable for local testing. */
    private boolean verifySignature = true;

    // Terminal details
    private String merchantName;
    private String operatorId;
    private String storeId;
    private String terminalId;
    private String storeName;

    // Goods details
    private String goodsCategory;
    private String categoriesTree;

    // Payment settings
    /** How long an ONLINE order holds its stock while waiting for payment. */
    private Integer expiryMinutes = 15;
    private String currency = "AED";
    private String paySceneCode = "DYNQR";
    private String payChannelCode = "BANKCARD";
    private String platformType = "WEB";

    // Frontend redirect URLs (orderId is appended as a query param)
    private String frontendSuccessUrl;
    private String frontendFailureUrl;
}
