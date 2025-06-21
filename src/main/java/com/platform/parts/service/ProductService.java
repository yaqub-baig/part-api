package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.ProductRequest;
import com.platform.parts.response.GenericResponse;

public interface ProductService {

	GenericResponse getAllProducts();

	GenericResponse getProductsById(String Id) throws NotFoundException;

	GenericResponse getProductsByCategoryId(String partCategoryId) throws NotFoundException;

	GenericResponse getProductsByName(String name) throws NotFoundException;

	GenericResponse createProduct(List<ProductRequest> productRequestList) throws AlreadyExistException;

	GenericResponse updateProduct(ProductRequest request) throws NotFoundException;

	GenericResponse deleteProductById(String id) throws NotFoundException;

	GenericResponse searchProductsByName(String name) throws NotFoundException;

	GenericResponse searchProductsByOtherNames(String otherName) throws NotFoundException;

	GenericResponse searchProductsByTag(String tag) throws NotFoundException;

	GenericResponse searchProductsBySku(String sku) throws NotFoundException;

	GenericResponse searchProductsByCategory(String categoryId) throws NotFoundException;

	GenericResponse searchProductsBySupplier(String supplierId) throws NotFoundException;

	GenericResponse searchProductsByPartNumber(String partNuber) throws NotFoundException;

	GenericResponse searchProductsByYearMakeModel(Integer year, String make, String model) throws NotFoundException;
}
