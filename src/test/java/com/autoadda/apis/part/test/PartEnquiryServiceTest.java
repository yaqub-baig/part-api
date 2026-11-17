package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import com.autoadda.apis.part.entity.Category;
import com.autoadda.apis.part.entity.EnquiryRecipient;
import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.PartEnquiry;
import com.autoadda.apis.part.entity.PartQuote;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ForbiddenException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
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
import com.autoadda.apis.part.request.PartQuoteRequest;
import com.autoadda.apis.part.response.AcceptQuoteResponse;
import com.autoadda.apis.part.response.PartEnquiryResponse;
import com.autoadda.apis.part.service.CategoryHierarchy;
import com.autoadda.apis.part.service.PartEnquiryService;
import com.autoadda.apis.part.util.EnquiryRecipientStatus;
import com.autoadda.apis.part.util.EnquiryStatus;
import com.autoadda.apis.part.util.ProductCondition;
import com.autoadda.apis.part.util.QuoteStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartEnquiryServiceTest {

	@Mock
	private PartEnquiryRepository enquiryRepository;
	@Mock
	private EnquiryRecipientRepository recipientRepository;
	@Mock
	private PartQuoteRepository quoteRepository;
	@Mock
	private SupplierRepository supplierRepository;
	@Mock
	private MakeRepository makeRepository;
	@Mock
	private ModelRepository modelRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private MediaFileRepository mediaFileRepository;
	@Mock
	private InventoryPartRepository inventoryPartRepository;
	@Mock
	private CategoryHierarchy categoryHierarchy;
	@InjectMocks
	private PartEnquiryService service;

	private static final Integer OWNER = 42;
	private static final String CUSTOMER = "42";
	private static final String SHOP_OWNER = "900";
	private Supplier shop;

	@BeforeEach
	void setUp() {
		shop = Supplier.builder().id(5).name("Shop A").sparePartShopOwner(900).isActive(true).build();
		when(supplierRepository.findById(5)).thenReturn(Optional.of(shop));
		when(makeRepository.existsById("mk-1")).thenReturn(true);
		when(productRepository.existsById("p1")).thenReturn(true);
		when(enquiryRepository.save(any(PartEnquiry.class))).thenAnswer(inv -> {
			PartEnquiry e = inv.getArgument(0);
			if (e.getId() == null) {
				e.setId(100);
			}
			return e;
		});
		when(quoteRepository.save(any(PartQuote.class))).thenAnswer(inv -> {
			PartQuote q = inv.getArgument(0);
			if (q.getId() == null) {
				q.setId(7);
			}
			return q;
		});
	}

	private static PartEnquiry enquiry(EnquiryStatus status, LocalDateTime expiresAt) {
		return PartEnquiry.builder().id(100).vehicleOwnerId(OWNER).makeId("mk-1").status(status)
				.conditions(new HashSet<>()).imageIds(new HashSet<>()).createdAt(LocalDateTime.now().minusHours(1))
				.expiresAt(expiresAt).build();
	}

	private static EnquiryRecipient recipient(EnquiryRecipientStatus status) {
		return EnquiryRecipient.builder().id(1).enquiryId(100).supplierId(5).status(status).build();
	}

	private static PartQuote quote(int id, int supplierId, double price, QuoteStatus status, LocalDateTime validUntil) {
		return PartQuote.builder().id(id).enquiryId(100).supplierId(supplierId).productId("p1").unitPrice(price)
				.condition(ProductCondition.USED).quantity(2).status(status).validUntil(validUntil).build();
	}

	private static PartQuoteRequest quoteRequest(ProductCondition condition) {
		return PartQuoteRequest.builder().productId("p1").unitPrice(150.0).condition(condition).quantity(1).build();
	}

	// ------------------------------------------------------------ create

	@Test
	@SuppressWarnings("unchecked")
	void createSendsTheEnquiryToMatchingShops() {
		Supplier other = Supplier.builder().id(6).isActive(true).build();
		when(supplierRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(shop, other)));
		when(categoryRepository.existsById("cat-9")).thenReturn(true);
		when(categoryHierarchy.withAncestors("cat-9")).thenReturn(Set.of("cat-9", "cat-1"));

		PartEnquiryResponse response = service.create(CUSTOMER, OWNER, PartEnquiryRequest.builder().makeId("mk-1")
				.categoryId("cat-9").description("Left headlight").conditions(List.of(ProductCondition.USED)).build());

		assertEquals(EnquiryStatus.OPEN, response.getStatus());
		assertEquals(2, response.getRecipientCount());
		assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusHours(47)));
		ArgumentCaptor<List<EnquiryRecipient>> recipients = ArgumentCaptor.forClass(List.class);
		verify(recipientRepository).saveAll(recipients.capture());
		assertEquals(List.of(5, 6), recipients.getValue().stream().map(EnquiryRecipient::getSupplierId).toList());
		assertTrue(recipients.getValue().stream().allMatch(r -> r.getStatus() == EnquiryRecipientStatus.NEW));
		verify(categoryHierarchy).withAncestors("cat-9"); // a shop covering the parent category matches
	}

	@Test
	@SuppressWarnings("unchecked")
	void createWithNoMatchingShopIsRejectedAndNothingSaved() {
		when(supplierRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
		assertThrows(ConflictException.class,
				() -> service.create(CUSTOMER, OWNER, PartEnquiryRequest.builder().makeId("mk-1").partNumber("123").build()));
		verify(enquiryRepository, never()).save(any());
	}

	@Test
	void createNeedsAPartDescriptionAndValidVehicle() {
		assertThrows(ValidationException.class,
				() -> service.create(CUSTOMER, OWNER, PartEnquiryRequest.builder().makeId("mk-1").build()));
		assertThrows(ValidationException.class,
				() -> service.create(CUSTOMER, OWNER, PartEnquiryRequest.builder().description("x").build()));
		when(modelRepository.findById("md-5")).thenReturn(Optional.of(Model.builder().id("md-5").name("Patrol").makeId("mk-2").build()));
		assertThrows(ValidationException.class, () -> service.create(CUSTOMER, OWNER,
				PartEnquiryRequest.builder().makeId("mk-1").modelId("md-5").description("x").build()));
		assertThrows(ValidationException.class,
				() -> service.create(CUSTOMER, OWNER, PartEnquiryRequest.builder().makeId("mk-1").year(1900).description("x").build()));
	}

	@Test
	void createIsLimitedToFiveOpenEnquiries() {
		when(enquiryRepository.countByVehicleOwnerIdAndStatusIn(any(), any())).thenReturn(5L);
		assertThrows(ConflictException.class,
				() -> service.create(CUSTOMER, OWNER, PartEnquiryRequest.builder().makeId("mk-1").description("x").build()));
	}

	@Test
	void customerCallsRequireTheLoggedInUserToBeThatCustomer() {
		assertThrows(ForbiddenException.class,
				() -> service.create("43", OWNER, PartEnquiryRequest.builder().makeId("mk-1").description("x").build()));
		assertThrows(ForbiddenException.class, () -> service.listForOwner("43", OWNER, null));
		assertThrows(ForbiddenException.class, () -> service.getForOwner("43", OWNER, 100));
		assertThrows(ForbiddenException.class, () -> service.cancel("43", OWNER, 100));
		assertThrows(ForbiddenException.class, () -> service.accept("43", OWNER, 100, 7));
		assertThrows(ForbiddenException.class, () -> service.cancel("abc", OWNER, 100));
		verify(enquiryRepository, never()).findById(any());
		verify(enquiryRepository, never()).findByIdForUpdate(any());
	}

	// ------------------------------------------------------------ quote

	@Test
	void firstQuoteMovesEnquiryAndRecipientToQuoted() {
		PartEnquiry enquiry = enquiry(EnquiryStatus.OPEN, LocalDateTime.now().plusHours(10));
		EnquiryRecipient recipient = recipient(EnquiryRecipientStatus.VIEWED);
		when(recipientRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.of(recipient));
		when(enquiryRepository.findByIdForUpdate(100)).thenReturn(Optional.of(enquiry));
		when(quoteRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.empty());

		PartEnquiryResponse response = service.quote(SHOP_OWNER, 5, 100, quoteRequest(ProductCondition.USED));

		assertEquals(EnquiryStatus.QUOTED, enquiry.getStatus());
		assertEquals(EnquiryRecipientStatus.QUOTED, recipient.getStatus());
		assertEquals(150.0, response.getMyQuote().getUnitPrice());
		assertEquals(QuoteStatus.SENT, response.getMyQuote().getStatus());
		assertNull(response.getVehicleOwnerId()); // customer stays private until a quote is accepted
	}

	@Test
	void quotingAgainReplacesTheShopsQuote() {
		PartEnquiry enquiry = enquiry(EnquiryStatus.QUOTED, LocalDateTime.now().plusHours(10));
		PartQuote existing = quote(7, 5, 200.0, QuoteStatus.SENT, LocalDateTime.now().plusHours(5));
		when(recipientRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.of(recipient(EnquiryRecipientStatus.QUOTED)));
		when(enquiryRepository.findByIdForUpdate(100)).thenReturn(Optional.of(enquiry));
		when(quoteRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.of(existing));

		service.quote(SHOP_OWNER, 5, 100, quoteRequest(ProductCondition.GENUINE));

		assertEquals(150.0, existing.getUnitPrice());
		assertEquals(ProductCondition.GENUINE, existing.getCondition());
		assertTrue(existing.getValidUntil().isAfter(LocalDateTime.now().plusHours(71)));
	}

	@Test
	void quoteRulesAreEnforced() {
		// not the shop's owner
		assertThrows(ForbiddenException.class, () -> service.quote("901", 5, 100, quoteRequest(ProductCondition.USED)));

		// shop didn't receive the enquiry
		when(recipientRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.quote(SHOP_OWNER, 5, 100, quoteRequest(ProductCondition.USED)));

		// past the deadline
		when(recipientRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.of(recipient(EnquiryRecipientStatus.NEW)));
		when(enquiryRepository.findByIdForUpdate(100)).thenReturn(Optional.of(enquiry(EnquiryStatus.OPEN, LocalDateTime.now().minusMinutes(1))));
		assertThrows(ConflictException.class, () -> service.quote(SHOP_OWNER, 5, 100, quoteRequest(ProductCondition.USED)));

		// condition the customer didn't ask for
		PartEnquiry genuineOnly = enquiry(EnquiryStatus.OPEN, LocalDateTime.now().plusHours(1));
		genuineOnly.getConditions().add(ProductCondition.GENUINE);
		when(enquiryRepository.findByIdForUpdate(100)).thenReturn(Optional.of(genuineOnly));
		assertThrows(ValidationException.class, () -> service.quote(SHOP_OWNER, 5, 100, quoteRequest(ProductCondition.USED)));
	}

	@Test
	void declineAfterQuotingIsRejected() {
		when(recipientRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.of(recipient(EnquiryRecipientStatus.QUOTED)));
		when(quoteRepository.findByEnquiryIdAndSupplierId(100, 5))
				.thenReturn(Optional.of(quote(7, 5, 100, QuoteStatus.SENT, LocalDateTime.now().plusHours(1))));
		assertThrows(ConflictException.class, () -> service.decline(SHOP_OWNER, 5, 100));
	}

	// ------------------------------------------------------------ accept

	@Test
	void acceptCreatesAListingAtTheQuotedPriceAndClosesTheEnquiry() {
		PartEnquiry enquiry = enquiry(EnquiryStatus.QUOTED, LocalDateTime.now().minusHours(1)); // deadline passed: accepting still fine
		PartQuote chosen = quote(7, 5, 150.0, QuoteStatus.SENT, LocalDateTime.now().plusHours(5));
		PartQuote other = quote(8, 6, 120.0, QuoteStatus.SENT, LocalDateTime.now().plusHours(5));
		when(enquiryRepository.findByIdForUpdate(100)).thenReturn(Optional.of(enquiry));
		when(quoteRepository.findByEnquiryIdOrderByUnitPriceAsc(100)).thenReturn(new ArrayList<>(List.of(other, chosen)));
		when(productRepository.findById("p1")).thenReturn(Optional.of(Product.builder().id("p1").name("Headlight").partNumber("HL-1").build()));
		when(inventoryPartRepository.save(any(InventoryPart.class))).thenAnswer(inv -> {
			InventoryPart part = inv.getArgument(0);
			part.setId(555);
			return part;
		});

		AcceptQuoteResponse response = service.accept(CUSTOMER, OWNER, 100, 7);

		assertEquals(555, response.getInventoryPartId());
		ArgumentCaptor<InventoryPart> listing = ArgumentCaptor.forClass(InventoryPart.class);
		verify(inventoryPartRepository).save(listing.capture());
		assertEquals("5", listing.getValue().getSupplierId());
		assertEquals(150.0, listing.getValue().getUnitPrice());
		assertEquals(2, listing.getValue().getQuantityInStock());
		assertEquals(ProductCondition.USED, listing.getValue().getProductCondition());
		assertTrue(listing.getValue().getIsActive());
		assertEquals(QuoteStatus.ACCEPTED, chosen.getStatus());
		assertEquals(QuoteStatus.REJECTED, other.getStatus());
		assertEquals(EnquiryStatus.ACCEPTED, enquiry.getStatus());
		assertEquals(7, enquiry.getAcceptedQuoteId());
	}

	@Test
	void acceptRules() {
		// someone else's enquiry looks like it doesn't exist
		when(enquiryRepository.findByIdForUpdate(100)).thenReturn(Optional.of(enquiry(EnquiryStatus.QUOTED, LocalDateTime.now())));
		assertThrows(ResourceNotFoundException.class, () -> service.accept("99", 99, 100, 7));

		// expired quote
		when(quoteRepository.findByEnquiryIdOrderByUnitPriceAsc(100))
				.thenReturn(List.of(quote(7, 5, 150.0, QuoteStatus.SENT, LocalDateTime.now().minusMinutes(1))));
		assertThrows(ConflictException.class, () -> service.accept(CUSTOMER, OWNER, 100, 7));

		// already accepted
		when(enquiryRepository.findByIdForUpdate(100)).thenReturn(Optional.of(enquiry(EnquiryStatus.ACCEPTED, LocalDateTime.now())));
		assertThrows(ConflictException.class, () -> service.accept(CUSTOMER, OWNER, 100, 7));
		verify(inventoryPartRepository, never()).save(any());
	}

	@Test
	void chosenShopSeesTheCustomerAfterAccept() {
		PartEnquiry enquiry = enquiry(EnquiryStatus.ACCEPTED, LocalDateTime.now());
		when(recipientRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.of(recipient(EnquiryRecipientStatus.QUOTED)));
		when(enquiryRepository.findById(100)).thenReturn(Optional.of(enquiry));
		when(quoteRepository.findByEnquiryIdAndSupplierId(100, 5))
				.thenReturn(Optional.of(quote(7, 5, 150.0, QuoteStatus.ACCEPTED, LocalDateTime.now())));

		assertEquals(OWNER, service.getForShop(SHOP_OWNER, 5, 100).getVehicleOwnerId());
	}

	@Test
	void viewingMarksANewEnquiryViewed() {
		EnquiryRecipient recipient = recipient(EnquiryRecipientStatus.NEW);
		when(recipientRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.of(recipient));
		when(enquiryRepository.findById(100)).thenReturn(Optional.of(enquiry(EnquiryStatus.OPEN, LocalDateTime.now().plusHours(1))));
		when(quoteRepository.findByEnquiryIdAndSupplierId(100, 5)).thenReturn(Optional.empty());

		service.getForShop(SHOP_OWNER, 5, 100);
		assertEquals(EnquiryRecipientStatus.VIEWED, recipient.getStatus());
	}

	// ------------------------------------------------------------ expiry

	@Test
	void expiryClosesOpenOnesAndKeepsQuotedOnesWithAValidQuote() {
		PartEnquiry open = enquiry(EnquiryStatus.OPEN, LocalDateTime.now().minusHours(1));
		PartEnquiry quotedValid = enquiry(EnquiryStatus.QUOTED, LocalDateTime.now().minusHours(1));
		quotedValid.setId(101);
		PartEnquiry quotedStale = enquiry(EnquiryStatus.QUOTED, LocalDateTime.now().minusHours(1));
		quotedStale.setId(102);
		when(enquiryRepository.findByStatusInAndExpiresAtBefore(any(), any())).thenReturn(List.of(open, quotedValid, quotedStale));
		when(quoteRepository.existsByEnquiryIdAndStatusAndValidUntilAfter(anyInt(), any(), any())).thenReturn(false);
		when(quoteRepository.existsByEnquiryIdAndStatusAndValidUntilAfter(org.mockito.ArgumentMatchers.eq(101), any(), any())).thenReturn(true);

		assertEquals(2, service.expireEnquiries());
		assertEquals(EnquiryStatus.EXPIRED, open.getStatus());
		assertEquals(EnquiryStatus.QUOTED, quotedValid.getStatus());
		assertEquals(EnquiryStatus.EXPIRED, quotedStale.getStatus());
	}

	// ------------------------------------------------------------ category ancestors

	@Test
	void ancestorsWalkUpTheTreeAndStopOnCycles() {
		CategoryRepository repo = mock(CategoryRepository.class);
		when(repo.findById("c3")).thenReturn(Optional.of(Category.builder().id("c3").parentCategoryId("c2").build()));
		when(repo.findById("c2")).thenReturn(Optional.of(Category.builder().id("c2").parentCategoryId("c1").build()));
		when(repo.findById("c1")).thenReturn(Optional.of(Category.builder().id("c1").parentCategoryId("c3").build()));

		assertEquals(Set.of("c3", "c2", "c1"), new CategoryHierarchy(repo).withAncestors("c3"));
	}

	@Test
	void countNeedsAMake() {
		assertThrows(ValidationException.class, () -> service.countMatchingShops(" ", null, null, null, List.of()));
		verify(supplierRepository, never()).count(any(Specification.class));
	}
}
