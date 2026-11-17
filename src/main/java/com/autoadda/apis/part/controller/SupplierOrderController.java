package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.response.SupplierOrderResponse;
import com.autoadda.apis.part.service.SupplierOrderService;
import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.CallerIdentity;
import com.autoadda.apis.part.util.FulfilmentStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** A shop's orders (customer and garage) and its delivery steps. Only the shop owner (X-Auto-Adda-Id). */
@RestController
@RequestMapping("/suppliers/{supplierId}/orders")
@RequiredArgsConstructor
public class SupplierOrderController {

    private final SupplierOrderService supplierOrderService;

    @Operation(summary = "The shop's orders, newest first (buyerType: CUSTOMER / GARAGE; status: of the shop's lines)")
    @GetMapping
    public List<SupplierOrderResponse> list(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                            @PathVariable Integer supplierId,
                                            @RequestParam(required = false) BuyerType buyerType,
                                            @RequestParam(required = false) FulfilmentStatus status) {
        return supplierOrderService.list(autoAddaId, supplierId, buyerType, status);
    }

    @GetMapping("/{orderId}")
    public SupplierOrderResponse get(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                     @PathVariable Integer supplierId, @PathVariable Integer orderId) {
        return supplierOrderService.get(autoAddaId, supplierId, orderId);
    }

    @PostMapping("/{orderId}/accept")
    public SupplierOrderResponse accept(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                        @PathVariable Integer supplierId, @PathVariable Integer orderId) {
        return supplierOrderService.accept(autoAddaId, supplierId, orderId);
    }

    @Operation(summary = "Reject the shop's lines not yet dispatched (body {\"reason\": \"...\"})")
    @PostMapping("/{orderId}/reject")
    public SupplierOrderResponse reject(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                        @PathVariable Integer supplierId, @PathVariable Integer orderId,
                                        @RequestBody(required = false) Map<String, String> body) {
        return supplierOrderService.reject(autoAddaId, supplierId, orderId, body == null ? null : body.get("reason"));
    }

    @PostMapping("/{orderId}/dispatch")
    public SupplierOrderResponse dispatch(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                          @PathVariable Integer supplierId, @PathVariable Integer orderId) {
        return supplierOrderService.dispatch(autoAddaId, supplierId, orderId);
    }

    @Operation(summary = "Customer orders only: confirm delivery")
    @PostMapping("/{orderId}/delivered")
    public SupplierOrderResponse delivered(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                           @PathVariable Integer supplierId, @PathVariable Integer orderId) {
        return supplierOrderService.delivered(autoAddaId, supplierId, orderId);
    }
}
