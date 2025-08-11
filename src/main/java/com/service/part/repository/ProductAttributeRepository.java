package com.service.part.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.part.entity.ProductAttribute;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, String> {

	List<ProductAttribute> findByProductId(String productId);

	ProductAttribute findByAttributeValue(String value);

	void deleteByProductId(String productId);
}
