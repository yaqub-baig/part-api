package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.util.CallerIdentity;
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
    public ResponseEntity<CartResponse> addToCart(@RequestHeader(CallerIdentity.HEADER) String autoAddaId, @PathVariable Integer vehicleOwnerId, @RequestParam Integer partId,
                                                  @RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok ( cartService.addToCart ( CallerIdentity.requireSelf ( autoAddaId, vehicleOwnerId ), partId, quantity ) );
    }

    @GetMapping("/{vehicleOwnerId}")
    public ResponseEntity<CartResponse> viewCart(@RequestHeader(CallerIdentity.HEADER) String autoAddaId, @PathVariable Integer vehicleOwnerId) {
        return ResponseEntity.ok ( cartService.getCart ( CallerIdentity.requireSelf ( autoAddaId, vehicleOwnerId ) ) );
    }

    /** Set an item's quantity; 0 removes it. */
    @PutMapping("/{vehicleOwnerId}/items/{partId}")
    public ResponseEntity<CartResponse> updateQuantity(@RequestHeader(CallerIdentity.HEADER) String autoAddaId, @PathVariable Integer vehicleOwnerId, @PathVariable Integer partId,
                                                       @RequestParam int quantity) {
        return ResponseEntity.ok ( cartService.updateItemQuantity ( CallerIdentity.requireSelf ( autoAddaId, vehicleOwnerId ), partId, quantity ) );
    }

    @DeleteMapping("/{vehicleOwnerId}/items/{partId}")
    public ResponseEntity<CartResponse> removeItem(@RequestHeader(CallerIdentity.HEADER) String autoAddaId, @PathVariable Integer vehicleOwnerId, @PathVariable Integer partId) {
        return ResponseEntity.ok ( cartService.removeItem ( CallerIdentity.requireSelf ( autoAddaId, vehicleOwnerId ), partId ) );
    }

    @DeleteMapping("/{vehicleOwnerId}/clear")
    public ResponseEntity<Void> clearCart(@RequestHeader(CallerIdentity.HEADER) String autoAddaId, @PathVariable Integer vehicleOwnerId) {
        cartService.clearCart ( CallerIdentity.requireSelf ( autoAddaId, vehicleOwnerId ) );
        return ResponseEntity.noContent ( ).build ( );
    }
}
