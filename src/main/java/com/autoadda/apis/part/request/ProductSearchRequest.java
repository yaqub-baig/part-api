package com.autoadda.apis.part.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for advanced product search with multiple filters")
public class ProductSearchRequest {

    // Basic search filters
    @Schema(description = "Product name (partial match)", example = "Brake Pad")
    private String name;

    @Schema(description = "Part number (exact match)", example = "BP-123456")
    private String partNumber;

    @Schema(description = "Alternative product names (partial match)", example = "brake shoe")
    private String otherNames;

    @Schema(description = "Product tag (partial match)", example = "performance")
    private String tag;

    // Category and manufacturer filters
    @Schema(description = "Category ID", example = "CAT-001")
    private String categoryId;

    @Schema(description = "Manufacturer ID", example = "MFG-001")
    private String manufacturerId;

    // Vehicle compatibility filters
    @Schema(description = "Vehicle year", example = "2020")
    private Integer year;

    @Schema(description = "Vehicle make", example = "Honda")
    private String make;

    @Schema(description = "Vehicle model", example = "Civic")
    private String model;

    // Supplier availability filters
    @Schema(description = "Minimum number of suppliers that must have the product in stock", example = "1")
    private Integer minSuppliers;

    @Schema(description = "Maximum number of suppliers", example = "10")
    private Integer maxSuppliers;

    @Schema(description = "Only show products that are in stock with at least one supplier", defaultValue = "false")
    private Boolean inStockOnly;

    // Sorting options
    @Schema(description = "Sort by field: 'name', 'partNumber', 'year', 'supplierCount'", example = "name")
    private String sortBy;

    @Schema(description = "Sort direction: 'ASC' or 'DESC'", example = "ASC")
    private String sortDirection;

    // Pagination options
    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    @Min(0)
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Page size", example = "10", defaultValue = "10")
    @Min(1)
    @Max(50)
    @Builder.Default
    private Integer size = 10;
}