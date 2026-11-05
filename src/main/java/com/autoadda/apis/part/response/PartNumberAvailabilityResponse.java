package com.autoadda.apis.part.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartNumberAvailabilityResponse {
    /** The checked value, trimmed (this is what will be stored). */
    private String partNumber;
    private boolean available;
    /** Set when taken, so the form can link to the existing product. */
    private String existingProductId;
    /** Ready-to-show message for the form field. */
    private String message;
}
