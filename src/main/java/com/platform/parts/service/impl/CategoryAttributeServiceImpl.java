package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.CategoryAttribute;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.CategoryAttributeRepository;
import com.platform.parts.request.CategoryAttributeRequest;
import com.platform.parts.response.CategoryAttributeResponse;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.service.CategoryAttributeService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryAttributeServiceImpl implements CategoryAttributeService {

	@Autowired
	private CategoryAttributeRepository categoryAttributeRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private CategoryAttribute prepareCategoryAttribute(CategoryAttributeRequest request) {
		return CategoryAttribute.builder().id(GUIDGenerator.generateGUID()).categoryId(request.getCategoryId())
				.attributeId(request.getAttributeId()).build();
	}

	private String prepareAttributeUrl(String attributeId) {
		String url = "http://localhost:8097/api/v1/attributes/";
		return url + attributeId;
	}

	@Override
	public GenericResponse getCategoryAttributes() {
		List<CategoryAttributeResponse> responseBody = new ArrayList<>();
		try {
			List<CategoryAttribute> categorieAttributes = categoryAttributeRepository.findAll();
			for (CategoryAttribute categoryAttribute : categorieAttributes) {
				responseBody.add(CategoryAttributeResponse.builder().id(categoryAttribute.getId())
						.attributeUrl(prepareAttributeUrl(categoryAttribute.getAttributeId()))
						.attributeId(categoryAttribute.getAttributeId()).categoryId(categoryAttribute.getCategoryId())
						.build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting Category Attributes");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
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
