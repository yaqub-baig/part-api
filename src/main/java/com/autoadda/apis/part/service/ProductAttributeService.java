package com.autoadda.apis.part.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.part.entity.ProductAttribute;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.ProductAttributeRepository;
import com.autoadda.apis.part.request.ProductAttributeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.ProductAttributeResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductAttributeService {

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    @Autowired
    private PartsDataValidator partsDataValidator;

    /**
	 * Prepares a ProductAttribute entity from the request.
	 *
	 * @param request the ProductAttributeRequest containing the details
	 * @return a ProductAttribute entity
	 */
    private ProductAttribute prepareProductAttribute(ProductAttributeRequest request) {
        return ProductAttribute.builder()
                .id(GUIDGenerator.generateGUID())
                .attributeId(request.getAttributeId())
                .attributeValue(request.getAttributeValue())
                .productId(request.getProductId())
                .build();
    }

    /**	 * Prepares the URL for the attribute based on its ID.
     * 
     * @param attributeId
     * @return the URL string for the attribute
     */
    private String prepareAttributeUrl(String attributeId) {
        String url = "http://localhost:8097/api/v1/attributes/";
        return url + attributeId;
    }

    /**
	 * Retrieves all product attributes.
	 *
	 * @return a GenericResponse containing the list of ProductAttributeResponse
	 */
    public GenericResponse getProductAttributes() {
        List<ProductAttribute> productAttributes = productAttributeRepository.findAll();
        List<ProductAttributeResponse> responseBody = productAttributes.stream().map(pa ->
                ProductAttributeResponse.builder()
                        .id(pa.getId())
                        .productId(pa.getProductId())
                        .attributeId(pa.getAttributeId())
                        .attributeValue(pa.getAttributeValue())
                        .attributeUrl(prepareAttributeUrl(pa.getAttributeId()))
                        .build()).toList();

        return GenericResponse.builder()
                .body(responseBody)
                .code("200")
                .message("success")
                .status("success")
                .build();
    }

    /**	 * Retrieves product attributes by product ID.
	 *
	 * @param productId the ID of the product
	 * @return a GenericResponse containing the list of ProductAttributeResponse
	 */
    public GenericResponse getProductAttributeByProductId(String productId) {
        List<ProductAttribute> productAttributes = productAttributeRepository.findByProductId(productId);

        List<ProductAttributeResponse> responseBody = productAttributes.stream().map(pa ->
                ProductAttributeResponse.builder()
                        .id(pa.getId())
                        .productId(pa.getProductId())
                        .attributeId(pa.getAttributeId())
                        .attributeValue(pa.getAttributeValue())
                        .attributeUrl(prepareAttributeUrl(pa.getAttributeId()))
                        .build()).toList();

        return GenericResponse.builder()
                .body(responseBody)
                .code("200")
                .message("Successfully retrieved attributes for productId: " + productId)
                .status("success")
                .build();
    }

    /**	 * Retrieves a product attribute by its ID.
     * 
     * @param id
     * @return a GenericResponse containing the ProductAttribute
     * @throws NotFoundException 
     */
    public GenericResponse getProductAttributeById(String id) throws NotFoundException {
        log.info("Validating productAttribute ID");
        partsDataValidator.validateProductAttributeId(id);

        ProductAttribute productAttribute = productAttributeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ProductAttribute not found with ID: " + id));

        return GenericResponse.builder()
                .body(productAttribute)
                .code("200")
                .message("Successfully retrieved ProductAttribute: " + id)
                .status("success")
                .build();
    }

    /**	 * Creates a new product attribute or a list of product attributes.
	 * 
	 * @param productAttributeRequestList
	 * @return a GenericResponse containing the created ProductAttribute(s)
	 */
    public GenericResponse createProductAttribute(List<ProductAttributeRequest> productAttributeRequestList) {
        List<ProductAttribute> productAttributeList = new ArrayList<>();

        for (ProductAttributeRequest request : productAttributeRequestList) {
            ProductAttribute saved = productAttributeRepository.save(prepareProductAttribute(request));
            log.info("ProductAttribute created - {}", request);
            productAttributeList.add(saved);
        }

        return GenericResponse.builder()
                .body(productAttributeList)
                .code("201")
                .message("Successfully created ProductAttribute(s)")
                .status("success")
                .build();
    }

    /**	 * Updates an existing product attribute.
     * 
     * @param request
     * @return a GenericResponse indicating the result of the update
     * @throws NotFoundException 
     */
    public GenericResponse updateProductAttribute(ProductAttributeRequest request) throws NotFoundException {
        log.info("Validating productAttribute ID");
        partsDataValidator.validateProductAttributeId(request.getId());

        ProductAttribute productAttribute = productAttributeRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("ProductAttribute not found with ID: " + request.getId()));

        productAttribute.setAttributeId(request.getAttributeId());
        productAttribute.setAttributeValue(request.getAttributeValue());
        productAttribute.setProductId(request.getProductId());

        productAttributeRepository.save(productAttribute);

        return GenericResponse.builder()
                .body("ProductAttribute has been updated")
                .code("200")
                .message("Successfully updated ProductAttribute: " + request.getId())
                .status("success")
                .build();
    }

    /**	 * Deletes a product attribute by its ID.
	 * 
	 * @param id
	 * @return a GenericResponse indicating the result of the deletion
	 * @throws NotFoundException 
	 */
	public GenericResponse deleteProductAttributeById(String id) throws NotFoundException {
        log.info("Validating productAttribute ID");
        partsDataValidator.validateProductAttributeId(id);

        ProductAttribute productAttribute = productAttributeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ProductAttribute not found with ID: " + id));

        productAttributeRepository.delete(productAttribute);

        return GenericResponse.builder()
                .body("ProductAttribute has been deleted")
                .code("200")
                .message("Successfully deleted")
                .status("success")
                .build();
    }
}

