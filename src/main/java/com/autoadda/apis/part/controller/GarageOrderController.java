package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.GarageOrderRequest;
import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.service.GarageOrderService;
import com.autoadda.apis.part.util.CallerIdentity;
import com.autoadda.apis.part.util.Status;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Orders in a garage's name, delivered to the garage. The caller (X-Auto-Adda-Id) needs PARTS_PURCHASE in the
 * garage; garage-api checks it on every call (403 otherwise).
 */
@RestController
@RequestMapping("/garages/{garageId}/orders")
@RequiredArgsConstructor
public class GarageOrderController {

    private final GarageOrderService garageOrderService;

    @Operation(summary = "Place a garage order (items may be for job card part request items)")
    @PostMapping
    public ResponseEntity<OrderResponse> place(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                               @PathVariable Integer garageId,
                                               @RequestBody GarageOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(garageOrderService.place(autoAddaId, garageId, request));
    }

    @Operation(summary = "The garage's orders, newest first")
    @GetMapping
    public List<OrderResponse> list(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                    @PathVariable Integer garageId,
                                    @RequestParam(required = false) Status status) {
        return garageOrderService.list(autoAddaId, garageId, status);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                             @PathVariable Integer garageId, @PathVariable Integer orderId) {
        return garageOrderService.get(autoAddaId, garageId, orderId);
    }

    @Operation(summary = "Start a new online payment attempt")
    @PostMapping("/{orderId}/pay")
    public OrderResponse pay(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                             @PathVariable Integer garageId, @PathVariable Integer orderId) {
        return garageOrderService.pay(autoAddaId, garageId, orderId);
    }

    @Operation(summary = "Cancel before any shop has dispatched it")
    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                @PathVariable Integer garageId, @PathVariable Integer orderId) {
        return garageOrderService.cancel(autoAddaId, garageId, orderId);
    }

    @Operation(summary = "Confirm receipt (body {\"itemIds\": [...]}, or empty for every accepted/dispatched line)")
    @PostMapping("/{orderId}/received")
    public OrderResponse received(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                  @PathVariable Integer garageId, @PathVariable Integer orderId,
                                  @RequestBody(required = false) Map<String, List<Long>> body) {
        return garageOrderService.received(autoAddaId, garageId, orderId, body == null ? null : body.get("itemIds"));
    }
}
