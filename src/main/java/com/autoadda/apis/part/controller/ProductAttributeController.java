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

import com.autoadda.apis.part.request.ProductAttributeListPayload;
import com.autoadda.apis.part.request.ProductAttributeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.ProductAttributeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/productattributes")
public class ProductAttributeController {

	@Autowired
	private ProductAttributeService productAttributeService;

	@CrossOrigin
	@Operation(summary = "Retrieving All ProductAttributes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all productattribues", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all productattribues api not found", content = @Content) })
	@GetMapping
	public GenericResponse getAllProductAttributes() {
		GenericResponse response = null;
		try {
			response = productAttributeService.getProductAttributes();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving product attributes-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving ProductAttributes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving productattribues", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve productattribues api not found", content = @Content) })
	@GetMapping("/{id}")
	public GenericResponse getProductAttributeById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = productAttributeService.getProductAttributeById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving product attributes-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}
	
	@Operation(summary = "Retrieving ProductAttributes by ProductId")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving productattribues", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve productattribues api not found", content = @Content) })
	@GetMapping("/productid/{productId}")
	public GenericResponse getProductAttributeByName(@PathVariable String productId) {
		GenericResponse response = null;
		try {
			response = productAttributeService.getProductAttributeByProductId(productId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving product attributes-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Creating a ProductAttributeAttribute with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered product details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register PartProductAttribute api not found", content = @Content) })
	@PostMapping
	public GenericResponse createProductAttribute(@RequestBody ProductAttributeListPayload payload) {
		GenericResponse response = null;
		try {
			response = productAttributeService.createProductAttribute(payload.getProductAttributeRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register product attribute -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@Operation(summary = "Updating ProductAttributes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull updating productattribues", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "update productattribues api not found", content = @Content) })
	@PutMapping
	public GenericResponse updateProductAttributeById(@RequestBody ProductAttributeRequest product) {
		GenericResponse response = null;
		try {
			response = productAttributeService.updateProductAttribute(product);
			return response;
		} catch (Exception e) {
			log.error("ERROR while updating product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
	}

	@Operation(summary = "Deleting ProductAttributes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull deleting productattribues", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete product api not found", content = @Content) })
	@DeleteMapping("/{id}")
	public GenericResponse deleteProductAttributeById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = productAttributeService.deleteProductAttributeById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting product-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}
}
