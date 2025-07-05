package com.platform.parts.entity;

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
