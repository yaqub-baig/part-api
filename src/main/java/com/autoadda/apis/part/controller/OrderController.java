package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.service.OrderService;
import com.autoadda.apis.part.util.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Checkout: place an order for everything in the cart.
     * paymentMethod=ONLINE returns {@code payment.paymentUrl} / {@code payment.qrCodeImage}; COD is confirmed at once.
     */
    @PostMapping("/place/{vehicleOwnerId}")
    public ResponseEntity<OrderResponse> placeOrder(@PathVariable Integer vehicleOwnerId,
                                                    @RequestParam(defaultValue = "COD") PaymentMethod paymentMethod) {
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( orderService.placeOrder ( vehicleOwnerId, paymentMethod ) );
    }

    /** Buy now: order a single part directly, bypassing the cart. */
    @PostMapping("/buy-now/{vehicleOwnerId}")
    public ResponseEntity<OrderResponse> buyNow(@PathVariable Integer vehicleOwnerId, @RequestParam Integer partId,
                                                @RequestParam(defaultValue = "1") int quantity,
                                                @RequestParam(defaultValue = "COD") PaymentMethod paymentMethod) {
        return ResponseEntity.status ( HttpStatus.CREATED )
                .body ( orderService.buyNow ( vehicleOwnerId, partId, quantity, paymentMethod ) );
    }

    /** Start a new online payment attempt after the previous one failed. */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> retryPayment(@PathVariable Integer orderId,
                                                      @RequestParam(required = false) Integer vehicleOwnerId) {
        return ResponseEntity.ok ( orderService.retryPayment ( orderId, vehicleOwnerId ) );
    }

    /** Record that cash was collected for a Cash on Delivery order. */
    @PostMapping("/{orderId}/cod-collected")
    public ResponseEntity<OrderResponse> markCodCollected(@PathVariable Integer orderId) {
        return ResponseEntity.ok ( orderService.markCodCollected ( orderId ) );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok ( orderService.getOrder ( orderId ) );
    }

    @GetMapping("/owner/{vehicleOwnerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersForOwner(@PathVariable Integer vehicleOwnerId) {
        return ResponseEntity.ok ( orderService.getOrdersForOwner ( vehicleOwnerId ) );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Integer orderId,
                                                     @RequestParam(required = false) Integer vehicleOwnerId) {
        return ResponseEntity.ok ( orderService.cancelOrder ( orderId, vehicleOwnerId ) );
    }
}
