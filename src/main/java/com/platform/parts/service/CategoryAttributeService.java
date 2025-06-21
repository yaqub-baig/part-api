package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.CategoryAttributeRequest;
import com.platform.parts.response.GenericResponse;

public interface CategoryAttributeService {

	GenericResponse getCategoryAttributes();

	GenericResponse getCategoryAttributesById(String id) throws NotFoundException;

	GenericResponse getCategoryAttributesByCategoryIdAndAttributeId(String categoryId, String attributeId)
			throws NotFoundException;

	GenericResponse getCategoryAttributesByCategoryId(String categoryId) throws NotFoundException;

	GenericResponse createCategoryAttribute(List<CategoryAttributeRequest> categoryAttributeRequestList)
			throws AlreadyExistException;

	GenericResponse updateCategoryAttribute(CategoryAttributeRequest request) throws AlreadyExistException;

	GenericResponse deleteCategoryAttributeById(String id) throws NotFoundException;
}
