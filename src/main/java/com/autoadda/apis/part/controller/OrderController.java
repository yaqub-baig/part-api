package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place/{vehicleOwnerId}")
    public ResponseEntity<Order> placeOrder(@PathVariable Integer vehicleOwnerId) {
        return ResponseEntity.ok ( orderService.placeOrder ( vehicleOwnerId ) );
    }
}
