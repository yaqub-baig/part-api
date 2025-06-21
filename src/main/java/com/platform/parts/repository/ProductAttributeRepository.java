package com.platform.parts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.parts.entity.ProductAttribute;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, String> {

	List<ProductAttribute> findByProductId(String productId);

	ProductAttribute findByAttributeValue(String value);
}
