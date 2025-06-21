package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.ProductAttribute;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.ProductAttributeRepository;
import com.platform.parts.request.ProductAttributeRequest;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.response.ProductAttributeResponse;
import com.platform.parts.service.ProductAttributeService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductAttributeServiceImpl implements ProductAttributeService {

	@Autowired
	private ProductAttributeRepository productAttributeRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private ProductAttribute prepareProductAttribute(ProductAttributeRequest request) {
		return ProductAttribute.builder().id(GUIDGenerator.generateGUID()).attributeId(request.getAttributeId())
				.attributeValue(request.getAttributeValue()).productId(request.getProductId()).build();
	}
	
	private String prepareAttributeUrl(String attributeId) {
		String url = "http://localhost:8097/api/v1/attributes/";
		return url + attributeId;
	}

	@Override
	public GenericResponse getProductAttributes() {
		List<ProductAttributeResponse> responseBody = new ArrayList<>();
		try {
			List<ProductAttribute> productAttributes = productAttributeRepository.findAll();
			for (ProductAttribute productAttribute : productAttributes) {
				responseBody.add(ProductAttributeResponse.builder().attributeId(productAttribute.getAttributeId())
						.attributeUrl(prepareAttributeUrl(productAttribute.getAttributeId()))
						.id(productAttribute.getId()).attributeValue(productAttribute.getAttributeValue())
						.productId(productAttribute.getProductId()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting product attributes");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getProductAttributeByProductId(String productId) throws NotFoundException {
		List<ProductAttributeResponse> responseBody = new ArrayList<>();
		try {

			List<ProductAttribute> productAttributes = productAttributeRepository.findByProductId(productId);
			for (ProductAttribute productAttribute : productAttributes) {
				responseBody.add(ProductAttributeResponse.builder().attributeId(productAttribute.getAttributeId())
						.id(productAttribute.getId()).attributeValue(productAttribute.getAttributeValue())
						.attributeUrl(prepareAttributeUrl(productAttribute.getAttributeId()))
						.productId(productAttribute.getProductId()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + productId + " ProductAttributes").status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting category By Product Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getProductAttributeById(String id) throws NotFoundException {
		try {
			log.info("perform ProductAttribute input validation");
			partsDataValidator.validateProductAttributeId(id);
			ProductAttribute productAttribute = productAttributeRepository.findById(id).get();

			return GenericResponse.builder().body(productAttribute).code("200")
					.message("successfully Retrived " + id + " ProductAttribute").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting category By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse createProductAttribute(List<ProductAttributeRequest> productAttributeRequestList)
			throws AlreadyExistException {
		try {

			List<ProductAttribute> productAttributeList = new ArrayList<>();
			for (ProductAttributeRequest productAttributeRequest : productAttributeRequestList) {

				ProductAttribute productAttribute = productAttributeRepository
						.save(prepareProductAttribute(productAttributeRequest));
				log.info("ProductAttribute created - " + productAttributeRequest.toString());
				productAttributeList.add(productAttribute);
			}

			return GenericResponse.builder().body(productAttributeList).code("201")
					.message("successfully Created  ProductAttribute").status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating Product Attribute");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse updateProductAttribute(ProductAttributeRequest request) throws NotFoundException {
		try {
			log.info("perform Product Attribute input validation");
			partsDataValidator.validateProductAttributeId(request.getId());
			ProductAttribute productAttribute = productAttributeRepository.findById(request.getId()).get();
			if (productAttribute != null) {
				productAttribute.setAttributeId(request.getAttributeId());
				productAttribute.setAttributeValue(request.getAttributeValue());
				productAttribute.setProductId(request.getProductId());
				productAttributeRepository.save(productAttribute);
			}
			return GenericResponse.builder().body("Attribute is been Updated").code("200")
					.message("successfully Updated " + request.getId() + " Attribute").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating Attribute");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteProductAttributeById(String id) throws NotFoundException {
		try {
			log.info("perform ProductAttribute input validation");
			partsDataValidator.validateProductAttributeId(id);
			ProductAttribute productAttribute = productAttributeRepository.findById(id).get();
			log.info("Deleting ProductAttribute by Id");
			if (productAttribute != null) {
				productAttributeRepository.delete(productAttribute);
			}
			return GenericResponse.builder().body("ProductAttribute is been Deleted").code("200")
					.message("successfully Deleted").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting ProductAttribute");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}
}
