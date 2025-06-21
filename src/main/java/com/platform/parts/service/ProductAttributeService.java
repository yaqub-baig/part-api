package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.ProductAttributeRequest;
import com.platform.parts.response.GenericResponse;

public interface ProductAttributeService {

	GenericResponse getProductAttributes();

	GenericResponse getProductAttributeById(String id) throws NotFoundException;

	GenericResponse getProductAttributeByProductId(String productId) throws NotFoundException;

	GenericResponse createProductAttribute(List<ProductAttributeRequest> productAttributeRequestList) throws AlreadyExistException;

	GenericResponse deleteProductAttributeById(String id) throws NotFoundException;
	
	GenericResponse updateProductAttribute(ProductAttributeRequest request) throws NotFoundException;
}
