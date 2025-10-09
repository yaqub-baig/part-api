package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, String> {

    CategoryAttribute findByCategoryIdAndAttributeId(String categoryId, String attributeId);

    List<CategoryAttribute> findByCategoryId(String categoryId);

}