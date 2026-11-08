package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.exception.VehicleFitException;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.request.ProductRequest;
import com.autoadda.apis.part.request.ProductSearchRequest;
import com.autoadda.apis.part.response.PagedResponse;
import com.autoadda.apis.part.response.ProductResponse;
import com.autoadda.apis.part.response.VehicleBackfillResponse;
import com.autoadda.apis.part.service.ProductAttributeService;
import com.autoadda.apis.part.service.ProductSearchService;
import com.autoadda.apis.part.service.SupplierService;
import com.autoadda.apis.part.service.VehicleDataBackfillService;
import com.autoadda.apis.part.service.VehicleFitResolver;
import com.autoadda.apis.part.service.VehicleFitResolver.VehicleFit;
import com.autoadda.apis.part.specification.ProductSpecification;
import com.autoadda.apis.part.specification.VehicleFilter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class VehicleSearchAndBackfillTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private InventoryPartRepository inventoryPartRepository;
	@Mock
	private ProductAttributeService productAttributeService;
	@Mock
	private SupplierService supplierService;
	@Mock
	private VehicleFitResolver vehicleFitResolver;

	private ProductSearchService searchService() {
		return new ProductSearchService(productRepository, inventoryPartRepository, productAttributeService,
				supplierService, vehicleFitResolver);
	}

	// ------------------------------------------------------------ specification

	@Test
	@SuppressWarnings("unchecked")
	void specificationFiltersOnMakeIdModelMembershipAndYearMembership() {
		Root<Product> root = mock(Root.class);
		CriteriaQuery<Object> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Path<Object> makeId = mock(Path.class);
		Path<Object> modelIds = mock(Path.class);
		Path<Object> years = mock(Path.class);
		when(root.get("makeId")).thenReturn(makeId);
		when(root.get("modelIds")).thenReturn(modelIds);
		when(root.get("years")).thenReturn(years);

		ProductSpecification.bySearchRequest(new ProductSearchRequest(),
				new VehicleFilter("mk-1", Set.of("md-7", "md-9"), 2020)).toPredicate(root, query, cb);

		verify(cb).equal(makeId, "mk-1");
		verify(cb).isMember(eq("md-7"), any());
		verify(cb).isMember(eq("md-9"), any());
		verify(cb).isMember(eq(2020), any());
		// the old text columns are no longer compared
		verify(root, never()).get("make");
		verify(root, never()).get("model");
		verify(root, never()).get("year");
	}

	// ------------------------------------------------------------ search service

	@Test
	@SuppressWarnings("unchecked")
	void searchByNameResolvesNamesFirst() {
		ProductSearchRequest request = ProductSearchRequest.builder().make("Toyota").year(2020).page(0).size(10).build();
		when(vehicleFitResolver.searchFilter(request))
				.thenReturn(Optional.of(new VehicleFilter("mk-1", Set.of(), 2020)));
		when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

		searchService().advancedSearchPaginated(request, false);

		verify(vehicleFitResolver).searchFilter(request);
	}

	@Test
	@SuppressWarnings("unchecked")
	void unknownMakeNameGivesEmptyResultWithoutQuerying() {
		ProductSearchRequest request = ProductSearchRequest.builder().make("Nope").page(0).size(10).build();
		when(vehicleFitResolver.searchFilter(request)).thenReturn(Optional.empty());

		PagedResponse<ProductResponse> page = searchService().advancedSearchPaginated(request, false);
		List<ProductResponse> byVehicle = searchService().searchProductsByVehicle(request, false);

		assertTrue(page.getContent().isEmpty());
		assertEquals(0L, page.getTotalElements());
		assertTrue(byVehicle.isEmpty());
		verify(productRepository, never()).findAll(any(Specification.class), any(Pageable.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	void searchByIdsSkipsNameLookupAndReturnsFitInResponse() {
		ProductSearchRequest request = ProductSearchRequest.builder().makeId("mk-1").modelId("md-7").build();
		Product product = Product.builder().id("p1").name("Brake pad").makeId("mk-1")
				.modelIds(Set.of("md-9", "md-7")).years(Set.of(2020, 2018)).build();
		when(productRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
				.thenReturn(List.of(product));

		List<ProductResponse> result = searchService().searchProductsByVehicle(request, false);

		verify(vehicleFitResolver, never()).searchFilter(any());
		assertEquals("mk-1", result.get(0).getMakeId());
		assertEquals(List.of("md-7", "md-9"), result.get(0).getModelIds());
		assertEquals(List.of(2018, 2020), result.get(0).getYears());
	}

	// ------------------------------------------------------------ backfill

	@Test
	void dryRunReportsAndSavesNothing() {
		Product ok = Product.builder().id("p1").partNumber("BP-1").make("Toyota").model("Camry").year("2020").build();
		Product bad = Product.builder().id("p2").partNumber("BP-2").make("Toyta").build();
		Product done = Product.builder().id("p3").make("Toyota").makeId("mk-1").build();
		Product noVehicle = Product.builder().id("p4").name("Oil").build();
		when(productRepository.findAll()).thenReturn(List.of(ok, bad, done, noVehicle));
		when(vehicleFitResolver.resolve(any(ProductRequest.class), eq(null))).thenAnswer(inv -> {
			ProductRequest r = inv.getArgument(0);
			if ("Toyta".equals(r.getMake())) {
				throw new VehicleFitException("make", "Toyta", "Unknown make 'Toyta'");
			}
			return new VehicleFit(null, List.of(), List.of(2020));
		});

		VehicleBackfillResponse report = new VehicleDataBackfillService(productRepository, vehicleFitResolver).backfill(true);

		assertEquals(2, report.getCandidates()); // p3 already converted, p4 has no vehicle text
		assertEquals(1, report.getResolvable());
		assertEquals(0, report.getConverted());
		assertEquals(1, report.getUnresolved().size());
		assertEquals("p2", report.getUnresolved().get(0).getProductId());
		assertEquals("make", report.getUnresolved().get(0).getField());
		assertEquals("Toyta", report.getUnresolved().get(0).getValue());
		verify(vehicleFitResolver, never()).apply(any(), any());
		verify(productRepository, never()).saveAll(any());
	}

	@Test
	@SuppressWarnings("unchecked")
	void realRunSavesOnlyResolvedProducts() {
		Product ok = Product.builder().id("p1").make("Toyota").model("Camry").year("2020").build();
		Product bad = Product.builder().id("p2").make("Toyta").build();
		when(productRepository.findAll()).thenReturn(List.of(ok, bad));
		VehicleFit fit = new VehicleFit(null, List.of(), List.of(2020));
		when(vehicleFitResolver.resolve(any(ProductRequest.class), eq(null))).thenAnswer(inv -> {
			if ("Toyta".equals(((ProductRequest) inv.getArgument(0)).getMake())) {
				throw new VehicleFitException("make", "Toyta", "Unknown make 'Toyta'");
			}
			return fit;
		});

		VehicleBackfillResponse report = new VehicleDataBackfillService(productRepository, vehicleFitResolver).backfill(false);

		assertEquals(1, report.getConverted());
		verify(vehicleFitResolver).apply(fit, ok);
		ArgumentCaptor<List<Product>> saved = ArgumentCaptor.forClass(List.class);
		verify(productRepository).saveAll(saved.capture());
		assertEquals(List.of(ok), saved.getValue());
		verify(vehicleFitResolver, never()).apply(any(), eq(bad));
		verify(supplierService, never()).getSuppliersByProductId(anyString());
	}
}
