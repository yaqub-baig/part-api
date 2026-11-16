package com.autoadda.apis.part.response;

import com.autoadda.apis.part.util.EnquiryRecipientStatus;
import com.autoadda.apis.part.util.EnquiryStatus;
import com.autoadda.apis.part.util.ProductCondition;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * An enquiry as the customer sees it (with all quotes) or as a shop sees it (with its own status and quote;
 * vehicleOwnerId only once the customer accepted that shop's quote).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartEnquiryResponse {
    private Integer id;
    private EnquiryStatus status;
    private String makeId;
    private String makeName;
    private String modelId;
    private String modelName;
    private Integer year;
    private String categoryId;
    private String categoryName;
    private String productId;
    private String partNumber;
    private String description;
    private List<ProductCondition> conditions;
    private List<Image> images;
    private Integer recipientCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime closedAt;

    // customer view
    private List<PartQuoteResponse> quotes;
    private Integer acceptedQuoteId;

    // shop view
    private EnquiryRecipientStatus myStatus;
    private PartQuoteResponse myQuote;
    private Integer vehicleOwnerId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        private Long id;
        /** Download with GET /api/v1/uploads/{filePath}. */
        private String filePath;
    }
}
