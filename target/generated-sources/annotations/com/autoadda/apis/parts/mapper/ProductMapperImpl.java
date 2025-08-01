package com.autoadda.apis.parts.mapper;

import com.autoadda.apis.parts.entity.Product;
import com.autoadda.apis.parts.entity.ProductAttribute;
import com.autoadda.apis.parts.request.ProductAttributeRequest;
import com.autoadda.apis.parts.request.ProductRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-01T15:05:35+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toProduct(ProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product product = new Product();

        return product;
    }

    @Override
    public List<Product> toProductList(List<ProductRequest> requests) {
        if ( requests == null ) {
            return null;
        }

        List<Product> list = new ArrayList<Product>( requests.size() );
        for ( ProductRequest productRequest : requests ) {
            list.add( toProduct( productRequest ) );
        }

        return list;
    }

    @Override
    public ProductAttribute toProductAttribute(ProductAttributeRequest request) {
        if ( request == null ) {
            return null;
        }

        ProductAttribute productAttribute = new ProductAttribute();

        return productAttribute;
    }

    @Override
    public List<ProductAttribute> toProductAttributeList(List<ProductAttributeRequest> requests) {
        if ( requests == null ) {
            return null;
        }

        List<ProductAttribute> list = new ArrayList<ProductAttribute>( requests.size() );
        for ( ProductAttributeRequest productAttributeRequest : requests ) {
            list.add( toProductAttribute( productAttributeRequest ) );
        }

        return list;
    }
}
