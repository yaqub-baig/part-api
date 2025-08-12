package com.autoadda.apis.part.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.part.entity.CategoryAttribute;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, String> {

	CategoryAttribute findByCategoryIdAndAttributeId(String categoryId, String attributeId);

	List<CategoryAttribute> findByCategoryId(String categoryId);

}