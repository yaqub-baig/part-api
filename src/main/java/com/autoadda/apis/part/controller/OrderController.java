package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.CheckoutRequest;
import com.autoadda.apis.part.service.CheckoutAddressResolver;
import com.autoadda.apis.part.util.CallerIdentity;
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
    private final CheckoutAddressResolver checkoutAddresses;

    /**
     * Checkout: place an order for everything in the cart.
     * paymentMethod=ONLINE returns {@code payment.paymentUrl} / {@code payment.qrCodeImage}; COD is confirmed at once.
     */
    @PostMapping("/place/{vehicleOwnerId}")
    public ResponseEntity<OrderResponse> placeOrder(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                                    @PathVariable Integer vehicleOwnerId,
                                                    @RequestParam(defaultValue = "COD") PaymentMethod paymentMethod,
                                                    @RequestBody(required = false) CheckoutRequest body) {
        return ResponseEntity.status ( HttpStatus.CREATED )
                .body ( orderService.placeOrder ( CallerIdentity.requireSelf ( autoAddaId, vehicleOwnerId ), paymentMethod,
                        checkoutAddresses.resolve ( vehicleOwnerId, body ) ) );
    }

    /** Buy now: order a single part directly, bypassing the cart. */
    @PostMapping("/buy-now/{vehicleOwnerId}")
    public ResponseEntity<OrderResponse> buyNow(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                                @PathVariable Integer vehicleOwnerId, @RequestParam Integer partId,
                                                @RequestParam(defaultValue = "1") int quantity,
                                                @RequestParam(defaultValue = "COD") PaymentMethod paymentMethod,
                                                @RequestBody(required = false) CheckoutRequest body) {
        return ResponseEntity.status ( HttpStatus.CREATED )
                .body ( orderService.buyNow ( CallerIdentity.requireSelf ( autoAddaId, vehicleOwnerId ), partId, quantity,
                        paymentMethod, checkoutAddresses.resolve ( vehicleOwnerId, body ) ) );
    }



    /** Start a new online payment attempt after the previous one failed. */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> retryPayment(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                                      @PathVariable Integer orderId) {
        return ResponseEntity.ok ( orderService.retryPayment ( orderId, CallerIdentity.callerId ( autoAddaId ) ) );
    }

    /** Record that cash was collected for a Cash on Delivery order. */
    @PostMapping("/{orderId}/cod-collected")
    public ResponseEntity<OrderResponse> markCodCollected(@PathVariable Integer orderId) {
        return ResponseEntity.ok ( orderService.markCodCollected ( orderId ) );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                                  @PathVariable Integer orderId) {
        return ResponseEntity.ok ( orderService.getOrder ( orderId, CallerIdentity.callerId ( autoAddaId ) ) );
    }

    @GetMapping("/owner/{vehicleOwnerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersForOwner(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                                                 @PathVariable Integer vehicleOwnerId) {
        return ResponseEntity.ok ( orderService.getOrdersForOwner ( CallerIdentity.requireSelf ( autoAddaId, vehicleOwnerId ) ) );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                                     @PathVariable Integer orderId) {
        return ResponseEntity.ok ( orderService.cancelOrder ( orderId, CallerIdentity.callerId ( autoAddaId ) ) );
    }
}
