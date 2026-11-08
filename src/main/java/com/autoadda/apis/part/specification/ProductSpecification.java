package com.autoadda.apis.part.specification;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.request.ProductSearchRequest;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification for dynamic Product queries
 * Provides type-safe query building from ProductSearchRequest
 */
public class ProductSpecification {

    // Field name constants to avoid string duplication
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PART_NUMBER = "partNumber";
    private static final String FIELD_OTHER_NAMES = "otherNames";
    private static final String FIELD_TAG = "tag";
    private static final String FIELD_CATEGORY_ID = "categoryId";
    private static final String FIELD_MANUFACTURER_ID = "manufacturerId";
    private static final String FIELD_YEAR = "year";
    private static final String FIELD_MAKE = "make";
    private static final String FIELD_MODEL = "model";

    // Private constructor to prevent instantiation
    private ProductSpecification() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Build a dynamic JPA Specification from ProductSearchRequest
     *
     * @param request the search request containing filter criteria
     * @return Specification that can be used with JpaSpecificationExecutor
     */
    public static Specification<Product> bySearchRequest(ProductSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Add all filter predicates
            addPartialMatchFilters(request, root, criteriaBuilder, predicates);
            addExactMatchFilters(request, root, criteriaBuilder, predicates);
            addConditionFilter(request, root, query, criteriaBuilder, predicates);

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Add partial match filters with case-insensitive search
     */
    private static void addPartialMatchFilters(
            ProductSearchRequest request,
            Root<Product> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        // Name filter (partial match, case-insensitive)
        addCaseInsensitiveLikeFilter(
                request.getName(), FIELD_NAME, root, criteriaBuilder, predicates);

        // Other names filter (partial match, case-insensitive)
        addCaseInsensitiveLikeFilter(
                request.getOtherNames(), FIELD_OTHER_NAMES, root, criteriaBuilder, predicates);

        // Tag filter (partial match, case-insensitive)
        addCaseInsensitiveLikeFilter(
                request.getTag(), FIELD_TAG, root, criteriaBuilder, predicates);
    }

    /**
     * Add exact match filters
     */
    private static void addExactMatchFilters(
            ProductSearchRequest request,
            Root<Product> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        // Part number filter (exact match)
        addEqualFilter(request.getPartNumber(), FIELD_PART_NUMBER, root, criteriaBuilder, predicates);

        // Category filter (exact match)
        addEqualFilter(request.getCategoryId(), FIELD_CATEGORY_ID, root, criteriaBuilder, predicates);

        // Manufacturer filter (exact match)
        addEqualFilter(request.getManufacturerId(), FIELD_MANUFACTURER_ID, root, criteriaBuilder, predicates);

        // Make filter (exact match)
        addEqualFilter(request.getMake(), FIELD_MAKE, root, criteriaBuilder, predicates);

        // Model filter (exact match)
        addEqualFilter(request.getModel(), FIELD_MODEL, root, criteriaBuilder, predicates);

        // Year filter (exact match, convert to string)
        if (request.getYear() != null) {
            predicates.add(criteriaBuilder.equal(
                root.get(FIELD_YEAR),
                request.getYear().toString()
            ));
        }
    }

    /**
     * Part condition lives on InventoryPart (each shop's listing), not on Product.
     * Keep only products that have at least one active, in-stock listing in one of the requested conditions:
     * EXISTS (SELECT 1 FROM InventoryPart ip WHERE ip.productId = p.id AND ip.isActive = true
     *         AND ip.quantityInStock > 0 AND ip.productCondition IN (:conditions))
     * Done in the query (not after loading) so pagination totals stay correct.
     */
    private static void addConditionFilter(
            ProductSearchRequest request,
            Root<Product> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (request.getConditions() == null || request.getConditions().isEmpty() || query == null) {
            return;
        }
        Subquery<Integer> listing = query.subquery(Integer.class);
        Root<InventoryPart> ip = listing.from(InventoryPart.class);
        listing.select(ip.get("id")).where(
                criteriaBuilder.equal(ip.get("productId"), root.get("id")),
                criteriaBuilder.isTrue(ip.get("isActive")),
                criteriaBuilder.greaterThan(ip.get("quantityInStock"), 0),
                ip.get("productCondition").in(request.getConditions())
        );
        predicates.add(criteriaBuilder.exists(listing));
    }

    /**
     * Helper method to add case-insensitive LIKE filter
     */
    private static void addCaseInsensitiveLikeFilter(
            String value,
            String fieldName,
            Root<Product> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (value != null && !value.isBlank()) {
            predicates.add(criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + value.toLowerCase() + "%"
            ));
        }
    }

    /**
     * Helper method to add EQUAL filter for string fields
     */
    private static void addEqualFilter(
            String value,
            String fieldName,
            Root<Product> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        if (value != null && !value.isBlank()) {
            predicates.add(criteriaBuilder.equal(
                root.get(fieldName),
                value
            ));
        }
    }
}
