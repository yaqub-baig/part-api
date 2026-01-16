package com.autoadda.apis.part.specification;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.request.InventoryPartSearchRequest;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification for dynamic InventoryPart queries
 * Provides type-safe query building from InventoryPartSearchRequest
 */
public class InventoryPartSpecification {

    // Field name constants to avoid string duplication
    private static final String FIELD_PART_NUMBER = "partNumber";
    private static final String FIELD_PART_NAME = "partName";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_MANUFACTURER = "manufacturer";
    private static final String FIELD_PRODUCT_ID = "productId";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_PRODUCT_CONDITION = "productCondition";
    private static final String FIELD_SUPPLIER_ID = "supplierId";
    private static final String FIELD_UNIT_PRICE = "unitPrice";
    private static final String FIELD_QUANTITY_IN_STOCK = "quantityInStock";
    private static final String FIELD_IS_ACTIVE = "isActive";

    // Private constructor to prevent instantiation
    private InventoryPartSpecification() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Build a dynamic JPA Specification from InventoryPartSearchRequest
     *
     * @param request the search request containing filter criteria
     * @param inStockOnly whether to filter for items with stock > 0
     * @param activeOnly whether to filter for active items only
     * @return Specification that can be used with JpaSpecificationExecutor
     */
    public static Specification<InventoryPart> bySearchRequest(
            InventoryPartSearchRequest request,
            Boolean inStockOnly,
            Boolean activeOnly) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Add all filter predicates
            addStringFilters(request, root, criteriaBuilder, predicates);
            addPriceFilters(request, root, criteriaBuilder, predicates);
            addQuantityFilters(request, root, criteriaBuilder, predicates);
            addStatusFilters(inStockOnly, activeOnly, root, criteriaBuilder, predicates);

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Add string-based filters (partial and exact matches)
     */
    private static void addStringFilters(
            InventoryPartSearchRequest request,
            Root<InventoryPart> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        // Partial match filters
        addLikeFilter(request.getPartNumber(), FIELD_PART_NUMBER, root, criteriaBuilder, predicates);
        addLikeFilter(request.getPartName(), FIELD_PART_NAME, root, criteriaBuilder, predicates);
        addLikeFilter(request.getLocation(), FIELD_LOCATION, root, criteriaBuilder, predicates);

        // Exact match filters
        addEqualFilter(request.getCategory(), FIELD_CATEGORY, root, criteriaBuilder, predicates);
        addEqualFilter(request.getManufacturer(), FIELD_MANUFACTURER, root, criteriaBuilder, predicates);
        addEqualFilter(request.getProductId(), FIELD_PRODUCT_ID, root, criteriaBuilder, predicates);
        addEqualFilter(request.getSupplierId(), FIELD_SUPPLIER_ID, root, criteriaBuilder, predicates);

        // Product condition filter
        if (request.getProductCondition() != null) {
            predicates.add(criteriaBuilder.equal(
                root.get(FIELD_PRODUCT_CONDITION),
                request.getProductCondition()
            ));
        }
    }

    /**
     * Add price range filters
     */
    private static void addPriceFilters(
            InventoryPartSearchRequest request,
            Root<InventoryPart> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (request.getMinPrice() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                root.get(FIELD_UNIT_PRICE),
                request.getMinPrice()
            ));
        }

        if (request.getMaxPrice() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(
                root.get(FIELD_UNIT_PRICE),
                request.getMaxPrice()
            ));
        }
    }

    /**
     * Add quantity filters
     */
    private static void addQuantityFilters(
            InventoryPartSearchRequest request,
            Root<InventoryPart> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (request.getMinQuantity() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                root.get(FIELD_QUANTITY_IN_STOCK),
                request.getMinQuantity()
            ));
        }
    }

    /**
     * Add status filters (in stock and active)
     */
    private static void addStatusFilters(
            Boolean inStockOnly,
            Boolean activeOnly,
            Root<InventoryPart> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (Boolean.TRUE.equals(inStockOnly)) {
            predicates.add(criteriaBuilder.greaterThan(
                root.get(FIELD_QUANTITY_IN_STOCK),
                0
            ));
        }

        if (Boolean.TRUE.equals(activeOnly)) {
            predicates.add(criteriaBuilder.equal(
                root.get(FIELD_IS_ACTIVE),
                true
            ));
        }
    }

    /**
     * Helper method to add LIKE filter for string fields
     */
    private static void addLikeFilter(
            String value,
            String fieldName,
            Root<InventoryPart> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (value != null && !value.isBlank()) {
            predicates.add(criteriaBuilder.like(
                root.get(fieldName),
                "%" + value + "%"
            ));
        }
    }

    /**
     * Helper method to add EQUAL filter for string fields
     */
    private static void addEqualFilter(
            String value,
            String fieldName,
            Root<InventoryPart> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (value != null && !value.isBlank()) {
            predicates.add(criteriaBuilder.equal(
                root.get(fieldName),
                value
            ));
        }
    }

    /**
     * Simplified specification for filtering by product ID and status
     * Used when you don't have a request object but need to filter by booleans
     */
    public static Specification<InventoryPart> byProductIdAndStatus(
            String productId,
            Boolean inStockOnly,
            Boolean activeOnly) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Product ID filter
            if (productId != null && !productId.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                    root.get(FIELD_PRODUCT_ID),
                    productId
                ));
            }

            // Status filters
            addStatusFilters(inStockOnly, activeOnly, root, criteriaBuilder, predicates);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
