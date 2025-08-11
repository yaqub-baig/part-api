package com.service.part.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.service.part.entity.Product;
import com.service.part.entity.ProductAttribute;
import com.service.part.request.ProductAttributeRequest;
import com.service.part.request.ProductRequest;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toProduct(ProductRequest request);

    List<Product> toProductList(List<ProductRequest> requests);

    ProductAttribute toProductAttribute(ProductAttributeRequest request);

    List<ProductAttribute> toProductAttributeList(List<ProductAttributeRequest> requests);
}
