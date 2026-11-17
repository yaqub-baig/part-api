package com.autoadda.apis.part.specification;

import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.util.ProductCondition;
import com.autoadda.apis.part.util.Status;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Shops whose coverage matches an enquiry: active, not blocked, covers the make, and the model / category /
 * condition / model year (a shop with none of those set covers them all).
 */
public final class ShopMatchSpecification {

    /** Account states that never receive enquiries. Shops are created PENDING and stay so today, so PENDING is allowed. */
    public static final Set<Status> EXCLUDED_STATUSES = Set.of(
            Status.REJECTED, Status.BLOCKED, Status.SUSPENDED, Status.DEACTIVATED, Status.DELETED, Status.INACTIVE);

    private ShopMatchSpecification() {
    }

    /**
     * @param makeId     required
     * @param modelId    optional
     * @param categoryIds the enquiry's category and its ancestors; empty = no category given
     * @param conditions conditions the customer accepts; empty = any
     * @param year       the customer's model year, optional; matched against the shop's yearFrom / yearTo
     *                   (a shop without a range covers all years)
     */
    public static Specification<Supplier> covering(String makeId, String modelId, Collection<String> categoryIds,
                                                   Collection<ProductCondition> conditions, Integer year) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            predicates.add(cb.or(root.get("accountStatus").isNull(),
                    cb.not(root.get("accountStatus").in(EXCLUDED_STATUSES))));

            Expression<Set<String>> makeIds = root.get("makeIds");
            predicates.add(cb.isMember(makeId, makeIds));

            if (modelId != null && !modelId.isBlank()) {
                Expression<Set<String>> modelIds = root.get("modelIds");
                predicates.add(cb.or(cb.isEmpty(modelIds), cb.isMember(modelId, modelIds)));
            }
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Expression<Set<String>> shopCategories = root.get("categoryIds");
                predicates.add(anyMemberOrEmpty(cb, shopCategories, categoryIds));
            }
            if (conditions != null && !conditions.isEmpty()) {
                Expression<Set<ProductCondition>> shopConditions = root.get("conditions");
                predicates.add(anyMemberOrEmpty(cb, shopConditions, conditions));
            }
            if (year != null) {
                predicates.add(cb.or(root.get("yearFrom").isNull(),
                        cb.lessThanOrEqualTo(root.get("yearFrom"), year)));
                predicates.add(cb.or(root.get("yearTo").isNull(),
                        cb.greaterThanOrEqualTo(root.get("yearTo"), year)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static <T> Predicate anyMemberOrEmpty(CriteriaBuilder cb, Expression<Set<T>> collection, Collection<T> values) {
        List<Predicate> options = new ArrayList<>();
        options.add(cb.isEmpty(collection));
        values.stream().sorted((a, b) -> a.toString().compareTo(b.toString()))
                .forEach(value -> options.add(cb.isMember(value, collection)));
        return cb.or(options.toArray(new Predicate[0]));
    }
}
