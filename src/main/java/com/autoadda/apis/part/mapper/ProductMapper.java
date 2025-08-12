package com.autoadda.apis.part.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.ProductAttribute;
import com.autoadda.apis.part.request.ProductAttributeRequest;
import com.autoadda.apis.part.request.ProductRequest;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toProduct(ProductRequest request);

    List<Product> toProductList(List<ProductRequest> requests);

    ProductAttribute toProductAttribute(ProductAttributeRequest request);

    List<ProductAttribute> toProductAttributeList(List<ProductAttributeRequest> requests);
}
