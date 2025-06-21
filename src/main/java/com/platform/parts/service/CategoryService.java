package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.CategoryRequest;
import com.platform.parts.response.GenericResponse;

public interface CategoryService {

	GenericResponse getTopLevelCategories();

	GenericResponse getSubCategoriesByParentId(String parentId) throws NotFoundException;

	GenericResponse getCategoryByName(String name) throws NotFoundException;

	GenericResponse createCategory(List<CategoryRequest> categoryRequestList) throws AlreadyExistException;

	GenericResponse deleteCategoryById(String id) throws NotFoundException;
}
