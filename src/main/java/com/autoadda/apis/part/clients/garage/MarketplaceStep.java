package com.autoadda.apis.part.clients.garage;

import com.autoadda.apis.part.util.ProductCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** garage-api POST /part-requests/item/{id}/marketplace body. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceStep {

    public enum Stage { ORDERED, CANCELLED, RECEIVED }

    private Stage stage;
    private Integer orderId;
    private Long orderItemId;
    private String supplierName;
    private String supplierContact;
    private Double unitPrice;
    /** Same names as garage-api's PartCondition. */
    private ProductCondition condition;
}
