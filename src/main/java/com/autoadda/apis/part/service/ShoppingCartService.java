package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.entity.ShoppingCartItem;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;
import com.autoadda.apis.part.response.CartItemResponse;
import com.autoadda.apis.part.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartRepository cartRepo;
    private final InventoryPartRepository partRepo;

    /**
     * Adds a part to the owner's cart. If the part is already in the cart the quantity is increased.
     * Stock is validated but not reserved; it is deducted only when the order is placed.
     */
    @Transactional
    public CartResponse addToCart(Integer vehicleOwnerId, Integer partId, int quantity) {
        requirePositive ( quantity );
        InventoryPart part = loadPurchasablePart ( partId );

        ShoppingCart cart = cartRepo.findByVehicleOwnerId ( vehicleOwnerId )
                .orElseGet ( () -> ShoppingCart.builder ( ).vehicleOwnerId ( vehicleOwnerId ).build ( ) );

        Optional<ShoppingCartItem> existing = findItem ( cart, partId );
        int newQuantity = existing.map ( ShoppingCartItem::getQuantity ).orElse ( 0 ) + quantity;
        ensureStock ( part, newQuantity );

        if ( existing.isPresent ( ) ) {
            existing.get ( ).setQuantity ( newQuantity );
        } else {
            cart.getItems ( ).add ( ShoppingCartItem.builder ( ).cart ( cart ).part ( part ).quantity ( newQuantity ).build ( ) );
        }
        cart.setUpdatedAt ( LocalDateTime.now ( ) );
        return toResponse ( cartRepo.save ( cart ) );
    }

    /** Sets the quantity of a part already in the cart. A quantity of 0 removes the item. */
    @Transactional
    public CartResponse updateItemQuantity(Integer vehicleOwnerId, Integer partId, int quantity) {
        if ( quantity < 0 ) {
            throw new ValidationException ( "Quantity cannot be negative" );
        }
        if ( quantity == 0 ) {
            return removeItem ( vehicleOwnerId, partId );
        }
        ShoppingCart cart = loadCart ( vehicleOwnerId );
        ShoppingCartItem item = findItem ( cart, partId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Part " + partId + " is not in the cart" ) );
        ensureStock ( loadPurchasablePart ( partId ), quantity );
        item.setQuantity ( quantity );
        cart.setUpdatedAt ( LocalDateTime.now ( ) );
        return toResponse ( cartRepo.save ( cart ) );
    }

    @Transactional
    public CartResponse removeItem(Integer vehicleOwnerId, Integer partId) {
        ShoppingCart cart = loadCart ( vehicleOwnerId );
        boolean removed = cart.getItems ( ).removeIf ( i -> i.getPart ( ).getId ( ).equals ( partId ) );
        if ( !removed ) {
            throw new ResourceNotFoundException ( "Part " + partId + " is not in the cart" );
        }
        cart.setUpdatedAt ( LocalDateTime.now ( ) );
        return toResponse ( cartRepo.save ( cart ) );
    }

    @Transactional
    public void clearCart(Integer vehicleOwnerId) {
        cartRepo.findByVehicleOwnerId ( vehicleOwnerId ).ifPresent ( cart -> {
            cart.getItems ( ).clear ( );
            cart.setUpdatedAt ( LocalDateTime.now ( ) );
            cartRepo.save ( cart );
        } );
    }

    /** Returns the cart, or an empty cart if the owner has never added anything. */
    @Transactional(readOnly = true)
    public CartResponse getCart(Integer vehicleOwnerId) {
        return cartRepo.findByVehicleOwnerId ( vehicleOwnerId ).map ( this::toResponse )
                .orElseGet ( () -> CartResponse.builder ( ).vehicleOwnerId ( vehicleOwnerId ).totalAmount ( 0.0 ).build ( ) );
    }

    // ---- helpers ----

    private ShoppingCart loadCart(Integer vehicleOwnerId) {
        return cartRepo.findByVehicleOwnerId ( vehicleOwnerId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Cart not found for vehicle owner " + vehicleOwnerId ) );
    }

    private InventoryPart loadPurchasablePart(Integer partId) {
        InventoryPart part = partRepo.findById ( partId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Part not found: " + partId ) );
        if ( !Boolean.TRUE.equals ( part.getIsActive ( ) ) ) {
            throw new ConflictException ( "Part " + partId + " is not available for sale" );
        }
        return part;
    }

    private static Optional<ShoppingCartItem> findItem(ShoppingCart cart, Integer partId) {
        return cart.getItems ( ).stream ( ).filter ( i -> i.getPart ( ).getId ( ).equals ( partId ) ).findFirst ( );
    }

    static void requirePositive(int quantity) {
        if ( quantity <= 0 ) {
            throw new ValidationException ( "Quantity must be greater than 0" );
        }
    }

    static void ensureStock(InventoryPart part, int requested) {
        int inStock = part.getQuantityInStock ( ) == null ? 0 : part.getQuantityInStock ( );
        if ( requested > inStock ) {
            throw new ConflictException ( "Only " + inStock + " unit(s) of " + part.getPartName ( ) + " (part "
                    + part.getId ( ) + ") in stock, requested " + requested );
        }
    }

    CartResponse toResponse(ShoppingCart cart) {
        List<CartItemResponse> items = cart.getItems ( ).stream ( ).map ( i -> {
            InventoryPart p = i.getPart ( );
            double price = p.getUnitPrice ( ) == null ? 0.0 : p.getUnitPrice ( );
            int stock = p.getQuantityInStock ( ) == null ? 0 : p.getQuantityInStock ( );
            return CartItemResponse.builder ( ).cartItemId ( i.getId ( ) ).partId ( p.getId ( ) ).productId ( p.getProductId ( ) )
                    .supplierId ( p.getSupplierId ( ) ).sku ( p.getSku ( ) ).partName ( p.getPartName ( ) )
                    .partNumber ( p.getPartNumber ( ) ).unitPrice ( price ).quantity ( i.getQuantity ( ) )
                    .lineTotal ( price * i.getQuantity ( ) ).availableStock ( stock )
                    .available ( Boolean.TRUE.equals ( p.getIsActive ( ) ) && stock >= i.getQuantity ( ) ).build ( );
        } ).toList ( );

        return CartResponse.builder ( ).cartId ( cart.getId ( ) ).vehicleOwnerId ( cart.getVehicleOwnerId ( ) ).items ( items )
                .totalItems ( items.stream ( ).mapToInt ( CartItemResponse::getQuantity ).sum ( ) )
                .totalAmount ( items.stream ( ).mapToDouble ( CartItemResponse::getLineTotal ).sum ( ) ).build ( );
    }
}
