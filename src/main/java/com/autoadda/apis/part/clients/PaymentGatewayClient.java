package com.autoadda.apis.part.clients;

import com.autoadda.apis.part.config.PaymentGatewayConfig;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.exception.PaymentGatewayException;
import com.autoadda.apis.part.request.payment.PaymentGatewayRequest;
import com.autoadda.apis.part.response.payment.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP client for the almarwan.net payment gateway. Ported from identity-api's PaymentGatewayAdaptor,
 * but builds the request from a parts {@link Order} instead of a subscription.
 */
@Component
@Slf4j
public class PaymentGatewayClient {

    private final PaymentGatewayConfig config;
    private final RestClient restClient;

    public PaymentGatewayClient(PaymentGatewayConfig config, RestClient.Builder restClientBuilder) {
        this.config = config;
        this.restClient = restClientBuilder.build ( );
    }

    public PaymentGatewayResponse createPayment(Order order, String orderReference) {
        String url = config.getBaseUrl ( ) + config.getCreateEndpoint ( );
        PaymentGatewayRequest request = buildPaymentRequest ( order, orderReference );
        log.info ( "Creating gateway payment for order {} (ref {}), amount {}", order.getId ( ), orderReference, request.getAmount ( ) );

        try {
            Map<String, Object> body = restClient.post ( ).uri ( url ).contentType ( MediaType.APPLICATION_JSON ).body ( request )
                    .retrieve ( ).body ( new ParameterizedTypeReference<Map<String, Object>> ( ) { } );
            if ( body == null ) {
                throw new PaymentGatewayException ( "Empty response from payment gateway" );
            }
            return parseGatewayResponse ( body );
        } catch ( RestClientException e ) {
            log.error ( "Failed to call payment gateway: {}", e.getMessage ( ), e );
            throw new PaymentGatewayException ( "Failed to connect to payment gateway", e );
        }
    }

    public static BigDecimal toAmount(Double value) {
        return BigDecimal.valueOf ( value == null ? 0.0 : value ).setScale ( 2, RoundingMode.HALF_UP );
    }

    /** Format: PO-{orderId}-{timestamp}-{random}. A new reference is generated for every attempt. */
    public String generateOrderReference(Integer orderId) {
        return String.format ( "PO-%d-%d-%s", orderId, System.currentTimeMillis ( ), UUID.randomUUID ( ).toString ( ).substring ( 0, 8 ) );
    }

    private PaymentGatewayRequest buildPaymentRequest(Order order, String orderReference) {
        BigDecimal amount = toAmount ( order.getTotalAmount ( ) );
        int quantity = order.getItems ( ).stream ( ).mapToInt ( i -> i.getQuantity ( ) ).sum ( );
        return PaymentGatewayRequest.builder ( )
                .subject ( "AutoAdda Parts Order #" + order.getId ( ) )
                .paySceneCode ( config.getPaySceneCode ( ) )
                .payChannelCode ( config.getPayChannelCode ( ) )
                .platformType ( config.getPlatformType ( ) )
                .amount ( amount )
                .goodsDetail ( PaymentGatewayRequest.GoodsDetail.builder ( )
                        .body ( "AutoAdda spare parts" )
                        .categoriesTree ( config.getCategoriesTree ( ) )
                        .goodsCategory ( config.getGoodsCategory ( ) )
                        .goodsId ( "ORDER-" + order.getId ( ) )
                        .goodsName ( "Parts order #" + order.getId ( ) )
                        .price ( PaymentGatewayRequest.GoodsDetail.Price.builder ( ).currency ( config.getCurrency ( ) ).amount ( amount ).build ( ) )
                        .quantity ( quantity )
                        .build ( ) )
                .terminalDetail ( PaymentGatewayRequest.TerminalDetail.builder ( )
                        .merchantName ( config.getMerchantName ( ) )
                        .operatorId ( config.getOperatorId ( ) )
                        .storeId ( config.getStoreId ( ) )
                        .terminalId ( config.getTerminalId ( ) )
                        .storeName ( config.getStoreName ( ) )
                        .build ( ) )
                .redirectUrl ( config.getCallbackUrl ( ) + "?orderRef=" + orderReference )
                .expiryMints ( config.getExpiryMinutes ( ) )
                .userLeft ( 1 )
                .isSendsms ( false )
                .mobno ( "" )
                .build ( );
    }

    private PaymentGatewayResponse parseGatewayResponse(Map<String, Object> body) {
        // Field names follow identity-api's parser, which accepts the gateway's alternate names.
        String transactionId = firstString ( body, "transactionId", "orderId", "orderNo" );
        Object success = body.get ( "success" );
        return PaymentGatewayResponse.builder ( )
                .success ( success instanceof Boolean b ? b : transactionId != null )
                .transactionId ( transactionId )
                .qrCodeImage ( firstString ( body, "qrCodeImage", "qrCode", "interactiveQR" ) )
                .paymentUrl ( firstString ( body, "paymentUrl", "redirectUrl" ) )
                .message ( firstString ( body, "message" ) )
                .status ( firstString ( body, "status" ) )
                .build ( );
    }

    private static String firstString(Map<String, Object> body, String... keys) {
        for ( String key : keys ) {
            Object v = body.get ( key );
            if ( v != null ) {
                return v.toString ( );
            }
        }
        return null;
    }
}
