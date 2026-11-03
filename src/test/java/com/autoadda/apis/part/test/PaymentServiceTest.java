package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.clients.PaymentGatewayClient;
import com.autoadda.apis.part.config.PaymentGatewayConfig;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.exception.PaymentGatewayException;
import com.autoadda.apis.part.exception.UnauthorizedAccessException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.request.PaymentCallbackRequest;
import com.autoadda.apis.part.response.PaymentInitiationResponse;
import com.autoadda.apis.part.response.payment.PaymentGatewayResponse;
import com.autoadda.apis.part.service.PaymentService;
import com.autoadda.apis.part.util.PaymentMethod;
import com.autoadda.apis.part.util.PaymentStatus;
import com.autoadda.apis.part.util.Status;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	private static final String SECRET = "test-secret";
	private static final String REF = "PO-42-1-abcd";

	@Mock
	private PaymentGatewayClient gatewayClient;
	@Mock
	private OrderRepository orderRepo;

	private PaymentGatewayConfig config;
	private PaymentService service;

	@BeforeEach
	void setUp() {
		config = new PaymentGatewayConfig();
		config.setCallbackSecret(SECRET);
		config.setFrontendSuccessUrl("https://fe/ok");
		config.setFrontendFailureUrl("https://fe/fail");
		service = new PaymentService(gatewayClient, config, orderRepo);
	}

	private Order pendingOrder() {
		return Order.builder().id(42).vehicleOwnerId(1).status(Status.PENDING_PAYMENT).paymentMethod(PaymentMethod.ONLINE)
				.paymentStatus(PaymentStatus.PENDING).totalAmount(150.5).gatewayOrderReference(REF)
				.gatewayTransactionId("TX1").items(new ArrayList<>()).build();
	}

	private PaymentCallbackRequest signed(String status, String amount) {
		PaymentCallbackRequest cb = PaymentCallbackRequest.builder().orderReference(REF).transactionId("TX1").status(status)
				.amount(amount == null ? null : new BigDecimal(amount)).currency("AED").build();
		cb.setSignature(PaymentService.computeSignature(cb, SECRET));
		return cb;
	}

	@Test
	void initiateStoresReferenceAndReturnsLink() {
		Order order = pendingOrder();
		order.setPaymentStatus(PaymentStatus.NOT_INITIATED);
		when(gatewayClient.generateOrderReference(42)).thenReturn(REF);
		when(gatewayClient.createPayment(order, REF)).thenReturn(
				PaymentGatewayResponse.builder().success(true).transactionId("TX9").paymentUrl("https://pay").build());

		PaymentInitiationResponse res = service.initiate(order);

		assertEquals("https://pay", res.getPaymentUrl());
		assertEquals(new BigDecimal("150.50"), res.getAmount());
		assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
		assertEquals("TX9", order.getGatewayTransactionId());
		assertTrue(order.getPaymentExpiresAt().isAfter(order.getPaymentInitiatedAt()));
	}

	@Test
	void initiateThrowsWhenGatewayRejects() {
		Order order = pendingOrder();
		when(gatewayClient.generateOrderReference(42)).thenReturn(REF);
		when(gatewayClient.createPayment(order, REF)).thenReturn(PaymentGatewayResponse.builder().success(false).message("bad").build());
		assertThrows(PaymentGatewayException.class, () -> service.initiate(order));
	}

	@Test
	void validSuccessWebhookConfirmsOrder() {
		Order order = pendingOrder();
		when(orderRepo.findByGatewayOrderReferenceForUpdate(REF)).thenReturn(Optional.of(order));

		service.handleWebhook(signed("SUCCESS", "150.50"));

		assertEquals(Status.CONFIRMED, order.getStatus());
		assertEquals(PaymentStatus.SUCCESS, order.getPaymentStatus());
	}

	@Test
	void duplicateSuccessWebhookIsNoOp() {
		Order order = pendingOrder();
		order.setStatus(Status.CONFIRMED);
		order.setPaymentStatus(PaymentStatus.SUCCESS);
		when(orderRepo.findByGatewayOrderReferenceForUpdate(REF)).thenReturn(Optional.of(order));

		service.handleWebhook(signed("SUCCESS", "150.50"));

		verify(orderRepo, never()).save(any());
	}

	@Test
	void badSignatureIsRejected() {
		PaymentCallbackRequest cb = signed("SUCCESS", "150.50");
		cb.setAmount(new BigDecimal("1.00")); // tampered after signing
		assertThrows(UnauthorizedAccessException.class, () -> service.handleWebhook(cb));
		verify(orderRepo, never()).findByGatewayOrderReferenceForUpdate(any());
	}

	@Test
	void missingSignatureIsRejected() {
		PaymentCallbackRequest cb = signed("SUCCESS", "150.50");
		cb.setSignature(null);
		assertThrows(UnauthorizedAccessException.class, () -> service.handleWebhook(cb));
	}

	@Test
	void amountMismatchIsRejected() {
		when(orderRepo.findByGatewayOrderReferenceForUpdate(REF)).thenReturn(Optional.of(pendingOrder()));
		assertThrows(ValidationException.class, () -> service.handleWebhook(signed("SUCCESS", "10.00")));
	}

	@Test
	void lateSuccessOnCancelledOrderIsMarkedForRefund() {
		Order order = pendingOrder();
		order.setStatus(Status.CANCELLED);
		order.setPaymentStatus(PaymentStatus.EXPIRED);
		when(orderRepo.findByGatewayOrderReferenceForUpdate(REF)).thenReturn(Optional.of(order));

		service.handleWebhook(signed("PAID", "150.50"));

		assertEquals(Status.PENDING_REFUND, order.getStatus());
		assertEquals(PaymentStatus.REFUND_PENDING, order.getPaymentStatus());
	}

	@Test
	void failureWebhookAllowsRetry() {
		Order order = pendingOrder();
		when(orderRepo.findByGatewayOrderReferenceForUpdate(REF)).thenReturn(Optional.of(order));

		service.handleWebhook(signed("FAILED", null));

		assertEquals(Status.PENDING_PAYMENT, order.getStatus());
		assertEquals(PaymentStatus.FAILED, order.getPaymentStatus());
	}

	@Test
	void browserRedirectNeverChangesOrder() {
		when(orderRepo.findByGatewayOrderReference(REF)).thenReturn(Optional.of(pendingOrder()));

		assertEquals("https://fe/ok?orderId=42", service.redirectUrl(REF, "SUCCESS"));
		assertEquals("https://fe/fail", service.redirectUrl("unknown", "SUCCESS"));
		verify(orderRepo, never()).save(any());
	}
}
