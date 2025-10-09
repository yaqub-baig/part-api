package com.autoadda.apis.part.response;

import com.autoadda.apis.part.entity.Category;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class CategoryResponse {
    private String id;
    private String name;
    private String code;
    private String parentCategoryId;
    private Set<Category> subCategories = new HashSet<> ( );
    private String attributeUrl;
}
