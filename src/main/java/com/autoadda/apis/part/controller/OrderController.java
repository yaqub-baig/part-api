package com.autoadda.apis.part.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {
	@Autowired
	private OrderService orderService;

	@PostMapping("/place/{vehicleOwnerId}")
	public ResponseEntity<Order> placeOrder(@PathVariable Integer vehicleOwnerId) {
		return ResponseEntity.ok(orderService.placeOrder(vehicleOwnerId));
	}
}
