package com.autoadda.apis.part.service;

import com.autoadda.apis.part.clients.PaymentGatewayClient;
import com.autoadda.apis.part.config.PaymentGatewayConfig;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.exception.PaymentGatewayException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.UnauthorizedAccessException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.request.PaymentCallbackRequest;
import com.autoadda.apis.part.response.PaymentInitiationResponse;
import com.autoadda.apis.part.response.payment.PaymentGatewayResponse;
import com.autoadda.apis.part.util.PaymentStatus;
import com.autoadda.apis.part.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Online payments for parts orders. Only the server-to-server webhook ({@link #handleWebhook}) can mark an
 * order as paid; the browser redirect never changes state, because its query string can be forged.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final Set<String> SUCCESS_STATUSES = Set.of ( "SUCCESS", "PAID", "COMPLETED" );
    private static final Set<String> FAILURE_STATUSES = Set.of ( "FAILED", "FAILURE", "CANCELLED", "CANCELED" );

    private final PaymentGatewayClient gatewayClient;
    private final PaymentGatewayConfig config;
    private final OrderRepository orderRepo;

    /**
     * Starts a gateway payment for a saved order. Runs in the caller's transaction: if the gateway call fails,
     * the exception rolls back the whole checkout (no order, no stock deducted).
     */
    public PaymentInitiationResponse initiate(Order order) {
        String reference = gatewayClient.generateOrderReference ( order.getId ( ) );
        PaymentGatewayResponse response = gatewayClient.createPayment ( order, reference );
        if ( !Boolean.TRUE.equals ( response.getSuccess ( ) ) ) {
            log.error ( "Gateway rejected payment for order {}: {}", order.getId ( ), response.getMessage ( ) );
            throw new PaymentGatewayException ( "Payment initiation failed"
                    + ( response.getMessage ( ) != null ? ": " + response.getMessage ( ) : "" ) );
        }

        LocalDateTime now = LocalDateTime.now ( );
        order.setGatewayOrderReference ( reference );
        order.setGatewayTransactionId ( response.getTransactionId ( ) );
        order.setPaymentInitiatedAt ( now );
        order.setPaymentExpiresAt ( now.plusMinutes ( config.getExpiryMinutes ( ) ) );
        order.setPaymentStatus ( PaymentStatus.PENDING );
        orderRepo.save ( order );

        return PaymentInitiationResponse.builder ( ).orderReference ( reference ).transactionId ( response.getTransactionId ( ) )
                .qrCodeImage ( response.getQrCodeImage ( ) ).paymentUrl ( response.getPaymentUrl ( ) )
                .amount ( PaymentGatewayClient.toAmount ( order.getTotalAmount ( ) ) ).currency ( config.getCurrency ( ) )
                .expiryMinutes ( config.getExpiryMinutes ( ) ).expiresAt ( order.getPaymentExpiresAt ( ) ).build ( );
    }

    /** Gateway webhook. Verifies the signature and amount, then applies the result. Safe to receive twice. */
    @Transactional
    public void handleWebhook(PaymentCallbackRequest callback) {
        if ( callback.getOrderReference ( ) == null ) {
            throw new ValidationException ( "orderReference is required" );
        }
        verifySignature ( callback );

        Order order = orderRepo.findByGatewayOrderReferenceForUpdate ( callback.getOrderReference ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Order not found for reference " + callback.getOrderReference ( ) ) );

        if ( callback.getTransactionId ( ) != null && order.getGatewayTransactionId ( ) != null
                && !callback.getTransactionId ( ).equals ( order.getGatewayTransactionId ( ) ) ) {
            log.error ( "Transaction ID mismatch for order {}: expected {}, got {}", order.getId ( ),
                    order.getGatewayTransactionId ( ), callback.getTransactionId ( ) );
            throw new ValidationException ( "Transaction ID mismatch" );
        }

        String status = callback.getStatus ( ) == null ? "" : callback.getStatus ( ).toUpperCase ( Locale.ROOT );
        if ( SUCCESS_STATUSES.contains ( status ) ) {
            handleSuccess ( order, callback );
        } else if ( FAILURE_STATUSES.contains ( status ) ) {
            handleFailure ( order, callback );
        } else {
            log.warn ( "Ignoring webhook with unknown status '{}' for order {}", callback.getStatus ( ), order.getId ( ) );
        }
    }

    /** Where to send the customer's browser after the gateway page. Does not change the order. */
    @Transactional(readOnly = true)
    public String redirectUrl(String orderReference, String status) {
        Optional<Order> order = orderReference == null ? Optional.empty ( ) : orderRepo.findByGatewayOrderReference ( orderReference );
        boolean looksSuccessful = status != null && SUCCESS_STATUSES.contains ( status.toUpperCase ( Locale.ROOT ) );
        String base = looksSuccessful && order.isPresent ( ) ? config.getFrontendSuccessUrl ( ) : config.getFrontendFailureUrl ( );
        // The frontend must read the real status from GET /orders/{id}; the webhook may arrive a few seconds later.
        return order.map ( o -> base + "?orderId=" + o.getId ( ) ).orElse ( base );
    }

    private void handleSuccess(Order order, PaymentCallbackRequest callback) {
        if ( order.getPaymentStatus ( ) == PaymentStatus.SUCCESS || order.getPaymentStatus ( ) == PaymentStatus.REFUND_PENDING ) {
            log.info ( "Duplicate success webhook for order {}, ignoring", order.getId ( ) );
            return;
        }
        BigDecimal expected = PaymentGatewayClient.toAmount ( order.getTotalAmount ( ) );
        if ( callback.getAmount ( ) == null || expected.compareTo ( callback.getAmount ( ).setScale ( 2, RoundingMode.HALF_UP ) ) != 0 ) {
            log.error ( "Amount mismatch for order {}: expected {}, got {}", order.getId ( ), expected, callback.getAmount ( ) );
            throw new ValidationException ( "Amount mismatch" );
        }
        if ( callback.getCurrency ( ) != null && !callback.getCurrency ( ).equalsIgnoreCase ( config.getCurrency ( ) ) ) {
            log.error ( "Currency mismatch for order {}: expected {}, got {}", order.getId ( ), config.getCurrency ( ), callback.getCurrency ( ) );
            throw new ValidationException ( "Currency mismatch" );
        }

        if ( callback.getTransactionId ( ) != null ) {
            order.setGatewayTransactionId ( callback.getTransactionId ( ) );
        }
        order.setPaymentCompletedAt ( LocalDateTime.now ( ) );

        if ( order.getStatus ( ) == Status.PENDING_PAYMENT ) {
            order.setStatus ( Status.CONFIRMED );
            order.setPaymentStatus ( PaymentStatus.SUCCESS );
            log.info ( "Order {} paid and confirmed", order.getId ( ) );
        } else {
            // Paid after it expired or was cancelled: stock is already released, so the money must go back.
            Status previous = order.getStatus ( );
            order.setStatus ( Status.PENDING_REFUND );
            order.setPaymentStatus ( PaymentStatus.REFUND_PENDING );
            log.error ( "Payment received for order {} in status {}; marked for refund", order.getId ( ), previous );
        }
        orderRepo.save ( order );
    }

    private void handleFailure(Order order, PaymentCallbackRequest callback) {
        if ( order.getStatus ( ) == Status.PENDING_PAYMENT && order.getPaymentStatus ( ) == PaymentStatus.PENDING ) {
            order.setPaymentStatus ( PaymentStatus.FAILED );
            orderRepo.save ( order );
            log.warn ( "Payment failed for order {}: {} {}", order.getId ( ), callback.getErrorCode ( ), callback.getErrorMessage ( ) );
        } else {
            log.info ( "Ignoring failure webhook for order {} in status {}/{}", order.getId ( ), order.getStatus ( ), order.getPaymentStatus ( ) );
        }
    }

    /**
     * HMAC-SHA256 over "orderReference|transactionId|status|amount" with the callback secret, hex encoded.
     * TODO: confirm the exact signing scheme with almarwan before go-live and adjust {@link #computeSignature}.
     */
    void verifySignature(PaymentCallbackRequest callback) {
        if ( !config.isVerifySignature ( ) ) {
            log.warn ( "Webhook signature verification is DISABLED (payment.gateway.verify-signature=false)" );
            return;
        }
        if ( config.getCallbackSecret ( ) == null || config.getCallbackSecret ( ).isBlank ( ) ) {
            log.error ( "payment.gateway.callback-secret is not set; rejecting webhook" );
            throw new UnauthorizedAccessException ( "Webhook signature cannot be verified" );
        }
        if ( callback.getSignature ( ) == null ) {
            throw new UnauthorizedAccessException ( "Missing webhook signature" );
        }
        byte[] expected = computeSignature ( callback, config.getCallbackSecret ( ) ).getBytes ( StandardCharsets.UTF_8 );
        byte[] actual = callback.getSignature ( ).trim ( ).toLowerCase ( Locale.ROOT ).getBytes ( StandardCharsets.UTF_8 );
        if ( !MessageDigest.isEqual ( expected, actual ) ) {
            log.error ( "Invalid webhook signature for reference {}", callback.getOrderReference ( ) );
            throw new UnauthorizedAccessException ( "Invalid webhook signature" );
        }
    }

    public static String computeSignature(PaymentCallbackRequest callback, String secret) {
        String payload = String.join ( "|", nz ( callback.getOrderReference ( ) ), nz ( callback.getTransactionId ( ) ),
                nz ( callback.getStatus ( ) ), callback.getAmount ( ) == null ? "" : callback.getAmount ( ).toPlainString ( ) );
        try {
            Mac mac = Mac.getInstance ( "HmacSHA256" );
            mac.init ( new SecretKeySpec ( secret.getBytes ( StandardCharsets.UTF_8 ), "HmacSHA256" ) );
            return HexFormat.of ( ).formatHex ( mac.doFinal ( payload.getBytes ( StandardCharsets.UTF_8 ) ) );
        } catch ( GeneralSecurityException e ) {
            throw new IllegalStateException ( "HmacSHA256 unavailable", e );
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
