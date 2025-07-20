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
    date = "2025-07-19T20:55:41-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 23.0.2 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toProduct(ProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.categoryId( request.getCategoryId() );
        product.description( request.getDescription() );
        product.id( request.getId() );
        product.imageId( request.getImageId() );
        product.make( request.getMake() );
        product.manufacturerId( request.getManufacturerId() );
        product.model( request.getModel() );
        product.name( request.getName() );
        product.otherNames( request.getOtherNames() );
        product.partCondition( request.getPartCondition() );
        product.partNumber( request.getPartNumber() );
        product.price( request.getPrice() );
        product.sku( request.getSku() );
        product.stock( request.getStock() );
        product.supplierId( request.getSupplierId() );
        product.tag( request.getTag() );
        product.year( request.getYear() );

        return product.build();
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

        ProductAttribute.ProductAttributeBuilder productAttribute = ProductAttribute.builder();

        productAttribute.attributeId( request.getAttributeId() );
        productAttribute.attributeValue( request.getAttributeValue() );
        productAttribute.id( request.getId() );
        productAttribute.productId( request.getProductId() );

        return productAttribute.build();
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
