package com.autoadda.apis.part.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.part.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, String> {

	Category findByName(String name);

	List<Category> findByParentCategoryId(String parentCategoryId);

	List<Category> findByParentCategoryIdIsNull();
}