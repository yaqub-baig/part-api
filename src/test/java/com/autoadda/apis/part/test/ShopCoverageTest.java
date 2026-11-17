package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.entity.Category;
import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.VehicleFitException;
import com.autoadda.apis.part.repository.CategoryRepository;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.request.SupplierRequest;
import com.autoadda.apis.part.response.ShopCoverageBackfillResponse;
import com.autoadda.apis.part.service.ShopCoverageBackfillService;
import com.autoadda.apis.part.service.ShopCoverageResolver;
import com.autoadda.apis.part.service.ShopCoverageResolver.ShopCoverage;
import com.autoadda.apis.part.util.ProductCondition;

@ExtendWith(MockitoExtension.class)
class ShopCoverageTest {

	@Mock
	private MakeRepository makeRepository;
	@Mock
	private ModelRepository modelRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@InjectMocks
	private ShopCoverageResolver resolver;

	private static final Make TOYOTA = Make.builder().id("mk-1").name("Toyota").build();
	private static final Make NISSAN = Make.builder().id("mk-2").name("Nissan").build();
	private static final Model CAMRY = Model.builder().id("md-7").name("Camry").makeId("mk-1").build();
	private static final Model PATROL = Model.builder().id("md-5").name("Patrol").makeId("mk-2").build();
	private static final Category ENGINE = Category.builder().id("cat-1").name("Engine").build();

	private static Supplier shop() {
		return Supplier.builder().id(3).name("Shop A").makeIds(new HashSet<>()).modelIds(new HashSet<>())
				.categoryIds(new HashSet<>()).conditions(new HashSet<>()).build();
	}

	@Test
	void legacyTextWithSeveralMakesResolvesToIdsAndNormalisedText() {
		when(makeRepository.findByName("Toyota")).thenReturn(TOYOTA);
		when(makeRepository.findByName("Nissan")).thenReturn(NISSAN);
		when(modelRepository.findByMakeIdInAndNameIn(List.of("mk-2", "mk-1"), List.of("patrol", "Camry")))
				.thenReturn(List.of(CAMRY, PATROL));
		SupplierRequest request = SupplierRequest.builder().brands(" Toyota, Nissan,Toyota ").models("patrol, Camry").build();

		Supplier supplier = shop();
		resolver.apply(resolver.resolve(request, null), supplier);

		assertEquals(Set.of("mk-1", "mk-2"), supplier.getMakeIds());
		assertEquals(Set.of("md-7", "md-5"), supplier.getModelIds());
		assertEquals("Nissan, Toyota", supplier.getBrands());
		assertEquals("Camry, Patrol", supplier.getModels());
		assertTrue(supplier.getCategoryIds().isEmpty());
	}

	@Test
	void jsonStyleListTextIsAccepted() {
		when(makeRepository.findByName("Toyota")).thenReturn(TOYOTA);
		when(makeRepository.findByName("Nissan")).thenReturn(NISSAN);
		ShopCoverage coverage = resolver.resolve(SupplierRequest.builder().brands("[\"Toyota\",\"Nissan\"]").build(), null);
		assertEquals(List.of(NISSAN, TOYOTA), coverage.makes());
	}

	@Test
	void idsCategoriesAndConditionsAreUsedAsGiven() {
		when(makeRepository.findAllById(any())).thenReturn(List.of(TOYOTA));
		when(modelRepository.findAllById(any())).thenReturn(List.of(CAMRY));
		when(categoryRepository.findAllById(any())).thenReturn(List.of(ENGINE));
		SupplierRequest request = SupplierRequest.builder().makeIds(List.of("mk-1")).modelIds(List.of("md-7"))
				.categoryIds(List.of("cat-1")).conditions(List.of(ProductCondition.USED, ProductCondition.GENUINE))
				.brands("ignored").build();

		Supplier supplier = shop();
		resolver.apply(resolver.resolve(request, null), supplier);

		assertEquals(Set.of("cat-1"), supplier.getCategoryIds());
		assertEquals(Set.of(ProductCondition.USED, ProductCondition.GENUINE), supplier.getConditions());
		verify(makeRepository, never()).findByName(any());
	}

	@Test
	void unknownMakeNameIsRejected() {
		when(makeRepository.findByName("Toyta")).thenReturn(null);
		VehicleFitException e = assertThrows(VehicleFitException.class,
				() -> resolver.resolve(SupplierRequest.builder().brands("Toyta").build(), null));
		assertEquals("brands", e.getField());
		assertEquals("Toyta", e.getValue());
	}

	@Test
	void unknownCategoryIdIsRejected() {
		when(categoryRepository.findAllById(any())).thenReturn(List.of());
		VehicleFitException e = assertThrows(VehicleFitException.class,
				() -> resolver.resolve(SupplierRequest.builder().categoryIds(List.of("cat-404")).build(), null));
		assertEquals("cat-404", e.getValue());
	}

	@Test
	void modelOfAMakeTheShopDoesNotCoverIsRejected() {
		when(makeRepository.findAllById(any())).thenReturn(List.of(TOYOTA));
		when(modelRepository.findAllById(any())).thenReturn(List.of(PATROL));
		assertThrows(VehicleFitException.class, () -> resolver.resolve(
				SupplierRequest.builder().makeIds(List.of("mk-1")).modelIds(List.of("md-5")).build(), null));
	}

	@Test
	void ambiguousModelNameIsRejected() {
		Model camry2 = Model.builder().id("md-70").name("Camry").makeId("mk-1").build();
		when(makeRepository.findByName("Toyota")).thenReturn(TOYOTA);
		when(modelRepository.findByMakeIdInAndNameIn(anyList(), anyList())).thenReturn(List.of(CAMRY, camry2));
		VehicleFitException e = assertThrows(VehicleFitException.class,
				() -> resolver.resolve(SupplierRequest.builder().brands("Toyota").models("Camry").build(), null));
		assertTrue(e.getMessage().contains("send modelIds"));
	}

	@Test
	void modelsWithoutMakesAreRejected() {
		assertThrows(VehicleFitException.class,
				() -> resolver.resolve(SupplierRequest.builder().models("Camry").build(), null));
	}

	@Test
	void changingOnlyMakesDropsModelsOfRemovedMakesAndKeepsTheRest() {
		Supplier existing = shop();
		existing.getMakeIds().addAll(Set.of("mk-1", "mk-2"));
		existing.getModelIds().addAll(Set.of("md-7", "md-5"));
		existing.getConditions().add(ProductCondition.USED);
		when(makeRepository.findAllById(any())).thenReturn(List.of(NISSAN));
		when(modelRepository.findAllById(any())).thenReturn(List.of(CAMRY, PATROL));

		ShopCoverage coverage = resolver.resolve(SupplierRequest.builder().makeIds(List.of("mk-2")).build(), existing);

		assertEquals(List.of(PATROL), coverage.models());
		assertEquals(Set.of(ProductCondition.USED), coverage.conditions());
	}

	@Test
	void emptyListsClearTheCoverage() {
		Supplier supplier = shop();
		supplier.getMakeIds().add("mk-1");
		supplier.setBrands("Toyota");
		resolver.apply(resolver.resolve(SupplierRequest.builder().makeIds(new ArrayList<>()).modelIds(List.of())
				.categoryIds(List.of()).conditions(List.of()).build(), supplier), supplier);
		assertTrue(supplier.getMakeIds().isEmpty());
		assertNull(supplier.getBrands());
	}

	@Test
	void requestWithoutCoverageFieldsIsDetected() {
		assertTrue(!ShopCoverageResolver.hasCoverageData(SupplierRequest.builder().name("Shop").build()));
		assertTrue(ShopCoverageResolver.hasCoverageData(SupplierRequest.builder().conditions(List.of()).build()));
	}

	// ------------------------------------------------------------ years

	@Test
	void yearRangeIsSavedAndValidated() {
		Supplier supplier = shop();
		resolver.apply(resolver.resolve(SupplierRequest.builder().yearFrom(2010).yearTo(2020).build(), null), supplier);
		assertEquals(2010, supplier.getYearFrom());
		assertEquals(2020, supplier.getYearTo());

		ShopCoverage openEnded = resolver.resolve(SupplierRequest.builder().yearFrom(2015).build(), null);
		assertEquals(2015, openEnded.yearFrom());
		assertNull(openEnded.yearTo());

		assertThrows(VehicleFitException.class,
				() -> resolver.resolve(SupplierRequest.builder().yearFrom(2020).yearTo(2010).build(), null));
		assertThrows(VehicleFitException.class,
				() -> resolver.resolve(SupplierRequest.builder().yearFrom(1949).build(), null));
		assertThrows(VehicleFitException.class,
				() -> resolver.resolve(SupplierRequest.builder().yearTo(java.time.Year.now().getValue() + 6).build(), null));
	}

	@Test
	void yearRangeOnUpdateIsKeptReplacedOrCleared() {
		Supplier existing = shop();
		existing.setYearFrom(2010);
		existing.setYearTo(2020);

		ShopCoverage kept = resolver.resolve(SupplierRequest.builder().conditions(List.of()).build(), existing);
		assertEquals(2010, kept.yearFrom());
		assertEquals(2020, kept.yearTo());

		ShopCoverage replaced = resolver.resolve(SupplierRequest.builder().yearTo(2022).build(), existing);
		assertNull(replaced.yearFrom()); // sending either year replaces both
		assertEquals(2022, replaced.yearTo());

		ShopCoverage cleared = resolver.resolve(SupplierRequest.builder().allYears(true).yearFrom(2000).build(), existing);
		assertNull(cleared.yearFrom());
		assertNull(cleared.yearTo());
		assertTrue(ShopCoverageResolver.hasCoverageData(SupplierRequest.builder().allYears(true).build()));
	}

	@Test
	@SuppressWarnings("unchecked")
	void matchingChecksTheYearAgainstTheShopsRangeOnlyWhenAYearIsGiven() {
		jakarta.persistence.criteria.Root<Supplier> root = org.mockito.Mockito.mock(jakarta.persistence.criteria.Root.class);
		jakarta.persistence.criteria.CriteriaQuery<Object> query = org.mockito.Mockito.mock(jakarta.persistence.criteria.CriteriaQuery.class);
		jakarta.persistence.criteria.CriteriaBuilder cb = org.mockito.Mockito.mock(jakarta.persistence.criteria.CriteriaBuilder.class);
		jakarta.persistence.criteria.Path<Object> path = org.mockito.Mockito.mock(jakarta.persistence.criteria.Path.class);
		when(root.get(org.mockito.ArgumentMatchers.anyString())).thenReturn(path);

		com.autoadda.apis.part.specification.ShopMatchSpecification
				.covering("mk-1", null, Set.of(), Set.of(), 2019).toPredicate(root, query, cb);
		verify(cb).lessThanOrEqualTo(org.mockito.ArgumentMatchers.<jakarta.persistence.criteria.Expression<Integer>>any(),
				org.mockito.ArgumentMatchers.eq(2019));
		verify(cb).greaterThanOrEqualTo(org.mockito.ArgumentMatchers.<jakarta.persistence.criteria.Expression<Integer>>any(),
				org.mockito.ArgumentMatchers.eq(2019));

		jakarta.persistence.criteria.CriteriaBuilder cbNoYear = org.mockito.Mockito.mock(jakarta.persistence.criteria.CriteriaBuilder.class);
		com.autoadda.apis.part.specification.ShopMatchSpecification
				.covering("mk-1", null, Set.of(), Set.of(), null).toPredicate(root, query, cbNoYear);
		verify(cbNoYear, never()).lessThanOrEqualTo(org.mockito.ArgumentMatchers.<jakarta.persistence.criteria.Expression<Integer>>any(),
				org.mockito.ArgumentMatchers.anyInt());
	}

	// ------------------------------------------------------------ backfill

	@Test
	void backfillDryRunReportsAndSavesNothingRealRunSavesOnlyResolvable() {
		SupplierRepository supplierRepository = org.mockito.Mockito.mock(SupplierRepository.class);
		Supplier good = shop();
		good.setBrands("Toyota");
		Supplier bad = shop();
		bad.setId(4);
		bad.setBrands("Toyta");
		Supplier done = shop();
		done.setBrands("Nissan");
		done.getMakeIds().add("mk-2");
		when(supplierRepository.findAll()).thenReturn(List.of(good, bad, done));
		when(makeRepository.findByName("Toyota")).thenReturn(TOYOTA);
		when(makeRepository.findByName("Toyta")).thenReturn(null);
		ShopCoverageBackfillService backfill = new ShopCoverageBackfillService(supplierRepository, resolver);

		ShopCoverageBackfillResponse dry = backfill.backfill(true);
		assertEquals(2, dry.getCandidates());
		assertEquals(1, dry.getResolvable());
		assertEquals(0, dry.getConverted());
		assertEquals(4, dry.getUnresolved().get(0).getSupplierId());
		assertTrue(good.getMakeIds().isEmpty());
		verify(supplierRepository, never()).saveAll(any());

		ShopCoverageBackfillResponse real = backfill.backfill(false);
		assertEquals(1, real.getConverted());
		assertEquals(Set.of("mk-1"), good.getMakeIds());
		verify(supplierRepository).saveAll(List.of(good));
	}

	@Test
	void unchangedFieldsOnRealRegistrationAreNotLookedUp() {
		ShopCoverage coverage = resolver.resolve(SupplierRequest.builder().conditions(List.of(ProductCondition.GENUINE)).build(), null);
		assertTrue(coverage.makes().isEmpty());
		assertEquals(Optional.empty(), coverage.models().stream().findAny());
	}
}
