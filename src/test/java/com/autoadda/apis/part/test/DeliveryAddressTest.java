package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.autoadda.apis.part.entity.DeliveryAddress;
import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.entity.ShoppingCartItem;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.InventoryTransactionRepository;
import com.autoadda.apis.part.repository.OrderItemRepository;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.response.SupplierOrderResponse;
import com.autoadda.apis.part.service.DeliveryAddresses;
import com.autoadda.apis.part.service.GarageOrderSync;
import com.autoadda.apis.part.service.OrderService;
import com.autoadda.apis.part.service.PaymentService;
import com.autoadda.apis.part.service.SupplierOrderService;
import com.autoadda.apis.part.util.PaymentMethod;

import jakarta.persistence.EntityManager;

/** Customer orders need a delivery address; it's stored with the order and shown to the shop. */
class DeliveryAddressTest {

	private static DeliveryAddress.DeliveryAddressBuilder address() {
		return DeliveryAddress.builder().contactName(" Sara Khan ").phone("050 123 4567")
				.addressLine1("Villa 12, Street 4").area("Al Barsha").city("Dubai");
	}

	@ParameterizedTest
	@ValueSource(strings = { "0501234567", "+971501234567", "00971501234567", "971 50 123 4567", "(050) 123-4567" })
	void uaeMobilesAreNormalised(String phone) {
		assertEquals("+971501234567", DeliveryAddresses.validated(address().phone(phone).build()).getPhone());
	}

	@Test
	void landlinesAreAcceptedAndTextIsTrimmed() {
		DeliveryAddress cleaned = DeliveryAddresses.validated(address().phone("04 123 4567").addressLine2("  ").build());
		assertEquals("+97141234567", cleaned.getPhone());
		assertEquals("Sara Khan", cleaned.getContactName());
		assertNull(cleaned.getAddressLine2());
	}

	@ParameterizedTest
	@ValueSource(strings = { "12345", "+441234567890", "0501234", "phone" })
	void nonUaePhonesAreRejected(String phone) {
		assertThrows(ValidationException.class, () -> DeliveryAddresses.validated(address().phone(phone).build()));
	}

	@Test
	void requiredFieldsAndCoordinatesAreChecked() {
		assertThrows(ValidationException.class, () -> DeliveryAddresses.validated(null));
		assertThrows(ValidationException.class, () -> DeliveryAddresses.validated(address().contactName(" ").build()));
		assertThrows(ValidationException.class, () -> DeliveryAddresses.validated(address().addressLine1(null).build()));
		assertThrows(ValidationException.class, () -> DeliveryAddresses.validated(address().area(null).build()));
		assertThrows(ValidationException.class, () -> DeliveryAddresses.validated(address().city("").build()));
		assertThrows(ValidationException.class, () -> DeliveryAddresses.validated(address().latitude(25.1).build()));
		assertThrows(ValidationException.class,
				() -> DeliveryAddresses.validated(address().latitude(95.0).longitude(55.2).build()));
		assertThrows(ValidationException.class, () -> DeliveryAddresses.validated(address().notes("x".repeat(501)).build()));
	}

	@Test
	void checkoutWithoutAddressOrdersNothing() {
		ShoppingCartRepository cartRepo = mock(ShoppingCartRepository.class);
		OrderRepository orderRepo = mock(OrderRepository.class);
		OrderService service = new OrderService(cartRepo, orderRepo, mock(InventoryPartRepository.class),
				mock(InventoryTransactionRepository.class), mock(EntityManager.class), mock(PaymentService.class),
				mock(SupplierRepository.class));
		assertThrows(ValidationException.class, () -> service.placeOrder(1, PaymentMethod.COD, (DeliveryAddress) null));
		assertThrows(ValidationException.class, () -> service.buyNow(1, 5, 1, PaymentMethod.COD, (DeliveryAddress) null));
		verify(cartRepo, never()).findByVehicleOwnerId(any());
		verify(orderRepo, never()).save(any());
	}

	@Test
	void addressIsKeptWithTheOrderAndShownToTheShop() {
		ShoppingCartRepository cartRepo = mock(ShoppingCartRepository.class);
		OrderRepository orderRepo = mock(OrderRepository.class);
		InventoryPartRepository partRepo = mock(InventoryPartRepository.class);
		OrderService service = new OrderService(cartRepo, orderRepo, partRepo, mock(InventoryTransactionRepository.class),
				mock(EntityManager.class), mock(PaymentService.class), mock(SupplierRepository.class));
		InventoryPart part = InventoryPart.builder().id(5).supplierId("9").partName("Pad").quantityInStock(3)
				.unitPrice(50.0).isActive(true).build();
		ShoppingCart cart = ShoppingCart.builder().vehicleOwnerId(1).items(new ArrayList<>()).build();
		cart.getItems().add(ShoppingCartItem.builder().cart(cart).part(part).quantity(1).build());
		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.of(cart));
		when(partRepo.findById(5)).thenReturn(Optional.of(part));
		Order[] saved = new Order[1];
		when(orderRepo.save(any(Order.class))).thenAnswer(inv -> {
			saved[0] = inv.getArgument(0);
			saved[0].setId(42);
			return saved[0];
		});

		OrderResponse order = service.placeOrder(1, PaymentMethod.COD, address().notes("Call on arrival").build());

		assertEquals("+971501234567", order.getDeliveryAddress().getPhone());
		assertEquals("Al Barsha", saved[0].getDeliveryAddress().getArea());

		SupplierRepository suppliers = mock(SupplierRepository.class);
		OrderItemRepository items = mock(OrderItemRepository.class);
		when(suppliers.findById(9)).thenReturn(Optional.of(Supplier.builder().id(9).sparePartShopOwner(900).build()));
		when(items.findForSupplier("9")).thenReturn(List.of(saved[0].getItems().get(0)));
		SupplierOrderService shop = new SupplierOrderService(suppliers, items, orderRepo, service,
				mock(GarageOrderSync.class));
		SupplierOrderResponse seen = shop.list("900", 9, null, null).get(0);
		assertEquals("Sara Khan", seen.getDeliveryAddress().getContactName());
		assertEquals("Call on arrival", seen.getDeliveryAddress().getNotes());
	}
}
