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
@Table(name = "category_attribute")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class CategoryAttribute {

	@Id
	@Column(name = "id")
	private String id;

//	@ManyToOne
	@Column(name = "category_id")
	private String categoryId;

//    @ManyToOne
	@Column(name = "attribute_id")
	private String attributeId;
}
