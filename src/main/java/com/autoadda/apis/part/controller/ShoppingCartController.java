package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin("*")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    @PostMapping("/{vehicleOwnerId}/add")
    public ResponseEntity<ShoppingCart> addToCart(@PathVariable Integer vehicleOwnerId, @RequestParam Integer partId,
                                                  @RequestParam int quantity) {
        return ResponseEntity.ok ( cartService.addToCart ( vehicleOwnerId, partId, quantity ) );
    }

    @GetMapping("/{vehicleOwnerId}")
    public ResponseEntity<ShoppingCart> viewCart(@PathVariable Integer vehicleOwnerId) {
        return ResponseEntity.ok ( cartService.getCart ( vehicleOwnerId ) );
    }

    @DeleteMapping("/{vehicleOwnerId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Integer vehicleOwnerId) {
        cartService.clearCart ( vehicleOwnerId );
        return ResponseEntity.noContent ( ).build ( );
    }
}
