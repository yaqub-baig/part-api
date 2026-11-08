package com.autoadda.apis.part.response;

import com.autoadda.apis.part.entity.MediaFile;
import com.autoadda.apis.part.entity.Product;
import lombok.*;

import java.util.Collection;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data

public class ProductResponse {

    private String id;
    private String description;
    private String name;

    private String partNumber;
    private String year;
    private String make;
    private String model;
    /** Vehicle fit by id: make, its models and the model years this part fits. */
    private String makeId;
    private List<String> modelIds;
    private List<Integer> years;
    private String tag;
    private String otherNames;
    private String categoryId;
    private String manufacturerId;
    private String attributeUrl;
    private CategoryResponse category;
    private List<ProductAttributeResponse> attributes;
    private List<MediaFile> images;

    // Supplier availability information
    private Long availableSupplierCount; // Number of suppliers with this product in stock
    private List<SupplierAvailabilityResponse> suppliers; // Optional: detailed supplier list with pricing and inventory

    public static class ProductResponseBuilder {
        /** Copies the product's vehicle fit (make id, sorted model ids and years). */
        public ProductResponseBuilder vehicleFit(Product product) {
            return makeId(product.getMakeId())
                    .modelIds(sorted(product.getModelIds()))
                    .years(sorted(product.getYears()));
        }

        private static <T extends Comparable<T>> List<T> sorted(Collection<T> values) {
            return values == null ? List.of() : values.stream().sorted().toList();
        }
    }
}
