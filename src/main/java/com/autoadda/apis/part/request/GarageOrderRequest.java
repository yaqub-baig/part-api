package com.autoadda.apis.part.request;

import com.autoadda.apis.part.util.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** A garage order: listings to buy, each optionally for a part request item of a job card. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageOrderRequest {
    /** COD (default) or ONLINE. */
    private PaymentMethod paymentMethod;
    private String notes;
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        /** The listing (offer) to buy: inventoryPartId from search or from an accepted quote. */
        private Integer inventoryPartId;
        /** Defaults to the part request item's quantity when ordering for a job. */
        private Integer quantity;
        /** The garage part request item (job card) this is for; the car comes from it. */
        private Integer partRequestItemId;
        /** Standalone purchases: the car, optional. Ignored when partRequestItemId is sent. */
        private String makeId;
        private String modelId;
        private Integer year;
    }
}
