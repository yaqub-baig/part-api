package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.autoadda.apis.part.clients.garage.GaragePartContext;
import com.autoadda.apis.part.clients.garage.MarketplaceStep;
import com.autoadda.apis.part.clients.garage.MarketplaceStep.Stage;
import com.autoadda.apis.part.entity.GarageSnapshot;
import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.entity.OrderItem;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ForbiddenException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.UpstreamUnavailableException;
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
import com.autoadda.apis.part.response.OrderItemResponse;
import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.response.SupplierOrderResponse;
import com.autoadda.apis.part.service.GarageBuyerResolver;
import com.autoadda.apis.part.service.GarageBuyerResolver.GarageBuyer;
import com.autoadda.apis.part.service.GarageOrderService;
import com.autoadda.apis.part.service.GarageOrderSync;
import com.autoadda.apis.part.service.OrderService;
import com.autoadda.apis.part.service.PaymentService;
import com.autoadda.apis.part.service.SupplierOrderService;
import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.FulfilmentStatus;
import com.autoadda.apis.part.util.PaymentMethod;
import com.autoadda.apis.part.util.PaymentStatus;
import com.autoadda.apis.part.util.ProductCondition;
import com.autoadda.apis.part.util.Status;

import jakarta.persistence.EntityManager;

/** Garage orders end to end with mocked repositories and garage-api: place, shop steps, receive, job card sync. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GarageOrderTest {

	@Mock private ShoppingCartRepository cartRepo;
	@Mock private OrderRepository orderRepo;
	@Mock private InventoryPartRepository partRepo;
	@Mock private InventoryTransactionRepository txnRepo;
	@Mock private EntityManager entityManager;
	@Mock private PaymentService paymentService;
	@Mock private GarageBuyerResolver garageBuyerResolver;
	@Mock private SupplierRepository supplierRepository;
	@Mock private OrderItemRepository orderItemRepository;
	@Mock private MakeRepository makeRepository;
	@Mock private ModelRepository modelRepository;

	private GarageOrderService garageOrders;
	private SupplierOrderService shopOrders;
	private GarageOrderSync sync;

	private static final String USER = "77";
	private static final String SHOP_OWNER = "900";
	private static final GarageSnapshot SPEEDY = GarageSnapshot.builder().garageName("Speedy Garage")
			.garagePhone("0501234567").garageAddress("Plot 12, Al Quoz, Dubai").build();

	private InventoryPart pads;
	private InventoryPart filter;
	private Order saved;

	@BeforeEach
	void setUp() {
		OrderService orderService = new OrderService(cartRepo, orderRepo, partRepo, txnRepo, entityManager, paymentService, supplierRepository);
		sync = new GarageOrderSync(garageBuyerResolver, supplierRepository, orderItemRepository);
		garageOrders = new GarageOrderService(orderService, orderRepo, garageBuyerResolver, sync, makeRepository, modelRepository);
		shopOrders = new SupplierOrderService(supplierRepository, orderItemRepository, orderRepo, orderService, sync);

		pads = InventoryPart.builder().id(10).supplierId("5").partName("Brake pads").partNumber("BP-1").quantityInStock(5)
				.unitPrice(100.0).productCondition(ProductCondition.GENUINE).isActive(true).build();
		filter = InventoryPart.builder().id(20).supplierId("6").partName("Oil filter").quantityInStock(10)
				.unitPrice(20.0).productCondition(ProductCondition.AFTER_MARKET).isActive(true).build();
		when(partRepo.findById(10)).thenReturn(Optional.of(pads));
		when(partRepo.findById(20)).thenReturn(Optional.of(filter));
		when(garageBuyerResolver.requirePurchaser(USER, 3)).thenReturn(new GarageBuyer(3, 77, SPEEDY));
		when(garageBuyerResolver.itemContext(USER, 3, 11)).thenReturn(GaragePartContext.builder().garageId(3)
				.workOrderId(70).jobCardId(50).itemId(11).quantity(2).brand("Toyota").model("Camry").year("2019")
				.purchasable(true).build());
		when(makeRepository.findByName("Toyota")).thenReturn(Make.builder().id("mk-1").name("Toyota").build());
		when(modelRepository.findByMakeIdAndNameIn("mk-1", List.of("Camry")))
				.thenReturn(List.of(Model.builder().id("md-7").name("Camry").makeId("mk-1").build()));
		when(supplierRepository.findById(5)).thenReturn(Optional.of(
				Supplier.builder().id(5).name("Al Noor Parts").mobileNumber("0509999999").sparePartShopOwner(900).isActive(true).build()));
		when(supplierRepository.findById(6)).thenReturn(Optional.of(
				Supplier.builder().id(6).name("Other Shop").sparePartShopOwner(901).isActive(true).build()));
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

	private OrderResponse placeForJob() {
		return garageOrders.place(USER, 3, GarageOrderRequest.builder().items(List.of(
				GarageOrderRequest.Item.builder().inventoryPartId(10).partRequestItemId(11).build(),
				GarageOrderRequest.Item.builder().inventoryPartId(20).quantity(3).build())).build());
	}

	private MarketplaceStep lastStep(Stage stage) {
		ArgumentCaptor<MarketplaceStep> step = ArgumentCaptor.forClass(MarketplaceStep.class);
		verify(garageBuyerResolver, org.mockito.Mockito.atLeastOnce()).marketplace(anyString(), eq(11), step.capture());
		return step.getAllValues().stream().filter(s -> s.getStage() == stage).reduce((a, b) -> b).orElseThrow();
	}

	// ------------------------------------------------------------ placing

	@Test
	void garageOrderIsInTheGaragesNameWithCarAndJobAndMarksTheJobOrdered() {
		OrderResponse order = placeForJob();

		assertEquals(BuyerType.GARAGE, order.getBuyerType());
		assertNull(order.getVehicleOwnerId());
		assertEquals(3, order.getGarageId());
		assertEquals("Speedy Garage", order.getGarageName());
		assertEquals("Plot 12, Al Quoz, Dubai", order.getGarageAddress());
		assertEquals(Status.CONFIRMED, order.getStatus()); // COD by default
		assertEquals(260.0, order.getTotalAmount()); // 2 x 100 (job quantity) + 3 x 20
		assertEquals(3, pads.getQuantityInStock());

		OrderItemResponse jobLine = order.getItems().get(0);
		assertEquals(2, jobLine.getQuantity());
		assertEquals("Toyota", jobLine.getVehicleMakeName());
		assertEquals("md-7", jobLine.getVehicleModelId());
		assertEquals(2019, jobLine.getVehicleYear());
		assertEquals(70, jobLine.getWorkOrderId());
		assertEquals(50, jobLine.getJobCardId());
		assertEquals(FulfilmentStatus.PENDING, jobLine.getFulfilmentStatus());

		MarketplaceStep ordered = lastStep(Stage.ORDERED);
		assertEquals("Al Noor Parts", ordered.getSupplierName());
		assertEquals(42, ordered.getOrderId());
		assertEquals(77, saved.getPlacedByUserId());
	}

	@Test
	void jobThatTheCustomerHasntApprovedCantBeOrdered() {
		when(garageBuyerResolver.itemContext(USER, 3, 11)).thenReturn(GaragePartContext.builder().garageId(3).itemId(11)
				.purchasable(false).procurementStatus("PENDING_PROCUREMENT").partRequestStatus("PENDING_CUSTOMER_RESPONSE").build());
		assertThrows(ConflictException.class, this::placeForJob);
		verify(orderRepo, never()).save(any());
		assertEquals(5, pads.getQuantityInStock());
	}

	@Test
	void sameJobItemTwiceIsRejected() {
		assertThrows(ValidationException.class, () -> garageOrders.place(USER, 3, GarageOrderRequest.builder().items(List.of(
				GarageOrderRequest.Item.builder().inventoryPartId(10).partRequestItemId(11).build(),
				GarageOrderRequest.Item.builder().inventoryPartId(10).partRequestItemId(11).build())).build()));
	}

	@Test
	void withoutPermissionNothingHappens() {
		when(garageBuyerResolver.requirePurchaser("78", 3)).thenThrow(new ForbiddenException("no"));
		assertThrows(ForbiddenException.class, () -> garageOrders.place("78", 3, GarageOrderRequest.builder()
				.items(List.of(GarageOrderRequest.Item.builder().inventoryPartId(10).build())).build()));
		verify(orderRepo, never()).save(any());
	}

	@Test
	void garageApiDownKeepsTheOrderAndRetriesTheJobUpdateLater() {
		when(garageBuyerResolver.marketplace(anyString(), eq(11), any())).thenThrow(new UpstreamUnavailableException("down"));
		OrderResponse order = placeForJob();
		assertEquals("ORDERED", order.getItems().get(0).getPendingGarageSync());

		OrderItem jobLine = saved.getItems().get(0);
		when(orderItemRepository.findByPendingGarageSyncIsNotNull()).thenReturn(List.of(jobLine));
		org.mockito.Mockito.reset(garageBuyerResolver);
		assertEquals(1, sync.retryPending());
		assertNull(jobLine.getPendingGarageSync());
		verify(garageBuyerResolver).marketplace(eq("77"), eq(11), any());
	}

	@Test
	void garageApiRefusalIsRecordedNotRetried() {
		when(garageBuyerResolver.marketplace(anyString(), eq(11), any())).thenThrow(new ConflictException("Bought elsewhere"));
		OrderResponse order = placeForJob();
		assertNull(order.getItems().get(0).getPendingGarageSync());
		assertTrue(order.getItems().get(0).getGarageSyncError().contains("Bought elsewhere"));
	}

	// ------------------------------------------------------------ shop steps and receipt

	@Test
	void shopSeesOnlyItsLinesWithGarageAndCar() {
		placeForJob();
		when(orderItemRepository.findForSupplier("5")).thenReturn(List.of(saved.getItems().get(0)));

		List<SupplierOrderResponse> inbox = shopOrders.list(SHOP_OWNER, 5, BuyerType.GARAGE, null);

		assertEquals(1, inbox.size());
		SupplierOrderResponse seen = inbox.get(0);
		assertEquals("Speedy Garage", seen.getGarageName());
		assertEquals("0501234567", seen.getGaragePhone());
		assertEquals(1, seen.getItems().size());
		assertEquals("Camry", seen.getItems().get(0).getVehicleModelName());
		assertEquals(200.0, seen.getShopTotal());
		assertEquals(0, shopOrders.list(SHOP_OWNER, 5, BuyerType.CUSTOMER, null).size());
	}

	@Test
	void onlyTheShopOwnerAndOnlyTheirOrders() {
		placeForJob();
		assertThrows(ForbiddenException.class, () -> shopOrders.accept("901", 5, 42));
		when(supplierRepository.findById(7)).thenReturn(Optional.of(Supplier.builder().id(7).sparePartShopOwner(902).build()));
		assertThrows(ResourceNotFoundException.class, () -> shopOrders.accept("902", 7, 42));
	}

	@Test
	void dispatchThenGarageReceivesCreatesTheProcurement() {
		placeForJob();
		shopOrders.accept(SHOP_OWNER, 5, 42);
		shopOrders.dispatch(SHOP_OWNER, 5, 42);
		assertEquals(FulfilmentStatus.DISPATCHED, saved.getItems().get(0).getFulfilmentStatus());
		assertEquals(Status.CONFIRMED, saved.getStatus()); // the other shop's line is still pending

		shopOrders.dispatch("901", 6, 42);
		assertEquals(Status.SHIPPED, saved.getStatus());

		OrderResponse received = garageOrders.received(USER, 3, 42, null);
		assertEquals(Status.DELIVERED, received.getStatus());
		MarketplaceStep step = lastStep(Stage.RECEIVED);
		assertEquals(100.0, step.getUnitPrice());
		assertEquals(ProductCondition.GENUINE, step.getCondition());
		assertEquals("0509999999", step.getSupplierContact());
	}

	@Test
	void receivingBeforeTheShopAcceptsIsRefused() {
		placeForJob();
		assertThrows(ConflictException.class, () -> garageOrders.received(USER, 3, 42, null));
		assertThrows(ConflictException.class, () -> garageOrders.received(USER, 3, 42, List.of(1L)));
	}

	@Test
	void shopRejectReturnsStockLowersTheCodTotalAndFreesTheJob() {
		placeForJob();
		shopOrders.reject(SHOP_OWNER, 5, 42, "Out of stock");

		OrderItem jobLine = saved.getItems().get(0);
		assertEquals(FulfilmentStatus.REJECTED, jobLine.getFulfilmentStatus());
		assertEquals("Out of stock", jobLine.getRejectReason());
		assertEquals(5, pads.getQuantityInStock());
		assertEquals(60.0, saved.getTotalAmount());
		assertEquals(Status.CONFIRMED, saved.getStatus());
		lastStep(Stage.CANCELLED);

		shopOrders.reject("901", 6, 42, null);
		assertEquals(Status.CANCELLED, saved.getStatus());
		assertEquals(PaymentStatus.CANCELLED, saved.getPaymentStatus());
		assertEquals(10, filter.getQuantityInStock());

		// cancelling later doesn't put the rejected stock back a second time
		assertThrows(ConflictException.class, () -> garageOrders.cancel(USER, 3, 42));
		assertEquals(5, pads.getQuantityInStock());
	}

	@Test
	void rejectOnAPaidOrderRecordsARefund() {
		placeForJob();
		saved.setPaymentMethod(PaymentMethod.ONLINE);
		saved.setPaymentStatus(PaymentStatus.SUCCESS);
		shopOrders.reject(SHOP_OWNER, 5, 42, null);
		assertEquals(200.0, saved.getRefundAmount());
		assertEquals(260.0, saved.getTotalAmount());
	}

	@Test
	void unpaidOnlineOrdersCantBeHandledByShops() {
		placeForJob();
		saved.setStatus(Status.PENDING_PAYMENT);
		assertThrows(ConflictException.class, () -> shopOrders.accept(SHOP_OWNER, 5, 42));
	}

	@Test
	void garageCancelsBeforeDispatchAndJobsAreFreed() {
		placeForJob();
		OrderResponse cancelled = garageOrders.cancel(USER, 3, 42);
		assertEquals(Status.CANCELLED, cancelled.getStatus());
		assertEquals(5, pads.getQuantityInStock());
		lastStep(Stage.CANCELLED);
	}

	@Test
	void noCancellingOnceAShopHasDispatched() {
		placeForJob();
		shopOrders.dispatch(SHOP_OWNER, 5, 42);
		assertThrows(ConflictException.class, () -> garageOrders.cancel(USER, 3, 42));
	}

	@Test
	void garageOrdersAreConfirmedByTheGarageNotTheShop() {
		placeForJob();
		shopOrders.dispatch(SHOP_OWNER, 5, 42);
		assertThrows(ConflictException.class, () -> shopOrders.delivered(SHOP_OWNER, 5, 42));
	}

	@Test
	void anotherGaragesOrderLooksNotFound() {
		placeForJob();
		when(garageBuyerResolver.requirePurchaser(USER, 4)).thenReturn(new GarageBuyer(4, 77, SPEEDY));
		assertThrows(ResourceNotFoundException.class, () -> garageOrders.get(USER, 4, 42));
		assertThrows(ResourceNotFoundException.class, () -> garageOrders.received(USER, 4, 42, null));
	}

	@Test
	void customerOrdersDontShowUpAsTheGaragesAndViceVersa() {
		placeForJob();
		OrderService orderService = new OrderService(cartRepo, orderRepo, partRepo, txnRepo, entityManager, paymentService, supplierRepository);
		assertThrows(ResourceNotFoundException.class, () -> orderService.getOrder(42, 3)); // vehicle owner 3 != garage 3
		List<Order> none = new ArrayList<>();
		when(orderRepo.findByGarageIdOrderByOrderDateDesc(3)).thenReturn(none);
		assertEquals(0, garageOrders.list(USER, 3, null).size());
	}
}
