package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.ProductAttribute;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.request.ProductSearchRequest;
import com.autoadda.apis.part.response.PagedResponse;
import com.autoadda.apis.part.response.ProductAttributeResponse;
import com.autoadda.apis.part.response.ProductResponse;
import com.autoadda.apis.part.response.SupplierAvailabilityResponse;
import com.autoadda.apis.part.specification.ProductSpecification;
import com.autoadda.apis.part.specification.VehicleFilter;
import com.autoadda.apis.part.util.ProductCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for unified product search with supplier availability information
 * This service combines product search results with inventory supplier counts
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    public static final String SUPPLIER_COUNT = "supplierCount";
    private final ProductRepository productRepository;
    private final InventoryPartRepository inventoryPartRepository;
    private final ProductAttributeService productAttributeService;
    private final SupplierService supplierService;
    private final VehicleFitResolver vehicleFitResolver;

    /** Vehicle filter with make/model names resolved to ids; empty when a name matches nothing. */
    private Optional<VehicleFilter> vehicleFilter(ProductSearchRequest request) {
        return VehicleFitResolver.hasVehicleNames(request)
                ? vehicleFitResolver.searchFilter(request)
                : Optional.of(VehicleFilter.fromIds(request));
    }

    /**
     * Helper method to enrich product responses with supplier counts
     */
    private List<ProductResponse> enrichProductsWithSupplierCounts(List<Product> products, boolean includeAttributes) {
        return enrichProductsWithSupplierCounts(products, includeAttributes, false);
    }

    /**
     * Helper method to enrich product responses with supplier counts and optionally supplier details
     */
    private List<ProductResponse> enrichProductsWithSupplierCounts(List<Product> products, boolean includeAttributes, boolean includeSuppliers) {
        return enrichProductsWithSupplierCounts(products, includeAttributes, includeSuppliers, null);
    }

    /**
     * Enrich products; when conditions are given, supplier counts and supplier details only include
     * listings in those conditions.
     */
    private List<ProductResponse> enrichProductsWithSupplierCounts(List<Product> products, boolean includeAttributes,
                                                                   boolean includeSuppliers, Collection<ProductCondition> conditions) {
        // Get all product IDs
        List<String> productIds = products.stream()
                .map(Product::getId)
                .toList();

        // Get supplier counts for all products at once
        Map<String, Long> supplierCounts = productIds.stream()
                .collect(Collectors.toMap(
                        productId -> productId,
                        productId -> countAvailableListings(productId, conditions)
                ));

        // Build responses with supplier counts
        return products.stream()
                .map(product -> buildProductResponseWithSupplierCount(product, supplierCounts.get(product.getId()), includeAttributes, includeSuppliers, conditions))
                .toList();
    }

    /**
     * Build a ProductResponse with supplier count
     */
    private ProductResponse buildProductResponseWithSupplierCount(Product product, Long supplierCount, boolean includeAttributes) {
        return buildProductResponseWithSupplierCount(product, supplierCount, includeAttributes, false);
    }

    /**
     * Build a ProductResponse with supplier count and optionally supplier details
     */
    private ProductResponse buildProductResponseWithSupplierCount(Product product, Long supplierCount, boolean includeAttributes, boolean includeSuppliers) {
        return buildProductResponseWithSupplierCount(product, supplierCount, includeAttributes, includeSuppliers, null);
    }

    private ProductResponse buildProductResponseWithSupplierCount(Product product, Long supplierCount, boolean includeAttributes,
                                                                  boolean includeSuppliers, Collection<ProductCondition> conditions) {
        String attributeUrl = "http://localhost:8060/api/v1/productattributes/productid/" + product.getId();

        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .manufacturerId(product.getManufacturerId())
                .categoryId(product.getCategoryId())
                .make(product.getMake())
                .partNumber(product.getPartNumber())
                .year(product.getYear())
                .tag(product.getTag())
                .model(product.getModel())
                .vehicleFit(product)
                .otherNames(product.getOtherNames())
                .attributeUrl(attributeUrl)
                .images(product.getImages())
                .availableSupplierCount(supplierCount != null ? supplierCount : 0L);

        if (includeAttributes) {
            Object attributesResponse = productAttributeService.getProductAttributeByProductId(product.getId()).getBody();
            builder.attributes(
                    attributesResponse != null ? (List<ProductAttributeResponse>) attributesResponse : new ArrayList<>()
            );
        }

        if (includeSuppliers) {
            List<SupplierAvailabilityResponse> suppliers = supplierService.getSuppliersByProductId(product.getId(), conditions);
            builder.suppliers(suppliers);
        }

        return builder.build();
    }

    private static boolean hasConditions(Collection<ProductCondition> conditions) {
        return conditions != null && !conditions.isEmpty();
    }

    /**
     * Number of active, in-stock listings for a product, optionally limited to the given conditions.
     */
    private long countAvailableListings(String productId, Collection<ProductCondition> conditions) {
        Long count = hasConditions(conditions)
                ? inventoryPartRepository.countByProductIdAndIsActiveAndQuantityInStockGreaterThanAndProductConditionIn(
                        productId, true, 0, conditions)
                : inventoryPartRepository.countByProductIdAndIsActiveAndQuantityInStockGreaterThan(productId, true, 0);
        return count != null ? count : 0L;
    }

    /**
     * Search products by name with supplier availability
     */
    @Transactional
    public List<ProductResponse> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContaining(name);
        return enrichProductsWithSupplierCounts(products, false);
    }

    /**
     * Search products by part number with supplier availability
     */
    @Transactional
    public ProductResponse searchProductByPartNumber(String partNumber) {
        Product product = productRepository.findByPartNumber(partNumber);
        if (product == null) {
            return null;
        }
        Long supplierCount = inventoryPartRepository.countByProductIdAndIsActiveAndQuantityInStockGreaterThan(
                product.getId(), true, 0
        );
        return buildProductResponseWithSupplierCount(product, supplierCount, false);
    }

    /**
     * Search products by category with supplier availability
     */
    @Transactional
    public List<ProductResponse> searchProductsByCategory(String categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return enrichProductsWithSupplierCounts(products, false);
    }

    /**
     * Search products by manufacturer with supplier availability
     */
    @Transactional
    public List<ProductResponse> searchProductsByManufacturer(String manufacturerId) {
        List<Product> products = productRepository.findByManufacturerId(manufacturerId);
        return enrichProductsWithSupplierCounts(products, false);
    }

    /**
     * Search products by other names with supplier availability
     */
    @Transactional
    public List<ProductResponse> searchProductsByOtherNames(String otherNames) {
        List<Product> products = productRepository.findByOtherNamesContaining(otherNames);
        return enrichProductsWithSupplierCounts(products, false);
    }

    /**
     * Search products by tag with supplier availability
     */
    @Transactional
    public List<ProductResponse> searchProductsByTag(String tag) {
        List<Product> products = productRepository.findByTagContaining(tag);
        return enrichProductsWithSupplierCounts(products, false);
    }

    /**
     * Get product by ID with supplier availability
     */
    @Transactional
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return null;
        }
        Long supplierCount = inventoryPartRepository.countByProductIdAndIsActiveAndQuantityInStockGreaterThan(
                product.getId(), true, 0
        );
        return buildProductResponseWithSupplierCount(product, supplierCount, true);
    }

    /**
     * Get all products with supplier availability
     */
    @Transactional
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return enrichProductsWithSupplierCounts(products, true);
    }

    /**
     * Advanced search with multiple optional filters
     * Combines product filters with supplier availability filtering
     */
    @Transactional
    public List<ProductResponse> advancedSearch(ProductSearchRequest request) {
        log.info("Performing advanced product search with request: {}", request);

        // Step 1: Build specification from request (make/model names resolved to ids first)
        Optional<VehicleFilter> vehicle = vehicleFilter(request);
        if (vehicle.isEmpty()) {
            log.info("Advanced search: make/model name matched nothing, returning no products");
            return List.of();
        }
        Specification<Product> spec = ProductSpecification.bySearchRequest(request, vehicle.get());

        // Step 2: Build sort
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());

        // Step 3: Execute query with specification and sorting (sort is always non-null)
        List<Product> products = productRepository.findAll(spec, sort);

        // Step 4: Enrich with supplier counts
        List<ProductResponse> enrichedProducts = enrichProductsWithSupplierCounts(products, false, false, request.getConditions());

        // inStockOnly / minSuppliers / maxSuppliers are applied in the query (ProductSpecification)

        // Step 5: Sort by supplier count if requested
        if (SUPPLIER_COUNT.equals(request.getSortBy())) {
            enrichedProducts = enrichedProducts.stream()
                    .sorted((p1, p2) -> {
                        int comparison = p1.getAvailableSupplierCount().compareTo(p2.getAvailableSupplierCount());
                        return "DESC".equalsIgnoreCase(request.getSortDirection()) ? -comparison : comparison;
                    })
                    .toList();
        }

        log.info("Advanced search returned {} products", enrichedProducts.size());
        return enrichedProducts;
    }

    // ============ Overloaded methods with includeSuppliers parameter ============

    /**
     * Search products by name with supplier availability (with optional supplier details)
     */
    @Transactional
    public List<ProductResponse> searchProductsByName(String name, boolean includeSuppliers) {
        List<Product> products = productRepository.findByNameContaining(name);
        return enrichProductsWithSupplierCounts(products, false, includeSuppliers);
    }

    /**
     * Search products by part number with supplier availability (with optional supplier details)
     */
    @Transactional
    public ProductResponse searchProductByPartNumber(String partNumber, boolean includeSuppliers) {
        Product product = productRepository.findByPartNumber(partNumber);
        if (product == null) {
            return null;
        }
        Long supplierCount = inventoryPartRepository.countByProductIdAndIsActiveAndQuantityInStockGreaterThan(
                product.getId(), true, 0
        );
        return buildProductResponseWithSupplierCount(product, supplierCount, false, includeSuppliers);
    }

    /**
     * Products that fit a vehicle: any combination of make, model (ids or names) and year.
     * Uses the same filter as the advanced search; unknown names give no results.
     */
    @Transactional
    public List<ProductResponse> searchProductsByVehicle(ProductSearchRequest vehicle, boolean includeSuppliers) {
        Optional<VehicleFilter> filter = vehicleFilter(vehicle);
        if (filter.isEmpty()) {
            return List.of();
        }
        List<Product> products = productRepository.findAll(
                ProductSpecification.bySearchRequest(vehicle, filter.get()), Sort.by("id"));
        return enrichProductsWithSupplierCounts(products, false, includeSuppliers);
    }

    /**
     * Search products by category with supplier availability (with optional supplier details)
     */
    @Transactional
    public List<ProductResponse> searchProductsByCategory(String categoryId, boolean includeSuppliers) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return enrichProductsWithSupplierCounts(products, false, includeSuppliers);
    }

    /**
     * Search products by manufacturer with supplier availability (with optional supplier details)
     */
    @Transactional
    public List<ProductResponse> searchProductsByManufacturer(String manufacturerId, boolean includeSuppliers) {
        List<Product> products = productRepository.findByManufacturerId(manufacturerId);
        return enrichProductsWithSupplierCounts(products, false, includeSuppliers);
    }

    /**
     * Search products by other names with supplier availability (with optional supplier details)
     */
    @Transactional
    public List<ProductResponse> searchProductsByOtherNames(String otherNames, boolean includeSuppliers) {
        List<Product> products = productRepository.findByOtherNamesContaining(otherNames);
        return enrichProductsWithSupplierCounts(products, false, includeSuppliers);
    }

    /**
     * Search products by tag with supplier availability (with optional supplier details)
     */
    @Transactional
    public List<ProductResponse> searchProductsByTag(String tag, boolean includeSuppliers) {
        List<Product> products = productRepository.findByTagContaining(tag);
        return enrichProductsWithSupplierCounts(products, false, includeSuppliers);
    }

    /**
     * Get product by ID with supplier availability (with optional supplier details)
     */
    @Transactional
    public ProductResponse getProductById(String id, boolean includeSuppliers) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return null;
        }
        Long supplierCount = inventoryPartRepository.countByProductIdAndIsActiveAndQuantityInStockGreaterThan(
                product.getId(), true, 0
        );
        return buildProductResponseWithSupplierCount(product, supplierCount, true, includeSuppliers);
    }

    /**
     * Get all products with supplier availability (with optional supplier details)
     */
    @Transactional
    public List<ProductResponse> getAllProducts(boolean includeSuppliers) {
        List<Product> products = productRepository.findAll();
        return enrichProductsWithSupplierCounts(products, true, includeSuppliers);
    }

    /**
     * Advanced search with multiple optional filters (with optional supplier details)
     * Combines product filters with supplier availability filtering
     */
    @Transactional
    public List<ProductResponse> advancedSearch(ProductSearchRequest request, boolean includeSuppliers) {
        log.info("Performing advanced product search with request: {}", request);

        // Step 1: Build specification from request (make/model names resolved to ids first)
        Optional<VehicleFilter> vehicle = vehicleFilter(request);
        if (vehicle.isEmpty()) {
            log.info("Advanced search: make/model name matched nothing, returning no products");
            return List.of();
        }
        Specification<Product> spec = ProductSpecification.bySearchRequest(request, vehicle.get());

        // Step 2: Build sort
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());

        // Step 3: Execute query with specification and sorting (sort is always non-null)
        List<Product> products = productRepository.findAll(spec, sort);

        // Step 4: Enrich with supplier counts and optionally supplier details
        List<ProductResponse> enrichedProducts = enrichProductsWithSupplierCounts(products, false, includeSuppliers, request.getConditions());

        // inStockOnly / minSuppliers / maxSuppliers are applied in the query (ProductSpecification)

        // Step 5: Sort by supplier count if requested
        if (SUPPLIER_COUNT.equals(request.getSortBy())) {
            enrichedProducts = enrichedProducts.stream()
                    .sorted((p1, p2) -> {
                        int comparison = p1.getAvailableSupplierCount().compareTo(p2.getAvailableSupplierCount());
                        return "DESC".equalsIgnoreCase(request.getSortDirection()) ? -comparison : comparison;
                    })
                    .toList();
        }

        log.info("Advanced search returned {} products", enrichedProducts.size());
        return enrichedProducts;
    }

    // ============ Paginated Search Methods ============

    /**
     * Advanced search with pagination
     * Uses pre-computed supplier counts from database for optimal performance
     */
    @Transactional
    public PagedResponse<ProductResponse> advancedSearchPaginated(
            ProductSearchRequest searchRequest,
            boolean includeSuppliers) {

        log.info("Advanced paginated product search: page={}, size={}, filters={}",
                searchRequest.getPage(), searchRequest.getSize(), searchRequest);

        // Build Pageable with sorting
        Pageable pageable = buildPageable(searchRequest);

        // Build specification from request (make/model names resolved to ids first)
        Optional<VehicleFilter> vehicle = vehicleFilter(searchRequest);
        if (vehicle.isEmpty()) {
            log.info("Paginated search: make/model name matched nothing, returning an empty page");
            return PagedResponse.<ProductResponse>builder()
                    .content(List.of()).page(pageable.getPageNumber()).size(pageable.getPageSize())
                    .totalElements(0L).totalPages(0).first(true).last(true)
                    .numberOfElements(0).empty(true).build();
        }
        Specification<Product> spec = ProductSpecification.bySearchRequest(searchRequest, vehicle.get());

        // Execute paginated query with specification
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // Live supplier counts for this page in one query (same definition the supplier filters use)
        Map<String, Long> supplierCounts = countAvailableListings(productPage.getContent(), searchRequest.getConditions());

        // inStockOnly / minSuppliers / maxSuppliers are applied in the query, so the page is already filtered
        List<ProductResponse> responses = productPage.getContent().stream()
                .map(product -> buildProductResponse(product, includeSuppliers, searchRequest.getConditions(),
                        supplierCounts.getOrDefault(product.getId(), 0L)))
                .toList();

        // Wrap in paginated response
        PagedResponse<ProductResponse> pagedResponse = PagedResponse.<ProductResponse>builder()
                .content(responses)
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .numberOfElements(responses.size())
                .empty(responses.isEmpty())
                .build();

        log.info("Advanced paginated search returned {} products in page {}/{}",
                responses.size(), productPage.getNumber(), productPage.getTotalPages());

        return pagedResponse;
    }

    /**
     * Build Pageable object from request parameters
     */
    private Pageable buildPageable(ProductSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;

        if (request.getSortBy() != null && !request.getSortBy().isBlank()) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            // Map sortBy field (handle supplierCount -> supplier_count)
            String sortField = SUPPLIER_COUNT.equals(request.getSortBy())
                    ? SUPPLIER_COUNT  // Entity field name
                    : request.getSortBy();

            return PageRequest.of(page, size, Sort.by(direction, sortField));
        }

        return PageRequest.of(page, size);
    }

    /**
     * Build Sort object from sort parameters
     * Used for non-paginated queries that need sorting
     */
    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by("id").ascending(); // Default sort
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Handle supplierCount separately
        if (SUPPLIER_COUNT.equals(sortBy)) {
            return Sort.by(direction, SUPPLIER_COUNT);
        }

        return Sort.by(direction, sortBy);
    }

    /**
     * Build ProductResponse from Product entity using pre-computed supplier count
     */
    private ProductResponse buildProductResponse(Product product, boolean includeSuppliers,
                                                 Collection<ProductCondition> conditions, long supplierCount) {
        String attributeUrl = "http://localhost:8060/api/v1/productattributes/productid/" + product.getId();

        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .partNumber(product.getPartNumber())
                .year(product.getYear())
                .make(product.getMake())
                .model(product.getModel())
                .vehicleFit(product)
                .tag(product.getTag())
                .otherNames(product.getOtherNames())
                .categoryId(product.getCategoryId())
                .manufacturerId(product.getManufacturerId())
                .attributeUrl(attributeUrl)
                .images(product.getImages())
                .availableSupplierCount(supplierCount);

        // Include detailed suppliers if requested
        if (includeSuppliers) {
            List<SupplierAvailabilityResponse> suppliers =
                    supplierService.getSuppliersByProductId(product.getId(), conditions);
            builder.suppliers(suppliers);
        }

        return builder.build();
    }

    /**
     * Live count of active, in-stock listings per product (limited to conditions, if given), one query per page.
     */
    private Map<String, Long> countAvailableListings(List<Product> products, Collection<ProductCondition> conditions) {
        if (products.isEmpty()) {
            return Map.of();
        }
        List<String> ids = products.stream().map(Product::getId).toList();
        List<Object[]> rows = hasConditions(conditions)
                ? inventoryPartRepository.countAvailableListingsByProductIdsAndConditions(ids, conditions)
                : inventoryPartRepository.countAvailableListingsByProductIds(ids);
        return rows.stream().collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).longValue()));
    }
}
