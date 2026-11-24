package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.autoadda.apis.part.clients.AuthClient;
import com.autoadda.apis.part.clients.identity.SavedAddress;
import com.autoadda.apis.part.entity.DeliveryAddress;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.UpstreamUnavailableException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.request.CheckoutRequest;
import com.autoadda.apis.part.service.CheckoutAddressResolver;
import com.autoadda.apis.part.service.DeliveryAddresses;
import com.autoadda.apis.part.service.OrderService.Checkout;
import com.autoadda.apis.part.util.DeliveryMethod;

import feign.FeignException;
import feign.Request;

/** Checkout with a saved address (identity-api) instead of typing it. */
class CheckoutAddressResolverTest {

	private final AuthClient identity = mock(AuthClient.class);
	private final CheckoutAddressResolver resolver = new CheckoutAddressResolver(identity);

	private static final SavedAddress HOME = SavedAddress.builder().id(7L).userId(42).label("Home")
			.contactName("Sara Khan").phone("+971501234567").addressLine1("Villa 12, Street 4").area("Al Barsha")
			.city("Dubai").emirate("Dubai").mapLink("https://maps/x").latitude(25.11).longitude(55.2).notes("Gate 3").build();

	private static FeignException feign(int status) {
		Request request = Request.create(Request.HttpMethod.GET, "http://identity/x", Map.of(), null, StandardCharsets.UTF_8, null);
		return FeignException.errorStatus("getSavedAddress", feign.Response.builder().status(status).reason("x")
				.request(request).headers(Map.of()).body("", StandardCharsets.UTF_8).build());
	}

	@Test
	void savedAddressBecomesTheDeliveryAddress() {
		when(identity.getSavedAddress(7L, 42)).thenReturn(HOME);

		Checkout checkout = resolver.resolve(42, CheckoutRequest.builder().addressId(7L).build());

		assertEquals(DeliveryMethod.DELIVERY, checkout.method());
		DeliveryAddress address = checkout.deliveryAddress();
		assertEquals("Sara Khan", address.getContactName());
		assertEquals("Al Barsha", address.getArea());
		assertEquals("Gate 3", address.getNotes());
		assertEquals(25.11, address.getLatitude());
		// and it passes the same checks as a typed address
		assertEquals("+971501234567", DeliveryAddresses.validated(address).getPhone());
	}

	@Test
	void typedAddressStillWorksAndNeedsNoLookup() {
		DeliveryAddress typed = DeliveryAddress.builder().contactName("Ali").build();
		Checkout checkout = resolver.resolve(42, CheckoutRequest.builder().deliveryAddress(typed).build());
		assertSame(typed, checkout.deliveryAddress());
		verify(identity, never()).getSavedAddress(any(), any());
	}

	@Test
	void bothIsAmbiguous() {
		assertThrows(ValidationException.class, () -> resolver.resolve(42, CheckoutRequest.builder().addressId(7L)
				.deliveryAddress(DeliveryAddress.builder().build()).build()));
	}

	@Test
	void someoneElsesOrUnknownAddressIsNotFound() {
		when(identity.getSavedAddress(8L, 42)).thenThrow(feign(404));
		assertThrows(ResourceNotFoundException.class, () -> resolver.resolve(42, CheckoutRequest.builder().addressId(8L).build()));

		SavedAddress other = SavedAddress.builder().id(9L).userId(43).build();
		when(identity.getSavedAddress(9L, 42)).thenReturn(other);
		assertThrows(ResourceNotFoundException.class, () -> resolver.resolve(42, CheckoutRequest.builder().addressId(9L).build()));
	}

	@Test
	void identityDownIsA503() {
		when(identity.getSavedAddress(7L, 42)).thenThrow(feign(500));
		assertThrows(UpstreamUnavailableException.class, () -> resolver.resolve(42, CheckoutRequest.builder().addressId(7L).build()));
	}

	@Test
	void pickupCanUseTheSavedAddressesNameAndPhone() {
		when(identity.getSavedAddress(7L, 42)).thenReturn(HOME);
		Checkout checkout = resolver.resolve(42, CheckoutRequest.builder().deliveryMethod(DeliveryMethod.PICKUP).addressId(7L).build());
		assertNull(checkout.deliveryAddress());
		assertEquals("Sara Khan", DeliveryAddresses.validatedContact(checkout.pickupContact()).getContactName());

		DeliveryAddress contact = DeliveryAddress.builder().contactName("Omar").phone("0509999999").build();
		Checkout given = resolver.resolve(42, CheckoutRequest.builder().deliveryMethod(DeliveryMethod.PICKUP)
				.addressId(7L).pickupContact(contact).build());
		assertSame(contact, given.pickupContact());
	}

	@Test
	void noBodyMeansNoAddress() {
		Checkout checkout = resolver.resolve(42, null);
		assertNull(checkout.deliveryAddress());
		assertNull(checkout.method());
	}
}
