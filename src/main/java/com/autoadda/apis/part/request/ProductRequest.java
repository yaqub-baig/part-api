package com.autoadda.apis.part.request;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data

public class ProductRequest {

    private String id;
    private String description;
    private String name;
    private String partNumber;
    private String year;
    private String make;
    private String model;
    private String tag;
    private String otherNames;
    private String categoryId;
    private String manufacturerId;
    private List<ProductAttributeRequest> attributes;
    private List<Long> imageIds = new ArrayList<> ( );

}
