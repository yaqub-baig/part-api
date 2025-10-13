package com.autoadda.apis.part.request;

import com.autoadda.apis.part.entity.Category;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<Category> subCategories = new HashSet<> ( );
    private List<Long> imageIds = new ArrayList<> ( );
}
