package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;
import com.autoadda.apis.part.service.ShoppingCartService;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceTest {

	@Mock
	private ShoppingCartRepository cartRepo;
	@Mock
	private InventoryPartRepository partRepo;
	@InjectMocks
	private ShoppingCartService service;

	@Test
	public void testAddToCart_NewCart() {
		InventoryPart part = new InventoryPart();
		part.setId(1);

		when(partRepo.findById(1)).thenReturn(Optional.of(part));
		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.empty());
		when(cartRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

		ShoppingCart cart = service.addToCart(1, 1, 2);

		assertEquals(1, cart.getItems().size());
		assertEquals(part, cart.getItems().get(0).getPart());
	}
}
