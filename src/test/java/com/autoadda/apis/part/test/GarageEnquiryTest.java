package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.autoadda.apis.part.clients.GarageClient;
import com.autoadda.apis.part.clients.garage.GarageApiResponse;
import com.autoadda.apis.part.clients.garage.GarageBuyerInfo;
import com.autoadda.apis.part.clients.garage.GaragePartContext;
import com.autoadda.apis.part.entity.EnquiryRecipient;
import com.autoadda.apis.part.entity.GarageSnapshot;
import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.PartEnquiry;
import com.autoadda.apis.part.entity.PartQuote;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ForbiddenException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.UpstreamUnavailableException;
import com.autoadda.apis.part.repository.CategoryRepository;
import com.autoadda.apis.part.repository.EnquiryRecipientRepository;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.repository.PartEnquiryRepository;
import com.autoadda.apis.part.repository.PartQuoteRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.request.PartEnquiryRequest;
import com.autoadda.apis.part.response.PartEnquiryResponse;
import com.autoadda.apis.part.service.CategoryHierarchy;
import com.autoadda.apis.part.service.GarageBuyerResolver;
import com.autoadda.apis.part.service.GarageBuyerResolver.GarageBuyer;
import com.autoadda.apis.part.service.PartEnquiryService;
import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.EnquiryRecipientStatus;
import com.autoadda.apis.part.util.EnquiryStatus;
import com.autoadda.apis.part.util.ProductCondition;
import com.autoadda.apis.part.util.QuoteStatus;

import feign.FeignException;
import feign.Request;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GarageEnquiryTest {

	@Mock private PartEnquiryRepository enquiryRepository;
	@Mock private EnquiryRecipientRepository recipientRepository;
	@Mock private PartQuoteRepository quoteRepository;
	@Mock private SupplierRepository supplierRepository;
	@Mock private MakeRepository makeRepository;
	@Mock private ModelRepository modelRepository;
	@Mock private CategoryRepository categoryRepository;
	@Mock private ProductRepository productRepository;
	@Mock private MediaFileRepository mediaFileRepository;
	@Mock private InventoryPartRepository inventoryPartRepository;
	@Mock private CategoryHierarchy categoryHierarchy;
	@Mock private GarageBuyerResolver garageBuyerResolver;
	@InjectMocks private PartEnquiryService service;

	private static final String USER = "77";
	private static final GarageSnapshot SPEEDY = GarageSnapshot.builder().garageName("Speedy Garage")
			.garagePhone("0501234567").garageAddress("Plot 12, Al Quoz, Dubai").garageMapLink("https://maps/x").build();

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		when(garageBuyerResolver.requirePurchaser(USER, 3)).thenReturn(new GarageBuyer(3, 77, SPEEDY));
		when(makeRepository.existsById("mk-1")).thenReturn(true);
		when(makeRepository.findByName("Toyota")).thenReturn(Make.builder().id("mk-1").name("Toyota").build());
		when(modelRepository.findByMakeIdAndNameIn("mk-1", List.of("Camry")))
				.thenReturn(List.of(Model.builder().id("md-7").name("Camry").makeId("mk-1").build()));
		when(modelRepository.findById("md-7")).thenReturn(Optional.of(Model.builder().id("md-7").name("Camry").makeId("mk-1").build()));
		when(supplierRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(Supplier.builder().id(5).isActive(true).build())));
		when(enquiryRepository.save(any(PartEnquiry.class))).thenAnswer(inv -> {
			PartEnquiry e = inv.getArgument(0);
			if (e.getId() == null) {
				e.setId(100);
			}
			return e;
		});
	}

	private static GaragePartContext job() {
		return GaragePartContext.builder().garageId(3).workOrderId(70).jobCardId(50).itemId(11)
				.partName("Brake pads").partNumber("BP-1").quantity(2).brand("Toyota").model("Camry").year("2019")
				.purchasable(true).build();
	}

	@Test
	void garageEnquiryFromAJobIsInTheGaragesNameWithTheCar() {
		when(garageBuyerResolver.itemContext(USER, 3, 11)).thenReturn(job());

		PartEnquiryResponse response = service.createForGarage(USER, 3, PartEnquiryRequest.builder().partRequestItemId(11).build());

		ArgumentCaptor<PartEnquiry> saved = ArgumentCaptor.forClass(PartEnquiry.class);
		verify(enquiryRepository).save(saved.capture());
		PartEnquiry e = saved.getValue();
		assertEquals(BuyerType.GARAGE, e.getBuyerType());
		assertEquals(3, e.getGarageId());
		assertNull(e.getVehicleOwnerId());
		assertEquals(77, e.getPlacedByUserId());
		assertEquals("Speedy Garage", e.getGarage().getGarageName());
		assertEquals("mk-1", e.getMakeId());
		assertEquals("md-7", e.getModelId());
		assertEquals(2019, e.getYear());
		assertEquals("BP-1", e.getPartNumber());
		assertEquals("Brake pads x2", e.getDescription());
		assertEquals(70, e.getWorkOrderId());
		assertEquals(50, e.getJobCardId());
		assertEquals(11, e.getPartRequestItemId());
		assertEquals(BuyerType.GARAGE, response.getBuyerType());
		assertEquals("0501234567", response.getGaragePhone()); // the garage sees its own details
	}

	@Test
	void standaloneGarageEnquiryNeedsAMakeLikeAnyOther() {
		PartEnquiryResponse response = service.createForGarage(USER, 3,
				PartEnquiryRequest.builder().makeId("mk-1").description("Oil filters x10").build());
		assertEquals(BuyerType.GARAGE, response.getBuyerType());
		assertNull(response.getJobCardId());
	}

	@Test
	void unknownBrandOnTheJobAsksForTheMake() {
		GaragePartContext job = job();
		job.setBrand("Tesla");
		when(garageBuyerResolver.itemContext(USER, 3, 11)).thenReturn(job);
		assertThrows(com.autoadda.apis.part.exception.ValidationException.class,
				() -> service.createForGarage(USER, 3, PartEnquiryRequest.builder().partRequestItemId(11).build()));
	}

	@Test
	void withoutPermissionNothingIsSent() {
		when(garageBuyerResolver.requirePurchaser("78", 3)).thenThrow(new ForbiddenException("no"));
		assertThrows(ForbiddenException.class, () -> service.createForGarage("78", 3,
				PartEnquiryRequest.builder().makeId("mk-1").description("x").build()));
		verify(enquiryRepository, never()).save(any());
	}

	@Test
	void garageLimitIsTwenty() {
		when(enquiryRepository.countByGarageIdAndStatusIn(eq(3), any())).thenReturn(20L);
		assertThrows(ConflictException.class, () -> service.createForGarage(USER, 3,
				PartEnquiryRequest.builder().makeId("mk-1").description("x").build()));
	}

	@Test
	void customersCannotUseJobItems() {
		assertThrows(com.autoadda.apis.part.exception.ValidationException.class, () -> service.create("42", 42,
				PartEnquiryRequest.builder().makeId("mk-1").description("x").partRequestItemId(11).build()));
	}

	@Test
	void anotherGaragesEnquiryLooksNotFoundAndCustomersDontSeeGarageOnes() {
		PartEnquiry garageEnquiry = PartEnquiry.builder().id(100).buyerType(BuyerType.GARAGE).garageId(4)
				.status(EnquiryStatus.OPEN).build();
		when(enquiryRepository.findById(100)).thenReturn(Optional.of(garageEnquiry));
		assertThrows(ResourceNotFoundException.class, () -> service.getForGarage(USER, 3, 100));

		PartEnquiry noOwner = PartEnquiry.builder().id(100).buyerType(BuyerType.GARAGE).garageId(3).build();
		when(enquiryRepository.findById(100)).thenReturn(Optional.of(noOwner));
		assertThrows(ResourceNotFoundException.class, () -> service.getForOwner("42", 42, 100));
	}

	@Test
	void shopSeesGarageAndCarAndGetsContactOnlyAfterAccept() {
		Supplier shop = Supplier.builder().id(5).sparePartShopOwner(900).isActive(true).build();
		when(supplierRepository.findById(5)).thenReturn(Optional.of(shop));
		PartEnquiry garageEnquiry = PartEnquiry.builder().id(100).buyerType(BuyerType.GARAGE).garageId(3).garage(SPEEDY)
				.makeId("mk-1").modelId("md-7").year(2019).workOrderId(70).jobCardId(50).status(EnquiryStatus.QUOTED)
				.conditions(new HashSet<>()).imageIds(new HashSet<>()).createdAt(LocalDateTime.now())
				.expiresAt(LocalDateTime.now().plusHours(5)).build();
		PartEnquiry customerEnquiry = PartEnquiry.builder().id(101).vehicleOwnerId(42).makeId("mk-1")
				.status(EnquiryStatus.OPEN).conditions(new HashSet<>()).imageIds(new HashSet<>()).build();
		when(recipientRepository.findBySupplierIdOrderByCreatedAtDesc(5)).thenReturn(List.of(
				EnquiryRecipient.builder().enquiryId(100).supplierId(5).status(EnquiryRecipientStatus.QUOTED).build(),
				EnquiryRecipient.builder().enquiryId(101).supplierId(5).status(EnquiryRecipientStatus.NEW).build()));
		when(enquiryRepository.findAllById(any())).thenReturn(List.of(garageEnquiry, customerEnquiry));
		PartQuote myQuote = PartQuote.builder().id(7).enquiryId(100).supplierId(5).productId("p1").unitPrice(100.0)
				.condition(ProductCondition.USED).quantity(1).status(QuoteStatus.SENT).validUntil(LocalDateTime.now().plusHours(3)).build();
		when(quoteRepository.findByEnquiryIdIn(any())).thenReturn(List.of(myQuote));
		when(makeRepository.findAllById(any())).thenReturn(List.of(Make.builder().id("mk-1").name("Toyota").build()));
		when(modelRepository.findAllById(any())).thenReturn(List.of(Model.builder().id("md-7").name("Camry").build()));

		List<PartEnquiryResponse> garageOnly = service.listForShop("900", 5, null, BuyerType.GARAGE);
		assertEquals(1, garageOnly.size());
		PartEnquiryResponse seen = garageOnly.get(0);
		assertEquals("Speedy Garage", seen.getGarageName());
		assertEquals("Toyota", seen.getMakeName());
		assertEquals("Camry", seen.getModelName());
		assertEquals(2019, seen.getYear());
		assertEquals(70, seen.getWorkOrderId());
		assertNull(seen.getGaragePhone()); // not before the garage accepts this shop's quote

		myQuote.setStatus(QuoteStatus.ACCEPTED);
		PartEnquiryResponse afterAccept = service.listForShop("900", 5, null, BuyerType.GARAGE).get(0);
		assertEquals("0501234567", afterAccept.getGaragePhone());
		assertEquals("Plot 12, Al Quoz, Dubai", afterAccept.getGarageAddress());

		assertEquals(1, service.listForShop("900", 5, null, BuyerType.CUSTOMER).size());
		assertEquals(2, service.listForShop("900", 5, null, null).size());
	}

	// ------------------------------------------------------------ garage-api calls

	private static FeignException feign(int status, String body) {
		Request request = Request.create(Request.HttpMethod.GET, "http://garage/x", Map.of(), null, StandardCharsets.UTF_8, null);
		return FeignException.errorStatus("call", feign.Response.builder().status(status).reason("x").request(request)
				.headers(Map.of()).body(body, StandardCharsets.UTF_8).build());
	}

	@Test
	void resolverMapsGarageApiAnswers() {
		GarageClient client = mock(GarageClient.class);
		GarageBuyerResolver resolver = new GarageBuyerResolver(client);
		when(client.purchasingAccess(3, USER)).thenReturn(GarageApiResponse.<GarageBuyerInfo>builder()
				.body(GarageBuyerInfo.builder().garageId(3).name("Speedy Garage").address("Al Quoz").build()).build());
		GarageBuyer buyer = resolver.requirePurchaser(USER, 3);
		assertEquals(77, buyer.userId());
		assertEquals("Speedy Garage", buyer.snapshot().getGarageName());

		when(client.purchasingAccess(3, "78")).thenThrow(feign(403, "{\"message\":\"Forbidden\"}"));
		assertThrows(ForbiddenException.class, () -> resolver.requirePurchaser("78", 3));

		when(client.purchasingAccess(4, USER)).thenThrow(feign(409, "{\"message\":\"Only approved, active garages can buy parts\"}"));
		ConflictException conflict = assertThrows(ConflictException.class, () -> resolver.requirePurchaser(USER, 4));
		assertEquals("Only approved, active garages can buy parts", conflict.getMessage());

		when(client.purchasingAccess(5, USER)).thenThrow(feign(500, ""));
		assertThrows(UpstreamUnavailableException.class, () -> resolver.requirePurchaser(USER, 5));
	}

	@Test
	void itemOfAnotherGarageIsNotFound() {
		GarageClient client = mock(GarageClient.class);
		GarageBuyerResolver resolver = new GarageBuyerResolver(client);
		GaragePartContext other = job();
		other.setGarageId(9);
		when(client.purchaseContext(eq(11), anyString())).thenReturn(GarageApiResponse.<GaragePartContext>builder().body(other).build());
		assertThrows(ResourceNotFoundException.class, () -> resolver.itemContext(USER, 3, 11));
		assertTrue(resolver.itemContext(USER, 9, 11).isPurchasable());
	}
}
