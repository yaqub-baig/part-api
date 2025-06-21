package com.platform.parts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_attribute")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class ProductAttribute {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "attribute_value")
	private String attributeValue;

//    @ManyToOne
	@Column(name = "product_id")
	private String productId;

//    @ManyToOne
	@Column(name = "attribute_id")
	private String attributeId;
}
