package com.autoadda.apis.parts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.parts.entity.ShoppingCart;
import com.autoadda.apis.parts.service.ShoppingCartService;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {
	@Autowired
	private ShoppingCartService cartService;

	@PostMapping("/{vehicleOwnerId}/add")
	public ResponseEntity<ShoppingCart> addToCart(@PathVariable Integer vehicleOwnerId, @RequestParam Integer partId,
			@RequestParam int quantity) {
		return ResponseEntity.ok(cartService.addToCart(vehicleOwnerId, partId, quantity));
	}

	@GetMapping("/{vehicleOwnerId}")
	public ResponseEntity<ShoppingCart> viewCart(@PathVariable Integer vehicleOwnerId) {
		return ResponseEntity.ok(cartService.getCart(vehicleOwnerId));
	}

	@DeleteMapping("/{vehicleOwnerId}/clear")
	public ResponseEntity<Void> clearCart(@PathVariable Integer vehicleOwnerId) {
		cartService.clearCart(vehicleOwnerId);
		return ResponseEntity.noContent().build();
	}
}
