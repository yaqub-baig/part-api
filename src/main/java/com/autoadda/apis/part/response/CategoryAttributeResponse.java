package com.autoadda.apis.part.response;

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
public class CategoryAttributeResponse {

	private String id;
	private String categoryId;
	private String attributeId;
	private String attributeUrl;
}
