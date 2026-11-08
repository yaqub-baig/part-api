package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.request.ProductSearchRequest;
import com.autoadda.apis.part.response.PagedResponse;
import com.autoadda.apis.part.response.ProductResponse;
import com.autoadda.apis.part.service.ProductAttributeService;
import com.autoadda.apis.part.service.ProductSearchService;
import com.autoadda.apis.part.service.SupplierService;
import com.autoadda.apis.part.specification.ProductSpecification;
import com.autoadda.apis.part.util.ProductCondition;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@ExtendWith(MockitoExtension.class)
class ProductSearchConditionTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private InventoryPartRepository inventoryPartRepository;
	@Mock
	private ProductAttributeService productAttributeService;
	@Mock
	private SupplierService supplierService;
	@InjectMocks
	private ProductSearchService service;

	private static final List<ProductCondition> GENUINE_OR_AFTERMARKET =
			List.of(ProductCondition.GENUINE, ProductCondition.AFTER_MARKET);

	private static Product product(String id, int precomputedSupplierCount) {
		return Product.builder().id(id).name("Brake pad").partNumber("BP-" + id)
				.supplierCount(precomputedSupplierCount).build();
	}

	@Test
	@SuppressWarnings("unchecked")
	void paginatedSearchWithConditionsCountsAndListsOnlyMatchingListings() {
		ProductSearchRequest request = ProductSearchRequest.builder()
				.conditions(GENUINE_OR_AFTERMARKET).page(0).size(10).build();
		when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(product("p1", 5))));
		when(inventoryPartRepository.countByProductIdAndIsActiveAndQuantityInStockGreaterThanAndProductConditionIn(
				"p1", true, 0, GENUINE_OR_AFTERMARKET)).thenReturn(2L);

		PagedResponse<ProductResponse> result = service.advancedSearchPaginated(request, true);

		// 5 listings overall, only 2 are genuine/after-market
		assertEquals(2L, result.getContent().get(0).getAvailableSupplierCount());
		verify(supplierService).getSuppliersByProductId("p1", GENUINE_OR_AFTERMARKET);
	}

	@Test
	@SuppressWarnings("unchecked")
	void paginatedSearchWithoutConditionsKeepsPrecomputedCount() {
		ProductSearchRequest request = ProductSearchRequest.builder().page(0).size(10).build();
		when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(product("p1", 5))));

		PagedResponse<ProductResponse> result = service.advancedSearchPaginated(request, false);

		assertEquals(5L, result.getContent().get(0).getAvailableSupplierCount());
		verify(inventoryPartRepository, never())
				.countByProductIdAndIsActiveAndQuantityInStockGreaterThanAndProductConditionIn(anyString(), any(), anyInt(), any());
	}

	@Test
	@SuppressWarnings("unchecked")
	void advancedSearchWithConditionUsesConditionCount() {
		List<ProductCondition> usedOnly = List.of(ProductCondition.USED);
		ProductSearchRequest request = ProductSearchRequest.builder().conditions(usedOnly).build();
		when(productRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
				.thenReturn(List.of(product("p1", 3)));
		when(inventoryPartRepository.countByProductIdAndIsActiveAndQuantityInStockGreaterThanAndProductConditionIn(
				"p1", true, 0, usedOnly)).thenReturn(1L);

		List<ProductResponse> result = service.advancedSearch(request, true);

		assertEquals(1L, result.get(0).getAvailableSupplierCount());
		verify(supplierService).getSuppliersByProductId("p1", usedOnly);
		verify(inventoryPartRepository, never()).countByProductIdAndIsActiveAndQuantityInStockGreaterThan(anyString(), any(), anyInt());
	}

	@Test
	@SuppressWarnings("unchecked")
	void specificationAddsExistsSubqueryOnlyWhenConditionsGiven() {
		Root<Product> root = mock(Root.class);
		CriteriaQuery<Object> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Subquery<Integer> subquery = mock(Subquery.class, RETURNS_DEEP_STUBS);
		Root<InventoryPart> ip = mock(Root.class);
		Path<Object> conditionPath = mock(Path.class);
		Predicate inPredicate = mock(Predicate.class);

		// No conditions: no subquery at all
		ProductSpecification.bySearchRequest(new ProductSearchRequest()).toPredicate(root, query, cb);
		verify(query, never()).subquery(any());

		// With conditions: EXISTS (... productCondition IN (...))
		when(query.subquery(Integer.class)).thenReturn(subquery);
		when(subquery.from(InventoryPart.class)).thenReturn(ip);
		when(ip.get(anyString())).thenReturn(mock(Path.class));
		when(ip.get("productCondition")).thenReturn(conditionPath);
		when(conditionPath.in(GENUINE_OR_AFTERMARKET)).thenReturn(inPredicate);

		ProductSearchRequest request = ProductSearchRequest.builder().conditions(GENUINE_OR_AFTERMARKET).build();
		ProductSpecification.bySearchRequest(request).toPredicate(root, query, cb);

		verify(conditionPath).in(GENUINE_OR_AFTERMARKET);
		verify(cb).exists(subquery);
		verify(cb).greaterThan(any(), eq(0));
	}
}
