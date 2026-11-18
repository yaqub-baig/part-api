package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

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

import com.autoadda.apis.part.entity.EnquiryRecipient;
import com.autoadda.apis.part.entity.GarageSnapshot;
import com.autoadda.apis.part.entity.Location;
import com.autoadda.apis.part.entity.PartEnquiry;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ValidationException;
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
import com.autoadda.apis.part.util.Status;

/** Customers and garages can send an enquiry to shops they choose instead of the matched ones. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnquiryChosenShopsTest {

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

	private static Supplier shop(int id, boolean active, Status status) {
		return Supplier.builder().id(id).name("Shop " + id).isActive(active).accountStatus(status).build();
	}

	@BeforeEach
	void setUp() {
		when(makeRepository.existsById("mk-1")).thenReturn(true);
		when(enquiryRepository.save(any(PartEnquiry.class))).thenAnswer(inv -> {
			PartEnquiry e = inv.getArgument(0);
			e.setId(100);
			return e;
		});
		when(garageBuyerResolver.requirePurchaser("77", 3))
				.thenReturn(new GarageBuyer(3, 77, GarageSnapshot.builder().garageName("Speedy Garage").build()));
	}

	private static PartEnquiryRequest request(List<Integer> supplierIds) {
		return PartEnquiryRequest.builder().makeId("mk-1").description("Left headlight").supplierIds(supplierIds).build();
	}

	@Test
	@SuppressWarnings("unchecked")
	void customerSendsOnlyToTheChosenShops() {
		when(supplierRepository.findAllById(any())).thenReturn(List.of(shop(5, true, null), shop(9, true, Status.PENDING)));

		PartEnquiryResponse response = service.create("42", 42, request(List.of(9, 5, 9)));

		assertTrue(response.getShopsChosen());
		assertEquals(2, response.getRecipientCount());
		ArgumentCaptor<List<EnquiryRecipient>> recipients = ArgumentCaptor.forClass(List.class);
		verify(recipientRepository).saveAll(recipients.capture());
		assertEquals(List.of(9, 5), recipients.getValue().stream().map(EnquiryRecipient::getSupplierId).toList());
		verify(supplierRepository, never()).findAll(any(Specification.class), any(Pageable.class));
	}

	@Test
	void garageCanChooseShopsToo() {
		when(supplierRepository.findAllById(any())).thenReturn(List.of(shop(5, true, null)));
		PartEnquiryResponse response = service.createForGarage("77", 3, request(List.of(5)));
		assertTrue(response.getShopsChosen());
		assertEquals(1, response.getRecipientCount());
	}

	@Test
	@SuppressWarnings("unchecked")
	void noChoiceMeansMatchedShops() {
		when(supplierRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(shop(5, true, null))));
		PartEnquiryResponse response = service.create("42", 42, request(List.of()));
		assertFalse(response.getShopsChosen());
		verify(supplierRepository, never()).findAllById(any());
	}

	@Test
	void unknownBlockedOrTooManyShopsAreRejected() {
		when(supplierRepository.findAllById(any())).thenReturn(List.of(shop(5, true, null)));
		assertThrows(ValidationException.class, () -> service.create("42", 42, request(List.of(5, 404))));

		when(supplierRepository.findAllById(any())).thenReturn(List.of(shop(6, false, null)));
		assertThrows(ValidationException.class, () -> service.create("42", 42, request(List.of(6))));

		when(supplierRepository.findAllById(any())).thenReturn(List.of(shop(7, true, Status.BLOCKED)));
		assertThrows(ValidationException.class, () -> service.create("42", 42, request(List.of(7))));

		List<Integer> many = IntStream.rangeClosed(1, 21).boxed().toList();
		assertThrows(ValidationException.class, () -> service.create("42", 42, request(many)));
		verify(enquiryRepository, never()).save(any());
	}

	@Test
	@SuppressWarnings("unchecked")
	void matchingShopsListsShopsToChooseFrom() {
		Supplier shop = shop(5, true, null);
		shop.setSupplierBrandName("Al Noor");
		shop.setLocation(Location.builder().area("Al Quoz").city("Dubai").build());
		when(supplierRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(shop)));

		List<Map<String, Object>> shops = service.matchingShops("mk-1", null, 2019, null, List.of());

		assertEquals(1, shops.size());
		assertEquals(5, shops.get(0).get("supplierId"));
		assertEquals("Al Noor", shops.get(0).get("brandName"));
		assertEquals("Al Quoz", shops.get(0).get("area"));
	}
}
