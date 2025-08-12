package com.autoadda.apis.part.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.part.entity.ProductAttribute;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, String> {

	List<ProductAttribute> findByProductId(String productId);

	ProductAttribute findByAttributeValue(String value);

	void deleteByProductId(String productId);
}
