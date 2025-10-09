package com.autoadda.apis.part.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class CategoryAttributeRequest {

    private String id;
    private String categoryId;
    private String attributeId;
}
