package com.autoadda.apis.part.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoadda.apis.part.entity.Category;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.CategoryRepository;
import com.autoadda.apis.part.request.CategoryRequest;
import com.autoadda.apis.part.response.CategoryResponse;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private String prepareAttributeUrl(String categoryId) {
		return "http://localhost:8097/api/v1/categoryattributes/categoryid/" + categoryId;
	}

	private Category prepareCategory(CategoryRequest request) {
		return Category.builder().id(request.getId()).name(request.getName()).code(request.getCode())
				.parentCategoryId(request.getParentCategoryId()).build();
	}

	/**
	 * Retrieves all categories from the repository and maps them to a response
	 * object.
	 *
	 * @return GenericResponse containing a list of CategoryResponse objects.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getTopLevelCategories() {
		List<Category> categories = categoryRepository.findByParentCategoryIdIsNull();

		List<CategoryResponse> responseBody = categories.stream()
				.map(category -> CategoryResponse.builder().name(category.getName()).id(category.getId())
						.attributeUrl(prepareAttributeUrl(category.getId())).code(category.getCode())
						.parentCategoryId(category.getParentCategoryId()).build())
				.collect(Collectors.toList());

		return GenericResponse.builder().body(responseBody).code("200").message("success").status("success").build();
	}

	/**
	 * Retrieves subcategories by parent category ID and maps them to a response
	 * object.
	 *
	 * @param parentId The ID of the parent category.
	 * @return GenericResponse containing a list of CategoryResponse objects for
	 *         subcategories.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getSubCategoriesByParentId(String parentId) {
		List<Category> categories = categoryRepository.findByParentCategoryId(parentId);

		List<CategoryResponse> responseBody = categories.stream()
				.map(category -> CategoryResponse.builder().id(category.getId()).name(category.getName())
						.attributeUrl(prepareAttributeUrl(category.getId())).code(category.getCode())
						.parentCategoryId(category.getParentCategoryId()).build())
				.collect(Collectors.toList());

		return GenericResponse.builder().body(responseBody).code("200").message("success").status("success").build();
	}

	/**
	 * Retrieves a category by its ID and maps it to a response object.
	 *
	 * @param id The ID of the category to retrieve.
	 * @return GenericResponse containing the CategoryResponse object.
	 * @throws NotFoundException if the category with the given ID does not exist.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getCategoryByName(String name) throws NotFoundException {
		log.info("Perform Category input validation");
		partsDataValidator.validateCategoryName(name);

		Category category = categoryRepository.findByName(name);
		if (category == null) {
			throw new NotFoundException("Category not found for name: " + name);
		}

		return GenericResponse.builder().body(category).code("200").message("Successfully retrieved category: " + name)
				.status("success").build();
	}

	/**
	 * Creates a new category based on the provided request object.
	 *
	 * @param categoryRequestList List of CategoryRequest objects containing the
	 *                            details of the categories to be created.
	 * @return GenericResponse containing the created categories.
	 */
	@Transactional
	public GenericResponse createCategory(List<CategoryRequest> categoryRequestList) {
		List<Category> categoryList = new ArrayList<>();

		for (CategoryRequest categoryRequest : categoryRequestList) {
			log.info("Perform Category input validation");
			partsDataValidator.isCategoryAlreadyExists(categoryRequest.getName());

			Category category = categoryRepository.save(prepareCategory(categoryRequest));
			log.info("Category created - {}", categoryRequest.toString());
			categoryList.add(category);
		}

		return GenericResponse.builder().body(categoryList).code("201").message("Successfully created category")
				.status("success").build();
	}

	/**
	 * Updates an existing category based on the provided request object.
	 *
	 * @param updateRequest CategoryRequest object containing the updated details.
	 * @return GenericResponse containing the updated category.
	 * @throws NotFoundException if the category with the specified ID does not
	 *                           exist.
	 */
	@Transactional
	public GenericResponse deleteCategoryById(String id) throws NotFoundException {
		log.info("Perform Category input validation");
		partsDataValidator.validateCategoryId(id);

		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Category not found for ID: " + id));

		categoryRepository.delete(category);

		return GenericResponse.builder().body("Category has been deleted").code("200").message("Successfully deleted")
				.status("success").build();
	}
}
