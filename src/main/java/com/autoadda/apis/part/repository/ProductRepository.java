package com.autoadda.apis.part.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.part.entity.Product;

public interface ProductRepository extends JpaRepository<Product, String> {

	Product findByName(String name);

	List<Product> findByNameContaining(String name);

	List<Product> findByOtherNamesContaining(String name);

	List<Product> findByCategoryId(String categoryId);

	List<Product> findByTagContaining(String tag);

	List<Product> findBySkuContaining(String sku);

	List<Product> findByYearAndMakeAndModel(Integer year, String make, String model);

	List<Product> findBySupplierId(String supplierId);
	
	List<Product> findByManufacturerId(String manufacturerId);

	List<Product> findByPartNumber(String partNumber);
}
