package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.entity.ShoppingCartItem;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;
import com.autoadda.apis.part.response.CartResponse;
import com.autoadda.apis.part.service.ShoppingCartService;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

	@Mock
	private ShoppingCartRepository cartRepo;
	@Mock
	private InventoryPartRepository partRepo;
	@InjectMocks
	private ShoppingCartService service;

	private InventoryPart part(int stock) {
		return InventoryPart.builder().id(1).partName("Brake Pad").quantityInStock(stock).unitPrice(150.0)
				.isActive(true).build();
	}

	@Test
	void testAddToCart_NewCart() {
		InventoryPart part = part(10);
		when(partRepo.findById(1)).thenReturn(Optional.of(part));
		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.empty());
		when(cartRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

		CartResponse cart = service.addToCart(1, 1, 2);

		assertEquals(1, cart.getItems().size());
		assertEquals(1, cart.getItems().get(0).getPartId());
		assertEquals(1, cart.getVehicleOwnerId());
		assertEquals(300.0, cart.getTotalAmount());
	}

	@Test
	void addingSamePartTwiceMergesQuantity() {
		InventoryPart part = part(10);
		ShoppingCart cart = ShoppingCart.builder().vehicleOwnerId(1).build();
		cart.getItems().add(ShoppingCartItem.builder().part(part).quantity(2).cart(cart).build());

		when(partRepo.findById(1)).thenReturn(Optional.of(part));
		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.of(cart));
		when(cartRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

		CartResponse res = service.addToCart(1, 1, 3);

		assertEquals(1, res.getItems().size());
		assertEquals(5, res.getItems().get(0).getQuantity());
	}

	@Test
	void cannotAddMoreThanStock() {
		when(partRepo.findById(1)).thenReturn(Optional.of(part(2)));
		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.empty());
		assertThrows(ConflictException.class, () -> service.addToCart(1, 1, 3));
	}

	@Test
	void cannotAddInactivePart() {
		InventoryPart part = part(5);
		part.setIsActive(false);
		when(partRepo.findById(1)).thenReturn(Optional.of(part));
		assertThrows(ConflictException.class, () -> service.addToCart(1, 1, 1));
	}

	@Test
	void rejectsNonPositiveQuantity() {
		assertThrows(ValidationException.class, () -> service.addToCart(1, 1, 0));
	}

	@Test
	void getCartReturnsEmptyCartWhenNoneExists() {
		when(cartRepo.findByVehicleOwnerId(9)).thenReturn(Optional.empty());
		CartResponse res = service.getCart(9);
		assertTrue(res.getItems().isEmpty());
		assertEquals(0.0, res.getTotalAmount());
	}
}
