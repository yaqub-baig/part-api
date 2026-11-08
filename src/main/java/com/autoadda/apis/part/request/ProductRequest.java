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
    /** Legacy text: comma-separated years, e.g. "2018, 2019" (ranges like "2018-2020" also accepted). */
    private String year;
    /** Legacy text: make name. Ignored when makeId is sent. */
    private String make;
    /** Legacy text: comma-separated model names of that make. Ignored when modelIds is sent. */
    private String model;
    /** Vehicle fit by id (preferred). */
    private String makeId;
    private List<String> modelIds;
    private List<Integer> years;
    private String tag;
    private String otherNames;
    private String categoryId;
    private String manufacturerId;
    private List<ProductAttributeRequest> attributes;
    @Builder.Default
    private List<Long> imageIds = new ArrayList<> ( );

}
