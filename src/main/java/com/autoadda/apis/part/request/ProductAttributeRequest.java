package com.autoadda.apis.part.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class ProductAttributeRequest {

    private String id;

    private String attributeValue;

    private String productId;

    private String attributeId;
}
