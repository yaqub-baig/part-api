package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.entity.OrderItem;
import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.entity.ShoppingCartItem;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.PaymentGatewayException;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.InventoryTransactionRepository;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;
import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.response.PaymentInitiationResponse;
import com.autoadda.apis.part.service.OrderService;
import com.autoadda.apis.part.service.PaymentService;
import com.autoadda.apis.part.util.PaymentMethod;
import com.autoadda.apis.part.util.PaymentStatus;
import com.autoadda.apis.part.util.Status;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private ShoppingCartRepository cartRepo;
	@Mock
	private OrderRepository orderRepo;
	@Mock
	private InventoryPartRepository partRepo;
	@Mock
	private InventoryTransactionRepository txnRepo;
	@Mock
	private EntityManager entityManager;
	@Mock
	private PaymentService paymentService;
	@InjectMocks
	private OrderService service;

	private InventoryPart part(int id, int stock, double price) {
		return InventoryPart.builder().id(id).partName("Part " + id).quantityInStock(stock).unitPrice(price)
				.isActive(true).build();
	}

	private ShoppingCart cartWith(InventoryPart part, int qty) {
		ShoppingCart cart = ShoppingCart.builder().vehicleOwnerId(1).build();
		cart.getItems().add(ShoppingCartItem.builder().part(part).quantity(qty).cart(cart).build());
		return cart;
	}

	private Order orderWith(InventoryPart part, int qty, Status status, PaymentMethod method, PaymentStatus ps) {
		Order order = Order.builder().id(42).vehicleOwnerId(1).status(status).paymentMethod(method).paymentStatus(ps)
				.totalAmount(qty * part.getUnitPrice()).items(new ArrayList<>()).build();
		order.getItems().add(OrderItem.builder().part(part).quantity(qty).unitPrice(part.getUnitPrice()).order(order).build());
		return order;
	}

	@Test
	void codOrderIsConfirmedImmediatelyAndEmptiesCart() {
		InventoryPart part = part(1, 10, 250.0);
		ShoppingCart cart = cartWith(part, 2);

		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.of(cart));
		when(partRepo.findById(1)).thenReturn(Optional.of(part));
		when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

		OrderResponse order = service.placeOrder(1, PaymentMethod.COD);

		assertEquals(Status.CONFIRMED, order.getStatus());
		assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
		assertEquals(500.0, order.getTotalAmount());
		assertEquals(8, part.getQuantityInStock());
		assertTrue(cart.getItems().isEmpty());
		assertNull(order.getPayment());
		verify(paymentService, never()).initiate(any());
	}

	@Test
	void onlineOrderAwaitsPaymentAndReturnsPaymentLink() {
		InventoryPart part = part(1, 10, 100.0);
		ShoppingCart cart = cartWith(part, 1);

		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.of(cart));
		when(partRepo.findById(1)).thenReturn(Optional.of(part));
		when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
		when(paymentService.initiate(any())).thenReturn(PaymentInitiationResponse.builder().paymentUrl("https://pay/x").build());

		OrderResponse order = service.placeOrder(1, PaymentMethod.ONLINE);

		assertEquals(Status.PENDING_PAYMENT, order.getStatus());
		assertNotNull(order.getPayment());
		assertEquals("https://pay/x", order.getPayment().getPaymentUrl());
		assertEquals(9, part.getQuantityInStock());
	}

	@Test
	void gatewayFailurePropagatesSoCheckoutRollsBack() {
		InventoryPart part = part(1, 10, 100.0);
		ShoppingCart cart = cartWith(part, 1);

		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.of(cart));
		when(partRepo.findById(1)).thenReturn(Optional.of(part));
		when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
		when(paymentService.initiate(any())).thenThrow(new PaymentGatewayException("down"));

		assertThrows(PaymentGatewayException.class, () -> service.placeOrder(1, PaymentMethod.ONLINE));
		// cart is cleared only after payment starts; the @Transactional rollback undoes the stock change in the DB
		assertEquals(1, cart.getItems().size());
		verify(cartRepo, never()).save(any());
	}

	@Test
	void placeOrderFailsWhenStockInsufficient() {
		InventoryPart part = part(1, 1, 100.0);
		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.of(cartWith(part, 3)));
		when(partRepo.findById(1)).thenReturn(Optional.of(part));

		assertThrows(ConflictException.class, () -> service.placeOrder(1, PaymentMethod.COD));
		verify(orderRepo, never()).save(any());
	}

	@Test
	void placeOrderFailsWhenCartEmpty() {
		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.of(ShoppingCart.builder().vehicleOwnerId(1).build()));
		assertThrows(ConflictException.class, () -> service.placeOrder(1, PaymentMethod.COD));
	}

	@Test
	void buyNowCreatesSingleItemOrder() {
		InventoryPart part = part(5, 4, 99.5);
		when(partRepo.findById(5)).thenReturn(Optional.of(part));
		when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

		OrderResponse order = service.buyNow(7, 5, 2, PaymentMethod.COD);

		assertEquals(7, order.getVehicleOwnerId());
		assertEquals(199.0, order.getTotalAmount());
		assertEquals(2, part.getQuantityInStock());
		verify(cartRepo, never()).findByVehicleOwnerId(any());
	}

	@Test
	void cancelOrderRestoresStock() {
		InventoryPart part = part(1, 3, 10.0);
		Order order = orderWith(part, 2, Status.CONFIRMED, PaymentMethod.COD, PaymentStatus.PENDING);

		when(orderRepo.findByIdForUpdate(42)).thenReturn(Optional.of(order));
		when(partRepo.findById(1)).thenReturn(Optional.of(part));
		when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

		OrderResponse res = service.cancelOrder(42, 1);

		assertEquals(Status.CANCELLED, res.getStatus());
		assertEquals(PaymentStatus.CANCELLED, res.getPaymentStatus());
		assertEquals(5, part.getQuantityInStock());
	}

	@Test
	void cancellingPaidOnlineOrderFlagsRefund() {
		InventoryPart part = part(1, 0, 10.0);
		Order order = orderWith(part, 1, Status.CONFIRMED, PaymentMethod.ONLINE, PaymentStatus.SUCCESS);

		when(orderRepo.findByIdForUpdate(42)).thenReturn(Optional.of(order));
		when(partRepo.findById(1)).thenReturn(Optional.of(part));
		when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

		assertEquals(PaymentStatus.REFUND_PENDING, service.cancelOrder(42, 1).getPaymentStatus());
	}

	@Test
	void cannotCancelShippedOrder() {
		Order order = Order.builder().id(42).vehicleOwnerId(1).status(Status.SHIPPED).items(List.of()).build();
		when(orderRepo.findByIdForUpdate(42)).thenReturn(Optional.of(order));
		assertThrows(ConflictException.class, () -> service.cancelOrder(42, 1));
	}

	@Test
	void expiredUnpaidOrderIsCancelledAndStockReleased() {
		InventoryPart part = part(1, 0, 10.0);
		Order order = orderWith(part, 3, Status.PENDING_PAYMENT, PaymentMethod.ONLINE, PaymentStatus.PENDING);
		order.setPaymentExpiresAt(LocalDateTime.now().minusMinutes(1));

		when(orderRepo.findByIdForUpdate(42)).thenReturn(Optional.of(order));
		when(partRepo.findById(1)).thenReturn(Optional.of(part));

		service.expireOrder(42);

		assertEquals(Status.CANCELLED, order.getStatus());
		assertEquals(PaymentStatus.EXPIRED, order.getPaymentStatus());
		assertEquals(3, part.getQuantityInStock());
	}

	@Test
	void expiryDoesNothingIfOrderWasPaidMeanwhile() {
		InventoryPart part = part(1, 0, 10.0);
		Order order = orderWith(part, 3, Status.CONFIRMED, PaymentMethod.ONLINE, PaymentStatus.SUCCESS);
		order.setPaymentExpiresAt(LocalDateTime.now().minusMinutes(1));
		when(orderRepo.findByIdForUpdate(42)).thenReturn(Optional.of(order));

		service.expireOrder(42);

		assertEquals(Status.CONFIRMED, order.getStatus());
		assertEquals(0, part.getQuantityInStock());
	}

	@Test
	void retryRejectedWhilePaymentStillPending() {
		InventoryPart part = part(1, 0, 10.0);
		Order order = orderWith(part, 1, Status.PENDING_PAYMENT, PaymentMethod.ONLINE, PaymentStatus.PENDING);
		order.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(10));
		when(orderRepo.findByIdForUpdate(42)).thenReturn(Optional.of(order));

		assertThrows(ConflictException.class, () -> service.retryPayment(42, 1));
	}

	@Test
	void retryAllowedAfterFailedAttempt() {
		InventoryPart part = part(1, 0, 10.0);
		Order order = orderWith(part, 1, Status.PENDING_PAYMENT, PaymentMethod.ONLINE, PaymentStatus.FAILED);
		order.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(10));
		when(orderRepo.findByIdForUpdate(42)).thenReturn(Optional.of(order));
		when(paymentService.initiate(order)).thenReturn(PaymentInitiationResponse.builder().paymentUrl("u2").build());

		assertEquals("u2", service.retryPayment(42, 1).getPayment().getPaymentUrl());
	}
}
