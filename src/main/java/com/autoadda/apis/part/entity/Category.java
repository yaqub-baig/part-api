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
@Table(name = "category")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class Category {
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "name")
	private String name;

	@Column(name = "code")
	private String code;

//	@ManyToOne
	@Column(name = "parent_category_id")
	private String parentCategoryId;

//	@OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
//	private Set<PartCategory> subCategories;

}
