package com.autoadda.apis.part.response;

import java.util.HashSet;
import java.util.Set;

import com.autoadda.apis.part.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
	private Set<Category> subCategories = new HashSet<>();
	private String attributeUrl;
}
