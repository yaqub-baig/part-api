package com.autoadda.apis.part.specification;

import com.autoadda.apis.part.request.ProductSearchRequest;

import java.util.Set;

/**
 * Vehicle part of a product search, by id: make, any of these models, and a model year.
 * Null / empty parts are not filtered on.
 */
public record VehicleFilter(String makeId, Set<String> modelIds, Integer year) {

    public VehicleFilter {
        makeId = (makeId == null || makeId.isBlank()) ? null : makeId.trim();
        modelIds = modelIds == null ? Set.of() : Set.copyOf(modelIds);
    }

    /** Uses only the id fields of the request (names must be resolved first, see VehicleFitResolver). */
    public static VehicleFilter fromIds(ProductSearchRequest request) {
        String modelId = request.getModelId();
        return new VehicleFilter(request.getMakeId(),
                modelId == null || modelId.isBlank() ? Set.of() : Set.of(modelId.trim()),
                request.getYear());
    }
}
