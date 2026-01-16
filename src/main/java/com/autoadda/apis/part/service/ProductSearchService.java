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
import java.util.List;
import java.util.Map;
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
        // Get all product IDs
        List<String> productIds = products.stream()
                .map(Product::getId)
                .toList();

        // Get supplier counts for all products at once
        Map<String, Long> supplierCounts = productIds.stream()
                .collect(Collectors.toMap(
                        productId -> productId,
                        productId -> inventoryPartRepository.countByProductIdAndIsActiveAndQuantityInStockGreaterThan(productId, true, 0)
                ));

        // Build responses with supplier counts
        return products.stream()
                .map(product -> buildProductResponseWithSupplierCount(product, supplierCounts.get(product.getId()), includeAttributes, includeSuppliers))
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
            List<SupplierAvailabilityResponse> suppliers = supplierService.getSuppliersByProductId(product.getId());
            builder.suppliers(suppliers);
        }

        return builder.build();
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
     * Search products by year, make, and model with supplier availability
     */
    @Transactional
    public List<ProductResponse> searchProductsByYearMakeModel(Integer year, String make, String model) {
        List<Product> products = productRepository.findByYearAndMakeAndModel(year != null ? year.toString() : null, make, model);
        return enrichProductsWithSupplierCounts(products, false);
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

        // Step 1: Build specification from request
        Specification<Product> spec = ProductSpecification.bySearchRequest(request);

        // Step 2: Build sort
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());

        // Step 3: Execute query with specification and sorting (sort is always non-null)
        List<Product> products = productRepository.findAll(spec, sort);

        // Step 4: Enrich with supplier counts
        List<ProductResponse> enrichedProducts = enrichProductsWithSupplierCounts(products, false);

        // Step 5: Apply supplier count filters if specified
        if (request.getInStockOnly() != null && request.getInStockOnly()) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() > 0)
                    .toList();
        }

        if (request.getMinSuppliers() != null) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() >= request.getMinSuppliers())
                    .toList();
        }

        if (request.getMaxSuppliers() != null) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() <= request.getMaxSuppliers())
                    .toList();
        }

        // Step 6: Sort by supplier count if requested
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
     * Search products by year, make, and model with supplier availability (with optional supplier details)
     */
    @Transactional
    public List<ProductResponse> searchProductsByYearMakeModel(Integer year, String make, String model, boolean includeSuppliers) {
        List<Product> products = productRepository.findByYearAndMakeAndModel(year != null ? year.toString() : null, make, model);
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

        // Step 1: Build specification from request
        Specification<Product> spec = ProductSpecification.bySearchRequest(request);

        // Step 2: Build sort
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());

        // Step 3: Execute query with specification and sorting (sort is always non-null)
        List<Product> products = productRepository.findAll(spec, sort);

        // Step 4: Enrich with supplier counts and optionally supplier details
        List<ProductResponse> enrichedProducts = enrichProductsWithSupplierCounts(products, false, includeSuppliers);

        // Step 3: Apply supplier count filters if specified
        if (request.getInStockOnly() != null && request.getInStockOnly()) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() > 0)
                    .toList();
        }

        if (request.getMinSuppliers() != null) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() >= request.getMinSuppliers())
                    .toList();
        }

        if (request.getMaxSuppliers() != null) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() <= request.getMaxSuppliers())
                    .toList();
        }

        // Step 4: Sort by supplier count if requested
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

        // Build specification from request
        Specification<Product> spec = ProductSpecification.bySearchRequest(searchRequest);

        // Build Pageable with sorting
        Pageable pageable = buildPageable(searchRequest);

        // Execute paginated query with specification
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // Convert to ProductResponse (supplier count already in entity)
        List<ProductResponse> responses = productPage.getContent().stream()
                .map(product -> buildProductResponse(product, includeSuppliers))
                .toList();

        // Apply supplier filters if specified
        if (searchRequest.getMinSuppliers() != null ||
                searchRequest.getMaxSuppliers() != null ||
                Boolean.TRUE.equals(searchRequest.getInStockOnly())) {
            responses = filterBySupplierCriteria(responses, searchRequest);
        }

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
    private ProductResponse buildProductResponse(Product product, boolean includeSuppliers) {
        String attributeUrl = "http://localhost:8060/api/v1/productattributes/productid/" + product.getId();

        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .partNumber(product.getPartNumber())
                .year(product.getYear())
                .make(product.getMake())
                .model(product.getModel())
                .tag(product.getTag())
                .otherNames(product.getOtherNames())
                .categoryId(product.getCategoryId())
                .manufacturerId(product.getManufacturerId())
                .attributeUrl(attributeUrl)
                .images(product.getImages())
                // Use pre-computed supplier count from database
                .availableSupplierCount(product.getSupplierCount() != null
                        ? product.getSupplierCount().longValue()
                        : 0L);

        // Include detailed suppliers if requested
        if (includeSuppliers) {
            List<SupplierAvailabilityResponse> suppliers =
                    supplierService.getSuppliersByProductId(product.getId());
            builder.suppliers(suppliers);
        }

        return builder.build();
    }

    /**
     * Filter products by supplier criteria
     */
    private List<ProductResponse> filterBySupplierCriteria(
            List<ProductResponse> responses,
            ProductSearchRequest request) {

        return responses.stream()
                .filter(p -> {
                    Long count = p.getAvailableSupplierCount();

                    if (request.getMinSuppliers() != null && count < request.getMinSuppliers()) {
                        return false;
                    }
                    if (request.getMaxSuppliers() != null && count > request.getMaxSuppliers()) {
                        return false;
                    }

                    return !Boolean.TRUE.equals(request.getInStockOnly()) || count > 0;
                })
                .toList();
    }
}