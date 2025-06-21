package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.Product;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.ProductRepository;
import com.platform.parts.request.ProductRequest;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.response.ProductResponse;
import com.platform.parts.service.ProductService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private Product prepareProduct(ProductRequest request) {
		return Product.builder().id(GUIDGenerator.generateGUID()).name(request.getName())
				.description(request.getDescription()).manufacturerId(request.getManufacturerId())
				.categoryId(request.getCategoryId()).model(request.getModel()).otherNames(request.getOtherNames())
				.imageId(request.getImageId()).stock(request.getStock()).sku(request.getSku())
				.partNumber(request.getPartNumber()).tag(request.getTag()).year(request.getYear())
				.supplierId(request.getSupplierId()).partCondition(request.getPartCondition()).make(request.getMake())
				.price(request.getPrice()).stock(request.getStock()).build();
	}

	private String prepareAttributeUrl(String productId) {
		String url = "http://localhost:8097/api/v1/productattributes/productid/";
		return url + productId;
	}

	@Override
	public GenericResponse getAllProducts() {
		List<ProductResponse> responseBody = new ArrayList<>();
		try {
			List<Product> products = productRepository.findAll();
			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting products");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getProductsById(String id) throws NotFoundException {
		try {

			Product product = productRepository.findById(id).get();

			return GenericResponse.builder().body(product).code("200")
					.message("successfully Retrived " + id + " Product").status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting Product By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getProductsByName(String name) throws NotFoundException {
		try {

			log.info("perform Product input validation");
			partsDataValidator.validateProductName(name);
			Product product = productRepository.findByName(name);

			return GenericResponse.builder().body(product).code("200")
					.message("successfully Retrived " + name + " Product").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting Product By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getProductsByCategoryId(String id) throws NotFoundException {
		List<ProductResponse> responseBody = new ArrayList<>();
		try {

			List<Product> products = productRepository.findByCategoryId(id);
			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + id + " Product").status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting Product By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse createProduct(List<ProductRequest> productRequestList) throws AlreadyExistException {
		try {

			List<Product> productList = new ArrayList<>();
			for (ProductRequest productRequest : productRequestList) {

				log.info("perform Product input validation");
				partsDataValidator.isProductAlreadyExists(productRequest.getName());

				Product product = productRepository.save(prepareProduct(productRequest));
				log.info("Product created - " + productRequest.toString());
				productList.add(product);
			}
			return GenericResponse.builder().body(productList).code("201").message("successfully Created  Product")
					.status("success").build();
		} catch (AlreadyExistException exc) {
			return GenericResponse.builder().status("failed").message("Resource Already Existed").code("400").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse updateProduct(ProductRequest request) throws NotFoundException {
		try {
			log.info("perform Product input validation");
			partsDataValidator.validateProductId(request.getId());
			Product product = productRepository.findById(request.getId()).get();
			if (product != null) {
				product.setId(request.getId());
				product.setName(request.getName());
				product.setDescription(request.getDescription());
				product.setPrice(request.getPrice());
				product.setStock(request.getStock());
				product.setSku(request.getSku());
				product.setPartNumber(request.getPartNumber());
				product.setPartCondition(request.getPartCondition());
				product.setCategoryId(request.getCategoryId());
				product.setYear(request.getYear());
				product.setMake(request.getMake());
				product.setModel(request.getModel());
				product.setOtherNames(request.getOtherNames());
				product.setTag(request.getTag());
				product.setImageId(request.getImageId());
				product.setSupplierId(request.getSupplierId());
				product.setManufacturerId(request.getManufacturerId());
				product.setPrice(request.getPrice());
				productRepository.save(product);
			}
			return GenericResponse.builder().body("Product is been Updated").code("200")
					.message("successfully Updated " + request.getId() + " Product").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteProductById(String id) throws NotFoundException {
		try {
			log.info("perform Product input validation");
			partsDataValidator.validateProductId(id);
			Product product = productRepository.findById(id).get();
			if (product != null) {
				productRepository.delete(product);
			}
			return GenericResponse.builder().body("Product is been Deleted").code("200").message("successfully Deleted")
					.status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse searchProductsByName(String name) {
		List<ProductResponse> responseBody = new ArrayList<>();
		try {
			List<Product> products = productRepository.findByNameContaining(name);

			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse searchProductsByOtherNames(String otherName) throws NotFoundException {
		List<ProductResponse> responseBody = new ArrayList<>();
		try {
			List<Product> products = productRepository.findByOtherNamesContaining(otherName);

			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse searchProductsByTag(String tag) {
		List<ProductResponse> responseBody = new ArrayList<>();
		try {
			List<Product> products = productRepository.findByTagContaining(tag);

			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse searchProductsBySku(String sku) {

		List<ProductResponse> responseBody = new ArrayList<>();
		try {
			List<Product> products = productRepository.findBySkuContaining(sku);

			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse searchProductsByCategory(String categoryId) {

		List<ProductResponse> responseBody = new ArrayList<>();
		try {
			List<Product> products = productRepository.findByCategoryId(categoryId);

			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse searchProductsBySupplier(String supplierId) {

		List<ProductResponse> responseBody = new ArrayList<>();
		try {
			List<Product> products = productRepository.findBySupplierId(supplierId);

			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse searchProductsByPartNumber(String partNumber) {

		List<ProductResponse> responseBody = new ArrayList<>();
		try {
			List<Product> products = productRepository.findByPartNumber(partNumber);

			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse searchProductsByYearMakeModel(Integer year, String make, String model) {

		List<ProductResponse> responseBody = new ArrayList<>();
		try {
			List<Product> products = productRepository.findByYearAndMakeAndModel(year, make, model);

			for (Product product : products) {
				responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
						.description(product.getDescription()).manufacturerId(product.getManufacturerId())
						.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
						.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
						.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
						.year(product.getYear()).tag(product.getTag()).model(product.getModel())
						.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
						.attributeUrl(prepareAttributeUrl(product.getId())).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Product");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

}
