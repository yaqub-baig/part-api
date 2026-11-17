package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import com.autoadda.apis.part.controller.OrderController;
import com.autoadda.apis.part.controller.ShoppingCartController;
import com.autoadda.apis.part.exception.ForbiddenException;
import com.autoadda.apis.part.service.OrderService;
import com.autoadda.apis.part.service.ShoppingCartService;
import com.autoadda.apis.part.util.CallerIdentity;
import com.autoadda.apis.part.util.PaymentMethod;

/** Cart and order calls only work for the logged-in customer (X-Auto-Adda-Id). */
class CustomerIdentityTest {

	private final ShoppingCartService cartService = mock(ShoppingCartService.class);
	private final OrderService orderService = mock(OrderService.class);
	private final ShoppingCartController cart = new ShoppingCartController(cartService);
	private final OrderController orders = new OrderController(orderService);

	@Test
	void anotherCustomersCartIsForbidden() {
		assertThrows(ForbiddenException.class, () -> cart.addToCart("2", 1, 10, 1));
		assertThrows(ForbiddenException.class, () -> cart.viewCart("2", 1));
		assertThrows(ForbiddenException.class, () -> cart.updateQuantity("2", 1, 10, 3));
		assertThrows(ForbiddenException.class, () -> cart.removeItem("2", 1, 10));
		assertThrows(ForbiddenException.class, () -> cart.clearCart("2", 1));
		assertThrows(ForbiddenException.class, () -> cart.viewCart("not-a-number", 1));
		verifyNoInteractions(cartService);
	}

	@Test
	void ownCartWorks() {
		cart.addToCart(" 1 ", 1, 10, 2);
		verify(cartService).addToCart(1, 10, 2);
	}

	@Test
	void placingOrListingOrdersForAnotherCustomerIsForbidden() {
		assertThrows(ForbiddenException.class, () -> orders.placeOrder("2", 1, PaymentMethod.COD));
		assertThrows(ForbiddenException.class, () -> orders.buyNow("2", 1, 10, 1, PaymentMethod.COD));
		assertThrows(ForbiddenException.class, () -> orders.getOrdersForOwner("2", 1));
		verifyNoInteractions(orderService);
	}

	@Test
	void orderByIdCallsPassTheCallerAsOwner() {
		orders.getOrder("7", 42);
		orders.cancelOrder("7", 42);
		orders.retryPayment("7", 42);
		verify(orderService).getOrder(42, 7);
		verify(orderService).cancelOrder(42, 7);
		verify(orderService).retryPayment(42, 7);
	}

	@Test
	void callerIdParsesTheHeader() {
		assertEquals(5, CallerIdentity.callerId(" 5 "));
		assertThrows(ForbiddenException.class, () -> CallerIdentity.callerId(""));
		assertThrows(ForbiddenException.class, () -> CallerIdentity.callerId(null));
	}
}
