package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.Category;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.CategoryRepository;
import com.platform.parts.request.CategoryRequest;
import com.platform.parts.response.CategoryResponse;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.service.CategoryService;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private String prepareAttributeUrl(String categoryId) {
		String url = "http://localhost:8097/api/v1/categoryattributes/categoryid/";
		return url + categoryId;
	}

	private Category prepareCategory(CategoryRequest request) {
		return Category.builder().id(request.getId()).name(request.getName()).code(request.getCode())
				.parentCategoryId(request.getParentCategoryId()).build();
	}

	@Override
	public GenericResponse getTopLevelCategories() {
		List<CategoryResponse> responseBody = new ArrayList<>();
		try {
			List<Category> categories = categoryRepository.findByParentCategoryIdIsNull();
			for (Category category : categories) {
				responseBody.add(CategoryResponse.builder().name(category.getName()).id(category.getId())
						.attributeUrl(prepareAttributeUrl(category.getId())).code(category.getCode())
						.parentCategoryId(category.getParentCategoryId()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting categories");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getSubCategoriesByParentId(String parentId) {
		List<CategoryResponse> responseBody = new ArrayList<>();
		try {
			List<Category> categories = categoryRepository.findByParentCategoryId(parentId);
			for (Category category : categories) {
				responseBody.add(CategoryResponse.builder().id(category.getId()).name(category.getName())
						.attributeUrl(prepareAttributeUrl(category.getId())).code(category.getCode())
						.parentCategoryId(category.getParentCategoryId()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting categories");
		}

		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getCategoryByName(String name) throws NotFoundException {
		try {
			log.info("perform Category input validation");
			partsDataValidator.validateCategoryName(name);
			Category category = categoryRepository.findByName(name);

			return GenericResponse.builder().body(category).code("200")
					.message("successfully Retrived " + name + " Category").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting category By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse createCategory(List<CategoryRequest> categoryRequestList) throws AlreadyExistException {
		try {

			List<Category> categoryList = new ArrayList<>();
			for (CategoryRequest categoryRequest : categoryRequestList) {

				log.info("perform Category input validation");
				partsDataValidator.isCategoryAlreadyExists(categoryRequest.getName());

				Category category = categoryRepository.save(prepareCategory(categoryRequest));
				log.info("Category created - " + categoryRequest.toString());
				categoryList.add(category);
			}

			return GenericResponse.builder().body(categoryList).code("201").message("successfully Created Category")
					.status("success").build();
		} catch (AlreadyExistException exc) {
			return GenericResponse.builder().status("failed").message("Resource Already Existed").code("400").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating category");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteCategoryById(String id) throws NotFoundException {
		try {
			log.info("perform Category input validation");
			partsDataValidator.validateCategoryId(id);
			Category category = categoryRepository.findById(id).get();
			log.info("Deleting Category by Id");
			if (category != null) {
				categoryRepository.delete(category);
			}
			return GenericResponse.builder().body("Category is been Deleted").code("200")
					.message("successfully Deleted").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting Category");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}
}
