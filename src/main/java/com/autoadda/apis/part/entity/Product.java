package com.autoadda.apis.part.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class Product {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "price")
	private Double price;

	@Column(name = "stock")
	private Integer stock;

	@Column(name = "sku")
	private String sku;

	@Column(name = "part_number")
	private String partNumber;

	@Column(name = "tag")
	private String tag;

	@Column(name = "other_names")
	private String otherNames;

	@Column(name = "part_condition")
	private String partCondition;

	@Column(name = "year")
	private Integer year;

	@Column(name = "make")
	private String make;

	@Column(name = "model")
	private String model;

	@Column(name = "category_id")
	private String categoryId;

	@Column(name = "manufacturer_id")
	private String manufacturerId;

	@Column(name = "supplier_id")
	private String supplierId;

	@Column(name = "image_id")
	private String imageId;
}
