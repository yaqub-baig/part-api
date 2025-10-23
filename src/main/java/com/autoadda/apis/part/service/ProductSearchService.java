package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.ProductAttribute;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.request.ProductSearchRequest;
import com.autoadda.apis.part.response.ProductResponse;
import com.autoadda.apis.part.response.SupplierAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
                    attributesResponse != null ? (List<ProductAttribute>) attributesResponse : new ArrayList<>()
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
        List<Product> products = productRepository.findByYearAndMakeAndModel(year, make, model);
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

        // Step 1: Search products using repository query
        List<Product> products = productRepository.advancedSearch(
                request.getName(),
                request.getPartNumber(),
                request.getOtherNames(),
                request.getTag(),
                request.getCategoryId(),
                request.getManufacturerId(),
                request.getYear(),
                request.getMake(),
                request.getModel(),
                request.getSortBy(),
                request.getSortDirection()
        );

        // Step 2: Enrich with supplier counts
        List<ProductResponse> enrichedProducts = enrichProductsWithSupplierCounts(products, false);

        // Step 3: Apply supplier count filters if specified
        if (request.getInStockOnly() != null && request.getInStockOnly()) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() > 0)
                    .collect(Collectors.toList());
        }

        if (request.getMinSuppliers() != null) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() >= request.getMinSuppliers())
                    .collect(Collectors.toList());
        }

        if (request.getMaxSuppliers() != null) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() <= request.getMaxSuppliers())
                    .collect(Collectors.toList());
        }

        // Step 4: Sort by supplier count if requested
        if ("supplierCount".equals(request.getSortBy())) {
            enrichedProducts = enrichedProducts.stream()
                    .sorted((p1, p2) -> {
                        int comparison = p1.getAvailableSupplierCount().compareTo(p2.getAvailableSupplierCount());
                        return "DESC".equalsIgnoreCase(request.getSortDirection()) ? -comparison : comparison;
                    })
                    .collect(Collectors.toList());
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
        List<Product> products = productRepository.findByYearAndMakeAndModel(year, make, model);
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

        // Step 1: Search products using repository query
        List<Product> products = productRepository.advancedSearch(
                request.getName(),
                request.getPartNumber(),
                request.getOtherNames(),
                request.getTag(),
                request.getCategoryId(),
                request.getManufacturerId(),
                request.getYear(),
                request.getMake(),
                request.getModel(),
                request.getSortBy(),
                request.getSortDirection()
        );

        // Step 2: Enrich with supplier counts and optionally supplier details
        List<ProductResponse> enrichedProducts = enrichProductsWithSupplierCounts(products, false, includeSuppliers);

        // Step 3: Apply supplier count filters if specified
        if (request.getInStockOnly() != null && request.getInStockOnly()) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() > 0)
                    .collect(Collectors.toList());
        }

        if (request.getMinSuppliers() != null) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() >= request.getMinSuppliers())
                    .collect(Collectors.toList());
        }

        if (request.getMaxSuppliers() != null) {
            enrichedProducts = enrichedProducts.stream()
                    .filter(p -> p.getAvailableSupplierCount() <= request.getMaxSuppliers())
                    .collect(Collectors.toList());
        }

        // Step 4: Sort by supplier count if requested
        if ("supplierCount".equals(request.getSortBy())) {
            enrichedProducts = enrichedProducts.stream()
                    .sorted((p1, p2) -> {
                        int comparison = p1.getAvailableSupplierCount().compareTo(p2.getAvailableSupplierCount());
                        return "DESC".equalsIgnoreCase(request.getSortDirection()) ? -comparison : comparison;
                    })
                    .collect(Collectors.toList());
        }

        log.info("Advanced search returned {} products", enrichedProducts.size());
        return enrichedProducts;
    }
}