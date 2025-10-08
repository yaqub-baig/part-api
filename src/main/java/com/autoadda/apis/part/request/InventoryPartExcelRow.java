package com.autoadda.apis.part.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class InventoryPartExcelRow {

    private Integer rowNumber;
    private String partNumber;
    private String sku;
    private Integer quantity;
    private Double pricePerPiece;
    private String condition; // Used, New, AfterMarket

    // Validation errors for this row
    private String errorMessage;
    private Boolean isValid;
}