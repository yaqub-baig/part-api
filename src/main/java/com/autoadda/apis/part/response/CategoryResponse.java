package com.autoadda.apis.part.response;

import com.autoadda.apis.part.entity.MediaFile;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

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
    private List<CategoryResponse> subCategories = new ArrayList<>();
    private String attributeUrl;
    private List<MediaFile> images = new ArrayList<>();
}
