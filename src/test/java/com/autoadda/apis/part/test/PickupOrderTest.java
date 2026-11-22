package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.autoadda.apis.part.entity.DeliveryAddress;
import com.autoadda.apis.part.entity.GarageSnapshot;
import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Location;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.entity.OrderItem;
import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.entity.ShoppingCartItem;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.InventoryTransactionRepository;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.repository.OrderItemRepository;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.request.GarageOrderRequest;
import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.response.PickupPointResponse;
import com.autoadda.apis.part.response.SupplierOrderResponse;
import com.autoadda.apis.part.service.GarageBuyerResolver;
import com.autoadda.apis.part.service.GarageBuyerResolver.GarageBuyer;
import com.autoadda.apis.part.service.GarageOrderService;
import com.autoadda.apis.part.service.GarageOrderSync;
import com.autoadda.apis.part.service.OrderService;
import com.autoadda.apis.part.service.OrderService.Checkout;
import com.autoadda.apis.part.service.PaymentService;
import com.autoadda.apis.part.service.SupplierOrderService;
import com.autoadda.apis.part.util.DeliveryMethod;
import com.autoadda.apis.part.util.FulfilmentStatus;
import com.autoadda.apis.part.util.PaymentMethod;
import com.autoadda.apis.part.util.Status;

import jakarta.persistence.EntityManager;

/** Self pickup: checkout without an address, pickup code, shops mark ready, hand over against the code. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PickupOrderTest {

	@Mock private ShoppingCartRepository cartRepo;
	@Mock private OrderRepository orderRepo;
	@Mock private InventoryPartRepository partRepo;
	@Mock private InventoryTransactionRepository txnRepo;
	@Mock private EntityManager entityManager;
	@Mock private PaymentService paymentService;
	@Mock private SupplierRepository supplierRepository;
	@Mock private OrderItemRepository orderItemRepository;
	@Mock private GarageBuyerResolver garageBuyerResolver;
	@Mock private MakeRepository makeRepository;
	@Mock private ModelRepository modelRepository;

	private OrderService orders;
	private SupplierOrderService shop;
	private GarageOrderService garageOrders;
	private InventoryPart pads;
	private Order saved;

	private static final String SHOP_OWNER = "900";
	private static final DeliveryAddress CONTACT = DeliveryAddress.builder().contactName("Sara Khan").phone("0501234567").build();

	@BeforeEach
	void setUp() {
		orders = new OrderService(cartRepo, orderRepo, partRepo, txnRepo, entityManager, paymentService, supplierRepository);
		GarageOrderSync sync = new GarageOrderSync(garageBuyerResolver, supplierRepository, orderItemRepository);
		shop = new SupplierOrderService(supplierRepository, orderItemRepository, orderRepo, orders, sync);
		garageOrders = new GarageOrderService(orders, orderRepo, garageBuyerResolver, sync, makeRepository, modelRepository);

		pads = InventoryPart.builder().id(10).supplierId("5").partName("Brake pads").quantityInStock(5).unitPrice(100.0)
				.isActive(true).build();
		when(partRepo.findById(10)).thenReturn(Optional.of(pads));
		when(supplierRepository.findById(5)).thenReturn(Optional.of(Supplier.builder().id(5).name("Al Noor Parts")
				.mobileNumber("0509999999").sparePartShopOwner(900).isActive(true).workingDays("Sat-Thu")
				.openingHours("09:00").closingHours("21:00")
				.location(Location.builder().streetAddress1("Shop 3, Auto Market").area("Al Quoz").city("Dubai")
						.gmap("https://maps/shop5").build())
				.build()));
		when(orderRepo.save(any(Order.class))).thenAnswer(inv -> {
			Order o = inv.getArgument(0);
			if (o.getId() == null) {
				o.setId(42);
				long id = 1;
				for (OrderItem line : o.getItems()) {
					line.setId(id++);
				}
			}
			saved = o;
			return o;
		});
		when(orderRepo.findByIdForUpdate(42)).thenAnswer(inv -> Optional.ofNullable(saved));
		when(orderRepo.findById(42)).thenAnswer(inv -> Optional.ofNullable(saved));
	}

	private OrderResponse buyNowForPickup() {
		return orders.buyNow(1, 10, 2, PaymentMethod.COD, new Checkout(DeliveryMethod.PICKUP, null, CONTACT));
	}

	@Test
	void pickupCheckoutNeedsOnlyWhoCollectsAndGivesACodeAndWhereToGo() {
		OrderResponse order = buyNowForPickup();

		assertEquals(DeliveryMethod.PICKUP, order.getDeliveryMethod());
		assertNull(order.getDeliveryAddress());
		assertEquals("+971501234567", order.getPickupContact().getPhone());
		assertTrue(order.getPickupCode().matches("\\d{6}"));
		PickupPointResponse point = order.getPickupPoints().get(0);
		assertEquals("Al Noor Parts", point.getName());
		assertEquals("Shop 3, Auto Market, Al Quoz, Dubai", point.getAddress());
		assertEquals("https://maps/shop5", point.getMapLink());
		assertEquals("Sat-Thu", point.getWorkingDays());
		assertEquals("09:00", point.getOpeningHours());
	}

	@Test
	void pickupWithoutContactOrdersNothing() {
		assertThrows(ValidationException.class,
				() -> orders.buyNow(1, 10, 1, PaymentMethod.COD, new Checkout(DeliveryMethod.PICKUP, null, null)));
		assertThrows(ValidationException.class, () -> orders.buyNow(1, 10, 1, PaymentMethod.COD,
				new Checkout(DeliveryMethod.PICKUP, null, DeliveryAddress.builder().contactName("Sara").phone("123").build())));
		verify(orderRepo, never()).save(any());
	}

	@Test
	void deliveryIsStillTheDefaultAndNeedsTheAddress() {
		assertThrows(ValidationException.class,
				() -> orders.buyNow(1, 10, 1, PaymentMethod.COD, new Checkout(null, null, CONTACT)));
	}

	@Test
	void shopPreparesThenHandsOverAgainstTheCode() {
		buyNowForPickup();
		assertThrows(ConflictException.class, () -> shop.dispatch(SHOP_OWNER, 5, 42));
		assertThrows(ConflictException.class, () -> shop.collected(SHOP_OWNER, 5, 42, saved.getPickupCode()));

		SupplierOrderResponse ready = shop.readyForPickup(SHOP_OWNER, 5, 42);
		assertEquals(Status.READY_FOR_PICKUP, ready.getOrderStatus());
		assertEquals(FulfilmentStatus.READY_FOR_PICKUP, ready.getItems().get(0).getFulfilmentStatus());
		assertEquals(DeliveryMethod.PICKUP, ready.getDeliveryMethod());
		assertEquals("Sara Khan", ready.getPickupContact().getContactName());
		assertNull(ready.getDeliveryAddress());

		String wrong = saved.getPickupCode().equals("000000") ? "111111" : "000000";
		assertThrows(ValidationException.class, () -> shop.collected(SHOP_OWNER, 5, 42, wrong));
		assertThrows(ConflictException.class, () -> shop.delivered(SHOP_OWNER, 5, 42));

		SupplierOrderResponse collected = shop.collected(SHOP_OWNER, 5, 42, " " + saved.getPickupCode() + " ");
		assertEquals(Status.COLLECTED, collected.getOrderStatus());
		assertEquals(FulfilmentStatus.RECEIVED, collected.getItems().get(0).getFulfilmentStatus());
	}

	@Test
	void customerCanStillCancelWhileItWaitsAtTheShop() {
		buyNowForPickup();
		shop.readyForPickup(SHOP_OWNER, 5, 42);
		assertEquals(3, pads.getQuantityInStock());

		OrderResponse cancelled = orders.cancelOrder(42, 1);
		assertEquals(Status.CANCELLED, cancelled.getStatus());
		assertEquals(5, pads.getQuantityInStock());
	}

	@Test
	void shopCanRejectALineWaitingAtTheShop() {
		buyNowForPickup();
		shop.readyForPickup(SHOP_OWNER, 5, 42);
		shop.reject(SHOP_OWNER, 5, 42, "Damaged");
		assertEquals(Status.CANCELLED, saved.getStatus());
		assertEquals(5, pads.getQuantityInStock());
	}

	@Test
	void readyForPickupIsOnlyForPickupOrders() {
		ShoppingCart cart = ShoppingCart.builder().vehicleOwnerId(1).items(new ArrayList<>()).build();
		cart.getItems().add(ShoppingCartItem.builder().cart(cart).part(pads).quantity(1).build());
		when(cartRepo.findByVehicleOwnerId(1)).thenReturn(Optional.of(cart));
		orders.placeOrder(1, PaymentMethod.COD, DeliveryAddress.builder().contactName("Sara").phone("0501234567")
				.addressLine1("Villa 1").area("Al Barsha").city("Dubai").build());

		assertNull(saved.getPickupCode());
		assertEquals(DeliveryMethod.DELIVERY, saved.getDeliveryMethod());
		assertThrows(ConflictException.class, () -> shop.readyForPickup(SHOP_OWNER, 5, 42));
	}

	@Test
	void garageStaffCanCollectAndTheGarageConfirms() {
		when(garageBuyerResolver.requirePurchaser("77", 3))
				.thenReturn(new GarageBuyer(3, 77, GarageSnapshot.builder().garageName("Speedy Garage").build()));
		OrderResponse order = garageOrders.place("77", 3, GarageOrderRequest.builder().deliveryMethod(DeliveryMethod.PICKUP)
				.items(List.of(GarageOrderRequest.Item.builder().inventoryPartId(10).quantity(1).build())).build());
		assertEquals(DeliveryMethod.PICKUP, order.getDeliveryMethod());
		assertNull(order.getPickupCode()); // the garage confirms receipt itself
		assertEquals(1, order.getPickupPoints().size());

		shop.readyForPickup(SHOP_OWNER, 5, 42);
		assertThrows(ConflictException.class, () -> shop.collected(SHOP_OWNER, 5, 42, "123456"));
		assertEquals(Status.COLLECTED, garageOrders.received("77", 3, 42, null).getStatus());
	}
}
