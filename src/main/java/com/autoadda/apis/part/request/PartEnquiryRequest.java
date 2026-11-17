package com.autoadda.apis.part.request;

import com.autoadda.apis.part.util.ProductCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** A customer's part enquiry. makeId is required; describe the part with productId, partNumber and/or description. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartEnquiryRequest {
    private String makeId;
    private String modelId;
    private Integer year;
    private String categoryId;
    private String productId;
    private String partNumber;
    private String description;
    private List<ProductCondition> conditions;
    private List<Long> imageIds;
    /**
     * Garage enquiries only: the garage part request item this is for. Vehicle, part number and description
     * are filled from it when not sent.
     */
    private Integer partRequestItemId;
}
