package com.autoadda.apis.part.response;

import com.autoadda.apis.part.entity.MediaFile;
import lombok.*;

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
}
