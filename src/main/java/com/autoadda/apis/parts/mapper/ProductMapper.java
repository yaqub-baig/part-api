package com.autoadda.apis.parts.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.autoadda.apis.parts.entity.Product;
import com.autoadda.apis.parts.entity.ProductAttribute;
import com.autoadda.apis.parts.request.ProductAttributeRequest;
import com.autoadda.apis.parts.request.ProductRequest;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toProduct(ProductRequest request);

    List<Product> toProductList(List<ProductRequest> requests);

    ProductAttribute toProductAttribute(ProductAttributeRequest request);

    List<ProductAttribute> toProductAttributeList(List<ProductAttributeRequest> requests);
}
