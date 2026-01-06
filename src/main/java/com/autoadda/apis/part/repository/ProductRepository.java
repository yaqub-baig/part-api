package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    Product findByName(String name);

    List<Product> findByNameContaining(String name);

    List<Product> findByOtherNamesContaining(String name);

    List<Product> findByCategoryId(String categoryId);

    List<Product> findByTagContaining(String tag);


    List<Product> findByYearAndMakeAndModel(Integer year, String make, String model);

    List<Product> findByManufacturerId(String manufacturerId);

    Product findByPartNumber(String partNumber);
}
