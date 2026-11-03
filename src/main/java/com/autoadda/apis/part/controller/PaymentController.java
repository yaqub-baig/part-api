package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.PaymentCallbackRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.service.OrderService;
import com.autoadda.apis.part.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    /** Server-to-server webhook from the payment gateway. The only call that can mark an order paid. */
    @PostMapping("/callback")
    public ResponseEntity<GenericResponse> webhook(@RequestBody PaymentCallbackRequest callback) {
        log.info ( "Payment webhook for reference {} status {}", callback.getOrderReference ( ), callback.getStatus ( ) );
        paymentService.handleWebhook ( callback );
        return ResponseEntity.ok ( GenericResponse.builder ( ).status ( "success" ).code ( "200" ).message ( "Callback processed" ).build ( ) );
    }

    /** Browser redirect after the gateway page. Never changes the order; the frontend then polls the order. */
    @GetMapping("/callback")
    public void browserRedirect(@RequestParam(required = false) String orderRef, @RequestParam(required = false) String status,
                                HttpServletResponse response) throws IOException {
        response.sendRedirect ( paymentService.redirectUrl ( orderRef, status ) );
    }

    /** Lightweight status for the frontend to poll after the redirect. */
    @GetMapping("/orders/{orderId}/status")
    public ResponseEntity<Map<String, Object>> status(@PathVariable Integer orderId) {
        OrderResponse order = orderService.getOrder ( orderId );
        Map<String, Object> body = new LinkedHashMap<> ( );
        body.put ( "orderId", order.getOrderId ( ) );
        body.put ( "orderStatus", order.getStatus ( ) );
        body.put ( "paymentMethod", order.getPaymentMethod ( ) );
        body.put ( "paymentStatus", order.getPaymentStatus ( ) );
        body.put ( "paymentExpiresAt", order.getPaymentExpiresAt ( ) );
        return ResponseEntity.ok ( body );
    }
}
