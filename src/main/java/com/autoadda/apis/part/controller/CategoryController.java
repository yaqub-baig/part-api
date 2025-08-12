package com.autoadda.apis.part.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.part.request.CategoryListPayload;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@CrossOrigin
	@Operation(summary = "Retrieving All Top Level Categories")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all categories", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all categories api not found", content = @Content) })
	@GetMapping("/toplevelcategories")
	public GenericResponse getAllTopLevelCategories() {
		GenericResponse response = null;
		try {
			response = categoryService.getTopLevelCategories();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving category-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Sub-Categories by Parent Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all categories", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all categories api not found", content = @Content) })
	@GetMapping("/{id}")
	public GenericResponse getCategoryById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = categoryService.getSubCategoriesByParentId(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving category-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Categories by Name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving categories", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve categories api not found", content = @Content) })
	@GetMapping("/name/{name}")
	public GenericResponse getCategoryByName(@PathVariable String name) {
		GenericResponse response = null;
		try {
			response = categoryService.getCategoryByName(name);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving category-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Creating a Category with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered category details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register PartCategory api not found", content = @Content) })
	@PostMapping
	public GenericResponse createPartCategory(@RequestBody CategoryListPayload payload) {
		GenericResponse response = null;
		try {
			response = categoryService.createCategory(payload.getCategoryRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register category -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@Operation(summary = "Deleting Categories by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull deleting categories", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete category api not found", content = @Content) })
	@DeleteMapping("/{id}")
	public GenericResponse deleteCategoryById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = categoryService.deleteCategoryById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting category-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}
}
