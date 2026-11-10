package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.autoadda.apis.part.entity.Category;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.repository.CategoryRepository;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.request.ProductSearchRequest;
import com.autoadda.apis.part.response.PagedResponse;
import com.autoadda.apis.part.response.ProductResponse;
import com.autoadda.apis.part.service.CategoryHierarchy;
import com.autoadda.apis.part.service.ProductAttributeService;
import com.autoadda.apis.part.service.ProductSearchService;
import com.autoadda.apis.part.service.SupplierService;
import com.autoadda.apis.part.service.VehicleFitResolver;
import com.autoadda.apis.part.specification.ProductSpecification;
import com.autoadda.apis.part.specification.VehicleFilter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class CategorySearchTest {

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
	@Mock
	private CategoryHierarchy categoryHierarchy;
	@InjectMocks
	private ProductSearchService service;

	private static Category category(String id, String parent) {
		return Category.builder().id(id).parentCategoryId(parent).build();
	}

	@Test
	void hierarchyReturnsCategoryAndAllDescendants() {
		CategoryRepository repo = mock(CategoryRepository.class);
		when(repo.findByParentCategoryIdIn(Set.of("1"))).thenReturn(List.of(category("10", "1"), category("11", "1")));
		when(repo.findByParentCategoryIdIn(Set.of("10", "11"))).thenReturn(List.of(category("100", "10")));
		when(repo.findByParentCategoryIdIn(Set.of("100"))).thenReturn(List.of());

		assertEquals(Set.of("1", "10", "11", "100"), new CategoryHierarchy(repo).withDescendants(" 1 "));
	}

	@Test
	void hierarchyStopsOnParentLinkCycles() {
		CategoryRepository repo = mock(CategoryRepository.class);
		when(repo.findByParentCategoryIdIn(Set.of("1"))).thenReturn(List.of(category("2", "1")));
		when(repo.findByParentCategoryIdIn(Set.of("2"))).thenReturn(List.of(category("1", "2")));

		assertEquals(Set.of("1", "2"), new CategoryHierarchy(repo).withDescendants("1"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void paginatedSearchByParentCategoryUsesTheWholeSubtree() {
		when(categoryHierarchy.withDescendants("1")).thenReturn(Set.of("1", "10", "11"));
		when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(Product.builder().id("p1").name("Brake pad").categoryId("10").build())));
		when(inventoryPartRepository.countAvailableListingsByProductIds(List.of("p1"))).thenReturn(List.of());

		PagedResponse<ProductResponse> result = service.advancedSearchPaginated(
				ProductSearchRequest.builder().categoryId("1").page(0).size(10).sortBy("name").sortDirection("ASC").build(),
				false);

		assertEquals(1, result.getNumberOfElements());
		verify(categoryHierarchy).withDescendants("1");
	}

	@Test
	@SuppressWarnings("unchecked")
	void specificationFiltersCategoryWithInOverTheGivenIds() {
		Root<Product> root = mock(Root.class);
		CriteriaQuery<Object> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Path<Object> categoryPath = mock(Path.class);
		when(root.get(anyString())).thenReturn(mock(Path.class));
		when(root.get("categoryId")).thenReturn(categoryPath);
		Set<String> ids = Set.of("1", "10");

		ProductSearchRequest request = ProductSearchRequest.builder().categoryId("1").build();
		ProductSpecification.bySearchRequest(request, VehicleFilter.fromIds(request), ids).toPredicate(root, query, cb);

		verify(categoryPath).in(ids);
	}

	@Test
	void categoryEndpointIncludesSubcategories() {
		when(categoryHierarchy.withDescendants("1")).thenReturn(Set.of("1", "10"));
		when(productRepository.findByCategoryIdIn(Set.of("1", "10")))
				.thenReturn(List.of(Product.builder().id("p1").name("Brake pad").categoryId("10").build()));

		assertEquals(1, service.searchProductsByCategory("1", false).size());
	}
}
