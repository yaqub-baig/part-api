package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, String> {

    List<ProductAttribute> findByProductId(String productId);

    ProductAttribute findByAttributeValue(String value);

    void deleteByProductId(String productId);
}
