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
