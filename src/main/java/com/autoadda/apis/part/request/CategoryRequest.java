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
public class CategoryRequest {

    private String id;
    private String name;
    private String code;
    private String parentCategoryId;
    @Builder.Default
    private List<Long> imageIds = new ArrayList<> ( );
}
