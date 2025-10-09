package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.MediaFile;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.ProductAttribute;
import com.autoadda.apis.part.exception.AlreadyExistException;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.repository.ProductAttributeRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.request.ProductRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.ProductResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PartsDataValidator partsDataValidator;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductAttributeService productAttributeServcie;
    private final MediaFileRepository mediaFileRepository;

    /**
     * Prepares a Product entity from the given ProductRequest.
     */
    private Product prepareProduct(ProductRequest request) {
        List<MediaFile> allById;

        Product build = Product.builder ( )
                .id ( GUIDGenerator.generateGUID ( ) )
                .name ( request.getName ( ) )
                .description ( request.getDescription ( ) )
                .manufacturerId ( request.getManufacturerId ( ) )
                .categoryId ( request.getCategoryId ( ) )
                .model ( request.getModel ( ) )
                .otherNames ( request.getOtherNames ( ) )
                .partNumber ( request.getPartNumber ( ) )
                .tag ( request.getTag ( ) )
                .year ( request.getYear ( ) )
                .make ( request.getMake ( ) )
                .build ( );
        if ( ! request.getImageIds ( ).isEmpty ( ) ) {
            allById = mediaFileRepository.findAllById ( request.getImageIds ( ) );
            build.setImages ( allById );
        }
        return build;
    }

    private String prepareAttributeUrl(String productId) {
        String url = "http://localhost:8060/api/v1/productattributes/productid/";
        return url + productId;
    }

    /**
     * Helper method to build ProductResponse from Product entity
     */
    private ProductResponse buildProductResponse(Product product, boolean includeAttributes) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder ( )
                .id ( product.getId ( ) )
                .name ( product.getName ( ) )
                .description ( product.getDescription ( ) )
                .manufacturerId ( product.getManufacturerId ( ) )
                .categoryId ( product.getCategoryId ( ) )
                .make ( product.getMake ( ) )
                .partNumber ( product.getPartNumber ( ) )
                .year ( product.getYear ( ) )
                .tag ( product.getTag ( ) )
                .model ( product.getModel ( ) )
                .otherNames ( product.getOtherNames ( ) )
                .attributeUrl ( prepareAttributeUrl ( product.getId ( ) ) )
                .images ( product.getImages ( ) );

        if ( includeAttributes ) {
            builder.attributes (
                    productAttributeServcie.getProductAttributeByProductId ( product.getId ( ) ).getBody ( ) != null
                            ? (List<ProductAttribute>) productAttributeServcie.getProductAttributeByProductId ( product.getId ( ) ).getBody ( )
                            : new ArrayList<> ( )
            );
        }

        return builder.build ( );
    }

    /**
     * Retrieves all products from the repository and prepares a response.
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public GenericResponse getAllProducts() {
        List<ProductResponse> responseBody = productRepository.findAll ( ).stream ( )
                .map ( product -> buildProductResponse ( product, true ) )
                .toList ( );

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "success" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Retrieves a product by its ID and prepares a response.
     */
    @Transactional
    public GenericResponse getProductsById(String id) throws NotFoundException {
        Product product = productRepository.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Product not found with id " + id ) );
        ProductResponse build = buildProductResponse ( product, true );
        return GenericResponse.builder ( )
                .body ( build )
                .code ( "200" )
                .message ( "successfully Retrived " + id + " Product" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Retrieves a product by its name and prepares a response.
     */
    @Transactional
    public GenericResponse getProductsByName(String name) throws NotFoundException {
        log.info ( "perform Product input validation" );
        partsDataValidator.validateProductName ( name );
        Product product = productRepository.findByName ( name );
        ProductResponse build = buildProductResponse ( product, true );
        return GenericResponse.builder ( )
                .body ( build )
                .code ( "200" )
                .message ( "successfully Retrived " + name + " Product" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Retrieves products by category ID and prepares a response.
     */
    @Transactional
    public GenericResponse getProductsByCategoryId(String id) throws NotFoundException {
        List<ProductResponse> responseBody = productRepository.findByCategoryId ( id ).stream ( )
                .map ( product -> buildProductResponse ( product, false ) )
                .toList ( );

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "successfully Retrived " + id + " Product" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Creates a list of products based on the provided ProductRequest list.
     */
    @Transactional
    public GenericResponse createProduct(List<ProductRequest> productRequestList) throws AlreadyExistException {
        List<Product> productList = new ArrayList<> ( );
        for (ProductRequest productRequest : productRequestList) {
            log.info ( "perform Product input validation" );
            partsDataValidator.isProductAlreadyExists ( productRequest.getName ( ) );
            Product product = productRepository.save ( prepareProduct ( productRequest ) );
            log.info ( "Product created - " + productRequest.toString ( ) );

            // Save associated attributes
            if ( productRequest.getAttributes ( ) != null ) {
                List<ProductAttribute> productAttributes = productRequest.getAttributes ( ).stream ( )
                        .map ( attrReq -> ProductAttribute.builder ( )
                                .id ( GUIDGenerator.generateGUID ( ) )
                                .productId ( product.getId ( ) )
                                .attributeId ( attrReq.getAttributeId ( ) )
                                .attributeValue ( attrReq.getAttributeValue ( ) )
                                .build ( ) )
                        .toList ( );

                productAttributeRepository.saveAll ( productAttributes );
                log.info ( "Product attributes saved for product: {}", product.getId ( ) );
            }
            productList.add ( product );
        }
        return GenericResponse.builder ( )
                .body ( productList )
                .code ( "201" )
                .message ( "successfully Created  Product" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Updates an existing product based on the provided ProductRequest.
     */
    @Transactional
    public GenericResponse updateProduct(ProductRequest request) throws NotFoundException {
        log.info ( "perform Product input validation" );
        partsDataValidator.validateProductId ( request.getId ( ) );
        List<MediaFile> images = takeOutMediaFileFromRequestImageIds ( request.getImageIds ( ) );
        Product product = productRepository.findById ( request.getId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "product repository " ) );
        if ( product != null ) {
            // Only update fields that are provided (not null)
            if ( request.getName ( ) != null ) {
                product.setName ( request.getName ( ) );
            }
            if ( request.getDescription ( ) != null ) {
                product.setDescription ( request.getDescription ( ) );
            }
            if ( request.getPartNumber ( ) != null ) {
                product.setPartNumber ( request.getPartNumber ( ) );
            }
            if ( request.getCategoryId ( ) != null ) {
                product.setCategoryId ( request.getCategoryId ( ) );
            }
            if ( request.getYear ( ) != null ) {
                product.setYear ( request.getYear ( ) );
            }
            if ( request.getMake ( ) != null ) {
                product.setMake ( request.getMake ( ) );
            }
            if ( request.getModel ( ) != null ) {
                product.setModel ( request.getModel ( ) );
            }
            if ( request.getOtherNames ( ) != null ) {
                product.setOtherNames ( request.getOtherNames ( ) );
            }
            if ( request.getTag ( ) != null ) {
                product.setTag ( request.getTag ( ) );
            }
            if ( request.getManufacturerId ( ) != null ) {
                product.setManufacturerId ( request.getManufacturerId ( ) );
            }
            // Update images only if imageIds are provided
            if ( request.getImageIds ( ) != null && ! request.getImageIds ( ).isEmpty ( ) ) {
                product.getImages ( ).clear ( );
                product.getImages ( ).addAll ( images );
            }
            productRepository.save ( product );
        }
        return GenericResponse.builder ( )
                .body ( "Product is been Updated" )
                .code ( "200" )
                .message ( "successfully Updated " + request.getId ( ) + " Product" )
                .status ( "success" )
                .build ( );
    }

    private List<MediaFile> takeOutMediaFileFromRequestImageIds(List<Long> imageIds) {
        List<MediaFile> mediaFiles = new ArrayList<> ( );
        for (Long imageId : imageIds) {
            mediaFiles.add ( mediaFileRepository.findById ( imageId )
                    .orElseThrow ( () -> new ResourceNotFoundException ( "media File not found" ) ) );
        }
        return mediaFiles;
    }

    /**
     * Deletes a product by its ID.
     */
    @Transactional
    public GenericResponse deleteProductById(String id) throws NotFoundException {
        log.info ( "perform Product input validation" );
        partsDataValidator.validateProductId ( id );
        Product product = productRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "resource not found exception" ) );
        if ( product != null ) {
            productRepository.delete ( product );
        }
        return GenericResponse.builder ( )
                .body ( "Product is been Deleted" )
                .code ( "200" )
                .message ( "successfully Deleted" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Searches for products by name and prepares a response.
     */
    @Transactional
    public GenericResponse searchProductsByName(String name) {
        List<ProductResponse> responseBody = productRepository.findByNameContaining ( name ).stream ( )
                .map ( product -> buildProductResponse ( product, false ) )
                .toList ( );

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "successfully Retrieved" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Searches for products by other names and prepares a response.
     */
    @Transactional
    public GenericResponse searchProductsByOtherNames(String otherName) throws NotFoundException {
        List<ProductResponse> responseBody = productRepository.findByOtherNamesContaining ( otherName ).stream ( )
                .map ( product -> buildProductResponse ( product, false ) )
                .toList ( );

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "successfully Retrieved" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Searches for products by tag and prepares a response.
     */
    @Transactional
    public GenericResponse searchProductsByTag(String tag) {
        List<ProductResponse> responseBody = productRepository.findByTagContaining ( tag ).stream ( )
                .map ( product -> buildProductResponse ( product, false ) )
                .toList ( );

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "successfully Retrieved" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Searches for products by SKU and prepares a response.
     */
//    @Transactional
//    public GenericResponse searchProductsBySku(String sku) {
//        List<ProductResponse> responseBody = productRepository.findBySkuContaining ( sku ).stream ( )
//                .map ( product -> buildProductResponse ( product, false ) )
//                .toList ( );
//
//        return GenericResponse.builder ( )
//                .body ( responseBody )
//                .code ( "200" )
//                .message ( "successfully Retrieved" )
//                .status ( "success" )
//                .build ( );
//    }

    /**
     * Searches for products by category ID and prepares a response.
     */
    @Transactional
    public GenericResponse searchProductsByCategory(String categoryId) {
        List<ProductResponse> responseBody = productRepository.findByCategoryId ( categoryId ).stream ( )
                .map ( product -> buildProductResponse ( product, false ) )
                .toList ( );

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "successfully Retrieved" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Searches for products by supplier ID - NO LONGER SUPPORTED
     * Products are predefined and not supplier-specific
     * Use InventoryPartRepository.findBySupplierId() instead
     */
    @Deprecated
    @Transactional
    public GenericResponse searchProductsBySupplier(String supplierId) {
        return GenericResponse.builder ( )
                .body ( new ArrayList<> ( ) )
                .code ( "400" )
                .message ( "This endpoint is deprecated. Products are not supplier-specific. Use inventory API instead." )
                .status ( "error" )
                .build ( );
    }

    /**
     * Searches for products by part number and prepares a response.
     */
    @Transactional
    public GenericResponse searchProductsByPartNumber(String partNumber) {
        ProductResponse responseBody = buildProductResponse ( productRepository.findByPartNumber ( partNumber ), false );

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "successfully Retrieved" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Searches for products by year, make, and model and prepares a response.
     */
    @Transactional
    public GenericResponse searchProductsByYearMakeModel(Integer year, String make, String model) {
        List<ProductResponse> responseBody = productRepository.findByYearAndMakeAndModel ( year, make, model ).stream ( )
                .map ( product -> buildProductResponse ( product, false ) )
                .toList ( );

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "successfully Retrieved" )
                .status ( "success" )
                .build ( );
    }

    /**
     * Searches for products by manufacturer ID and prepares a response.
     */
    @Transactional
    public GenericResponse searchProductsByManufacturer(String manufacturerId) {
        List<ProductResponse> responseBody = productRepository.findByManufacturerId ( manufacturerId ).stream ( )
                .map ( product -> buildProductResponse ( product, false ) )
                .toList ( );

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "successfully Retrieved" )
                .status ( "success" )
                .build ( );
    }
}