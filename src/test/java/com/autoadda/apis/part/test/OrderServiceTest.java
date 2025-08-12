package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.entity.ShoppingCartItem;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;
import com.autoadda.apis.part.service.OrderService;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

	@Mock
	private ShoppingCartRepository cartRepo;
	@Mock
	private OrderRepository orderRepo;
	@InjectMocks
	private OrderService service;

	@Test
	public void testPlaceOrder() {
		InventoryPart part = new InventoryPart();
		part.setId(1);

		ShoppingCartItem item = new ShoppingCartItem();
		item.setPart(part);
		item.setQuantity(2);

		ShoppingCart cart = new ShoppingCart();
		cart.setVehicleOwnerId(1);
		cart.setItems(List.of(item));

		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.of(cart));
		when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

		Order order = service.placeOrder(1);

		assertEquals(1, order.getItems().size());
		assertEquals("CREATED", order.getStatus());
	}
}