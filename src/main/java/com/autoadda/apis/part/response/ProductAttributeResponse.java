package com.autoadda.apis.part.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data

public class ProductAttributeResponse {

    private String id;
    private String attributeValue;
    private String attributeId;
    private String productId;
    private String attributeUrl;
}
