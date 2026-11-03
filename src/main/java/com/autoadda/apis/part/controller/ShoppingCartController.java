package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.response.CartResponse;
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

    /** Add a part to the cart (increments quantity if it's already there). */
    @PostMapping("/{vehicleOwnerId}/add")
    public ResponseEntity<CartResponse> addToCart(@PathVariable Integer vehicleOwnerId, @RequestParam Integer partId,
                                                  @RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok ( cartService.addToCart ( vehicleOwnerId, partId, quantity ) );
    }

    @GetMapping("/{vehicleOwnerId}")
    public ResponseEntity<CartResponse> viewCart(@PathVariable Integer vehicleOwnerId) {
        return ResponseEntity.ok ( cartService.getCart ( vehicleOwnerId ) );
    }

    /** Set an item's quantity; 0 removes it. */
    @PutMapping("/{vehicleOwnerId}/items/{partId}")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Integer vehicleOwnerId, @PathVariable Integer partId,
                                                       @RequestParam int quantity) {
        return ResponseEntity.ok ( cartService.updateItemQuantity ( vehicleOwnerId, partId, quantity ) );
    }

    @DeleteMapping("/{vehicleOwnerId}/items/{partId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Integer vehicleOwnerId, @PathVariable Integer partId) {
        return ResponseEntity.ok ( cartService.removeItem ( vehicleOwnerId, partId ) );
    }

    @DeleteMapping("/{vehicleOwnerId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Integer vehicleOwnerId) {
        cartService.clearCart ( vehicleOwnerId );
        return ResponseEntity.noContent ( ).build ( );
    }
}
