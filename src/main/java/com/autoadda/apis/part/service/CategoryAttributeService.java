package com.autoadda.apis.part.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoadda.apis.part.entity.CategoryAttribute;
import com.autoadda.apis.part.exception.AlreadyExistException;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.CategoryAttributeRepository;
import com.autoadda.apis.part.request.CategoryAttributeRequest;
import com.autoadda.apis.part.response.CategoryAttributeResponse;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryAttributeService {

	@Autowired
	private CategoryAttributeRepository categoryAttributeRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private CategoryAttributeResponse mapToResponse(CategoryAttribute categoryAttribute) {

		return CategoryAttributeResponse.builder().id(categoryAttribute.getId())
				.attributeId(categoryAttribute.getAttributeId()).categoryId(categoryAttribute.getCategoryId())
				.attributeUrl(prepareAttributeUrl(categoryAttribute.getAttributeId())).build();
	}

	private CategoryAttribute prepareCategoryAttribute(CategoryAttributeRequest request) {
		return CategoryAttribute.builder().id(GUIDGenerator.generateGUID()).categoryId(request.getCategoryId())
				.attributeId(request.getAttributeId()).build();
	}

	private String prepareAttributeUrl(String attributeId) {
		String url = "http://localhost:8097/api/v1/attributes/";
		return url + attributeId;
	}

	/**
	 * Retrieves all category attributes from the repository and maps them to a
	 * response object.
	 *
	 * @return GenericResponse containing a list of CategoryAttributeResponse
	 *         objects.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getCategoryAttributes() {

		List<CategoryAttribute> categoryAttributes = categoryAttributeRepository.findAll();

		List<CategoryAttributeResponse> responseList = categoryAttributes.stream().map(this::mapToResponse).toList();
		log.info("Retrieved {} category attributes", responseList.size());
		return GenericResponse.builder().body(responseList).code("200").message("success").status("success").build();

	}

	/**
	 * Creates a new category attribute or a list of category attributes based on
	 * the provided request objects.
	 *
	 * @param categoryAttributeRequestList List of CategoryAttributeRequest objects
	 *                                     containing category attribute details.
	 * @return GenericResponse containing the created category attributes.
	 * @throws AlreadyExistException if a category attribute with the same
	 *                               categoryId and attributeId already exists.
	 */
	@Transactional(readOnly = true)
	public GenericResponse createCategoryAttribute(List<CategoryAttributeRequest> categoryAttributeRequestList)
			throws AlreadyExistException {
		try {

			List<CategoryAttribute> categoryAttributeList = new ArrayList<>();
			for (CategoryAttributeRequest categoryAttributeRequest : categoryAttributeRequestList) {
				CategoryAttribute categoryAttribute = categoryAttributeRepository
						.save(prepareCategoryAttribute(categoryAttributeRequest));
				categoryAttributeList.add(categoryAttribute);
			}
			return GenericResponse.builder().body(categoryAttributeList).code("201")
					.message("successfully Created CategoryAttribute").status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating attribute");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	/**
	 * Retrieves a category attribute by its ID and maps it to a response object.
	 *
	 * @param id The ID of the category attribute to retrieve.
	 * @return GenericResponse containing the CategoryAttributeResponse object.
	 * @throws NotFoundException if the category attribute with the specified ID
	 *                           does not exist.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getCategoryAttributesById(String id) throws NotFoundException {
		List<CategoryAttributeResponse> responseBody = new ArrayList<>();
		try {

			log.info("perform Category Attribute input validation");
			partsDataValidator.validateCategoryAttributeId(id);
			CategoryAttribute categoryAttribute = categoryAttributeRepository.findById(id).get();
			if (categoryAttribute != null && categoryAttribute.getId() != null) {
				responseBody.add(CategoryAttributeResponse.builder().id(categoryAttribute.getId())
						.attributeUrl(prepareAttributeUrl(categoryAttribute.getAttributeId()))
						.attributeId(categoryAttribute.getAttributeId()).categoryId(categoryAttribute.getCategoryId())
						.build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + id + " Attribute").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting attribute By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	/**
	 * Retrieves a category attribute by its category ID and attribute ID and maps
	 * it to a response object.
	 *
	 * @param categoryId  The ID of the category to which the attribute belongs.
	 * @param attributeId The ID of the attribute to retrieve.
	 * @return GenericResponse containing the CategoryAttributeResponse object.
	 * @throws NotFoundException if the category attribute with the specified IDs
	 *                           does not exist.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getCategoryAttributesByCategoryIdAndAttributeId(String categoryId, String attributeId)
			throws NotFoundException {
		List<CategoryAttributeResponse> responseBody = new ArrayList<>();
		try {

			log.info("perform CategoryAttribute input validation");
			partsDataValidator.validateCategoryIdAndAttributeId(categoryId, attributeId);
			CategoryAttribute categoryAttribute = categoryAttributeRepository.findByCategoryIdAndAttributeId(categoryId,
					attributeId);
			if (categoryAttribute != null && categoryAttribute.getId() != null) {
				responseBody.add(CategoryAttributeResponse.builder().id(categoryAttribute.getId())
						.attributeUrl(prepareAttributeUrl(categoryAttribute.getAttributeId()))
						.categoryId(categoryAttribute.getCategoryId()).attributeId(categoryAttribute.getAttributeId())
						.build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrieved  CategoryAttribute").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting attribute By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	/**
	 * Retrieves all category attributes for a specific category ID and maps them to
	 * a response object.
	 *
	 * @param categoryId The ID of the category for which to retrieve attributes.
	 * @return GenericResponse containing a list of CategoryAttributeResponse
	 *         objects.
	 * @throws NotFoundException if no category attributes are found for the given
	 *                           category ID.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getCategoryAttributesByCategoryId(String categoryId) throws NotFoundException {
		List<CategoryAttributeResponse> responseBody = new ArrayList<>();
		try {

			List<CategoryAttribute> categoryAttributes = categoryAttributeRepository.findByCategoryId(categoryId);
			for (CategoryAttribute categoryAttribute : categoryAttributes) {
				responseBody.add(CategoryAttributeResponse.builder().id(categoryAttribute.getId())
						.attributeUrl(prepareAttributeUrl(categoryAttribute.getAttributeId()))
						.categoryId(categoryAttribute.getCategoryId()).attributeId(categoryAttribute.getAttributeId())
						.build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrieved  CategoryAttribute").status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting attribute By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	/**
	 * Updates an existing category attribute based on the provided request object.
	 *
	 * @param request CategoryAttributeRequest object containing the updated
	 *                details.
	 * @return GenericResponse containing the updated category attribute.
	 * @throws AlreadyExistException if a category attribute with the same
	 *                               categoryId and attributeId already exists.
	 */
	@Transactional(readOnly = true)
	public GenericResponse updateCategoryAttribute(CategoryAttributeRequest request) throws AlreadyExistException {
		try {
			log.info("perform CategoryAttribute input validation");
			partsDataValidator.validateCategoryIdAndAttributeId(request.getCategoryId(), request.getAttributeId());
			CategoryAttribute categoryAttribute = categoryAttributeRepository.findById(request.getId()).get();
			if (categoryAttribute != null) {
				categoryAttribute.setAttributeId(request.getAttributeId());
				categoryAttribute.setCategoryId(request.getCategoryId());
				categoryAttributeRepository.save(categoryAttribute);
			}
			return GenericResponse.builder().body("CategoryAttribute is been Updated").code("200")
					.message("successfully Updated " + request.getId() + " CategoryAttribute").status("success")
					.build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating CategoryAttribute");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	/**
	 * Deletes a category attribute by its ID.
	 *
	 * @param id The ID of the category attribute to delete.
	 * @return GenericResponse indicating the result of the deletion operation.
	 * @throws NotFoundException if the category attribute with the specified ID
	 *                           does not exist.
	 */
	@Transactional(readOnly = true)
	public GenericResponse deleteCategoryAttributeById(String id) throws NotFoundException {
		try {
			log.info("perform CategoryAttribute input validation");
			partsDataValidator.validateCategoryAttributeId(id);
			CategoryAttribute attribute = categoryAttributeRepository.findById(id).get();
			log.info("Deleting CategoryAttribute by Id");
			if (attribute != null) {
				categoryAttributeRepository.delete(attribute);
			}
			return GenericResponse.builder().body("CategoryAttribute is been Deleted").code("200")
					.message("successfully Deleted").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting CategoryAttribute");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

}
