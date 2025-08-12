package com.autoadda.apis.part.request;

import java.util.List;

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

public class ProductRequest {

	private String id;
	private String description;
	private String name;
	private String sku;
	private String partNumber;
	private Integer year;
	private String make;
	private String model;
	private Double price;
	private Integer stock;
	private String partCondition;
	private String tag;
	private String otherNames;
	private String categoryId;
	private String imageId;
	private String supplierId;
	private String manufacturerId;
	private List<ProductAttributeRequest> attributes;
	
}
