package com.autoadda.apis.part.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.ProductAttribute;
import com.autoadda.apis.part.exception.AlreadyExistException;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.ProductAttributeRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.request.ProductRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.ProductResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;

import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	@Autowired
	private ProductAttributeRepository productAttributeRepository;

	@Autowired
	private ProductAttributeService productAttributeServcie;

	/**
	 * Prepares a Product entity from the given ProductRequest.
	 *
	 * @param request The ProductRequest containing product details.
	 * @return A Product entity with the provided details.
	 */
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

	/**
	 * Retrieves all products from the repository and prepares a response.
	 *
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public GenericResponse getAllProducts() {
		List<ProductResponse> responseBody = new ArrayList<>();
		List<Product> products = productRepository.findAll();
		for (Product product : products) {
			responseBody.add(ProductResponse.builder().id(product.getId()).name(product.getName())
					.description(product.getDescription()).manufacturerId(product.getManufacturerId())
					.imageId(product.getImageId()).categoryId(product.getCategoryId()).make(product.getMake())
					.categoryId(product.getCategoryId()).partNumber(product.getPartNumber()).sku(product.getSku())
					.supplierId(product.getSupplierId()).partCondition(product.getPartCondition())
					.year(product.getYear()).tag(product.getTag()).model(product.getModel())
					.otherNames(product.getOtherNames()).price(product.getPrice()).stock(product.getStock())
					.attributes(
							productAttributeServcie.getProductAttributeByProductId(product.getId()).getBody() != null
									? (List<ProductAttribute>) productAttributeServcie
											.getProductAttributeByProductId(product.getId()).getBody()
									: new ArrayList<>())
					.attributeUrl(prepareAttributeUrl(product.getId())).build());

		}
		return GenericResponse.builder().body(responseBody).code("200").message("success").status("success").build();

	}

	/**
	 * Retrieves a product by its ID and prepares a response.
	 *
	 * @param id The ID of the product to retrieve.
	 * @return A GenericResponse containing the Product entity.
	 * @throws NotFoundException If the product with the given ID does not exist.
	 */
	@Transactional
	public GenericResponse getProductsById(String id) throws NotFoundException {

		Product product = productRepository.findById(id).get();

		return GenericResponse.builder().body(product).code("200").message("successfully Retrived " + id + " Product")
				.status("success").build();
	}

	/**
	 * Retrieves a product by its name and prepares a response.
	 *
	 * @param name The name of the product to retrieve.
	 * @return A GenericResponse containing the Product entity.
	 * @throws NotFoundException If the product with the given name does not exist.
	 */
	@Transactional
	public GenericResponse getProductsByName(String name) throws NotFoundException {
		log.info("perform Product input validation");
		partsDataValidator.validateProductName(name);
		Product product = productRepository.findByName(name);

		return GenericResponse.builder().body(product).code("200").message("successfully Retrived " + name + " Product")
				.status("success").build();
	}

	/**
	 * Retrieves products by category ID and prepares a response.
	 *
	 * @param id The category ID to filter products.
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 * @throws NotFoundException If no products are found for the given category ID.
	 */
	@Transactional
	public GenericResponse getProductsByCategoryId(String id) throws NotFoundException {
		List<ProductResponse> responseBody = new ArrayList<>();

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

	}

	/**
	 * Creates a list of products based on the provided ProductRequest list.
	 *
	 * @param productRequestList The list of ProductRequest objects containing
	 *                           product details.
	 * @return A GenericResponse containing the created products.
	 * @throws AlreadyExistException If a product with the same name already exists.
	 */
	@Transactional
	public GenericResponse createProduct(List<ProductRequest> productRequestList) throws AlreadyExistException {

		List<Product> productList = new ArrayList<>();
		for (ProductRequest productRequest : productRequestList) {

			log.info("perform Product input validation");
			partsDataValidator.isProductAlreadyExists(productRequest.getName());

			Product product = productRepository.save(prepareProduct(productRequest));
			log.info("Product created - " + productRequest.toString());

			// Save associated attributes
			if (productRequest.getAttributes() != null) {
				List<ProductAttribute> productAttributes = productRequest.getAttributes().stream()
						.map(attrReq -> ProductAttribute.builder().id(GUIDGenerator.generateGUID())
								.productId(product.getId()).attributeId(attrReq.getAttributeId())
								.attributeValue(attrReq.getAttributeValue()).build())
						.collect(Collectors.toList());

				productAttributeRepository.saveAll(productAttributes);
				log.info("Product attributes saved for product: {}", product.getId());
			}
			productList.add(product);
		}
		return GenericResponse.builder().body(productList).code("201").message("successfully Created  Product")
				.status("success").build();

	}

	/**
	 * Updates an existing product based on the provided ProductRequest.
	 *
	 * @param request The ProductRequest containing updated product details.
	 * @return A GenericResponse indicating the success of the update operation.
	 * @throws NotFoundException If the product with the given ID does not exist.
	 */
	@Transactional
	public GenericResponse updateProduct(ProductRequest request) throws NotFoundException {
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

	}

	/**
	 * Deletes a product by its ID.
	 *
	 * @param id The ID of the product to delete.
	 * @return A GenericResponse indicating the success of the deletion operation.
	 * @throws NotFoundException If the product with the given ID does not exist.
	 */
	@Transactional
	public GenericResponse deleteProductById(String id) throws NotFoundException {

		log.info("perform Product input validation");
		partsDataValidator.validateProductId(id);
		Product product = productRepository.findById(id).get();
		if (product != null) {
			productRepository.delete(product);
		}
		return GenericResponse.builder().body("Product is been Deleted").code("200").message("successfully Deleted")
				.status("success").build();

	}

	/**
	 * Searches for products by name and prepares a response.
	 *
	 * @param name The name to search for in product names.
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 */
	@Transactional
	public GenericResponse searchProductsByName(String name) {
		List<ProductResponse> responseBody = new ArrayList<>();
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
	}

	/**
	 * Searches for products by other names and prepares a response.
	 *
	 * @param otherName The other name to search for in product other names.
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 * @throws NotFoundException If no products are found with the given other name.
	 */
	@Transactional
	public GenericResponse searchProductsByOtherNames(String otherName) throws NotFoundException {
		List<ProductResponse> responseBody = new ArrayList<>();
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
	}

	/**
	 * Searches for products by tag and prepares a response.
	 *
	 * @param tag The tag to search for in product tags.
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 */
	@Transactional
	public GenericResponse searchProductsByTag(String tag) {
		List<ProductResponse> responseBody = new ArrayList<>();
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
	}

	/**
	 * Searches for products by SKU and prepares a response.
	 *
	 * @param sku The SKU to search for in product SKUs.
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 */
	@Transactional
	public GenericResponse searchProductsBySku(String sku) {

		List<ProductResponse> responseBody = new ArrayList<>();
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
	}

	/**
	 * Searches for products by category ID and prepares a response.
	 *
	 * @param categoryId The category ID to filter products.
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 */
	@Transactional
	public GenericResponse searchProductsByCategory(String categoryId) {

		List<ProductResponse> responseBody = new ArrayList<>();
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
	}

	/**
	 * Searches for products by supplier ID and prepares a response.
	 *
	 * @param supplierId The supplier ID to filter products.
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 */
	@Transactional
	public GenericResponse searchProductsBySupplier(String supplierId) {

		List<ProductResponse> responseBody = new ArrayList<>();
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
	}

	/**
	 * Searches for products by part number and prepares a response.
	 *
	 * @param partNumber The part number to search for in products.
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 */
	@Transactional
	public GenericResponse searchProductsByPartNumber(String partNumber) {

		List<ProductResponse> responseBody = new ArrayList<>();
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

	}

	/**
	 * Searches for products by year, make, and model and prepares a response.
	 *
	 * @param year  The year of the product.
	 * @param make  The make of the product.
	 * @param model The model of the product.
	 * @return A GenericResponse containing a list of ProductResponse objects.
	 */
	@Transactional
	public GenericResponse searchProductsByYearMakeModel(Integer year, String make, String model) {

		List<ProductResponse> responseBody = new ArrayList<>();
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

	}

}
