package com.autoadda.apis.part.request;

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
public class ProductAttributeRequest {

	private String id;

	private String attributeValue;

	private String productId;

	private String attributeId;
}
