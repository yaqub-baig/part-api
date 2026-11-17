package com.autoadda.apis.part.clients.garage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** garage-api GET /part-requests/item/{id}/purchase-context: a part request item with its job and car. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaragePartContext {
    private Integer garageId;
    private Integer workOrderId;
    private Integer jobCardId;
    private Integer partRequestId;
    private Integer itemId;
    private String partName;
    private String partNumber;
    private Integer quantity;
    private String description;
    private String partRequestStatus;
    private String procurementStatus;
    private boolean purchasable;
    private Integer marketplaceOrderId;
    private Long marketplaceOrderItemId;
    private Integer vehicleId;
    private String brand;
    private String model;
    private String year;
}
