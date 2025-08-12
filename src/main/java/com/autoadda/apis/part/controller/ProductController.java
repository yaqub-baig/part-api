package com.autoadda.apis.part.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.part.request.ProductListPayload;
import com.autoadda.apis.part.request.ProductRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	@CrossOrigin
	@Operation(summary = "Retrieving All Top Level Products")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all products api not found", content = @Content) })
	@GetMapping
	public GenericResponse getAllTopLevelProducts() {
		GenericResponse response = null;
		try {
			response = productService.getAllProducts();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Products by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve products api not found", content = @Content) })
	@GetMapping("/{id}")
	public GenericResponse getProductByName(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = productService.getProductsById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Creating a Product with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered product details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register PartProduct api not found", content = @Content) })
	@PostMapping
	public GenericResponse createPartProduct(@RequestBody ProductListPayload payload) {
		GenericResponse response = null;
		try {
			response = productService.createProduct(payload.getProductRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register product -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@Operation(summary = "Updating Products by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull updating products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "update products api not found", content = @Content) })
	@PutMapping
	public GenericResponse updateProductById(@RequestBody ProductRequest product) {
		GenericResponse response = null;
		try {
			response = productService.updateProduct(product);
			return response;
		} catch (Exception e) {
			log.error("ERROR while updating product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
	}

	@Operation(summary = "Deleting Products by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull deleting products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete product api not found", content = @Content) })
	@DeleteMapping("/{id}")
	public GenericResponse deleteProductById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = productService.deleteProductById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}

	@Operation(summary = "Searching Products by Name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve products api not found", content = @Content) })
	@GetMapping("/name/{name}")
	public GenericResponse searchProductsByName(@PathVariable String name) {
		GenericResponse response = null;
		try {
			response = productService.searchProductsByName(name);
			return response;
		} catch (Exception e) {
			log.error("ERROR while searching product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Searching Products by Other Names")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull Searching products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content) })
	@GetMapping("/othernames/{otherNames}")
	public GenericResponse searchProductsByOtherNames(@PathVariable String otherNames) {
		GenericResponse response = null;
		try {
			response = productService.searchProductsByOtherNames(otherNames);
			return response;
		} catch (Exception e) {
			log.error("ERROR while searching product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to Search").build();
	}

	@Operation(summary = "Searching Products by Tag")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull searching products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content) })
	@GetMapping("/tag/{tag}")
	public GenericResponse searchProductsByTag(@PathVariable String tag) {
		GenericResponse response = null;
		try {
			response = productService.searchProductsByTag(tag);
			return response;
		} catch (Exception e) {
			log.error("ERROR while searching product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to Search").build();
	}

	@Operation(summary = "Searching Products by SKU")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull searching products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content) })
	@GetMapping("/sku/{sku}")
	public GenericResponse searchProductsBySku(@PathVariable String sku) {
		GenericResponse response = null;
		try {
			response = productService.searchProductsBySku(sku);
			return response;
		} catch (Exception e) {
			log.error("ERROR while searching product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to Search").build();
	}

	@Operation(summary = "Searching Products by Category")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull searching products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content) })
	@GetMapping("/category/{category}")
	public GenericResponse searchProductsByCategory(@PathVariable String category) {
		GenericResponse response = null;
		try {
			response = productService.searchProductsByCategory(category);
			return response;
		} catch (Exception e) {
			log.error("ERROR while searching product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to Search").build();
	}

	@Operation(summary = "Searching Products by Supplier")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull searching products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content) })
	@GetMapping("/supplier/{supplierId}")
	public GenericResponse searchProductsBySupplier(@PathVariable String supplierId) {
		GenericResponse response = null;
		try {
			response = productService.searchProductsBySupplier(supplierId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while searching product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to Search").build();
	}

	@Operation(summary = "Searching Products by PartNumber")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull searching products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content) })
	@GetMapping("/partnumber/{partNumber}")
	public GenericResponse searchProductsByPartNumber(@PathVariable String partNumber) {
		GenericResponse response = null;
		try {
			response = productService.searchProductsByPartNumber(partNumber);
			return response;
		} catch (Exception e) {
			log.error("ERROR while searching product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to Search").build();
	}

	@Operation(summary = "Searching Products by Year Make Model")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull searching products", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content) })
	@GetMapping("/year/{year}/make/{make}/model/{model}")
	public GenericResponse searchProductsByPartNumber(@PathVariable Integer year, @PathVariable String make,
			@PathVariable String model) {
		GenericResponse response = null;
		try {
			response = productService.searchProductsByYearMakeModel(year, make, model);
			return response;
		} catch (Exception e) {
			log.error("ERROR while searching product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to Search").build();
	}

}
