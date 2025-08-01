package com.autoadda.apis.parts.controller;

import com.autoadda.apis.parts.request.SupplierRequest;
import com.autoadda.apis.parts.entity.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autoadda.apis.parts.service.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

	private final SupplierService supplierService;

	@Operation(summary = "Creating a Supplier with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered supplier details"),
			@ApiResponse(responseCode = "404", description = "register Supplier api not found", content = @Content) })
	@PostMapping
	public ResponseEntity<Supplier> createSupplier(
			@RequestHeader(value = "X-User-Id") String userId,
			@RequestHeader(value = "X-User-Role") String userRole,
			@RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
			@RequestBody SupplierRequest payload) {
		return supplierService.createSupplier(payload,autoAddaId);
	}

//	@Operation(summary = "Retrieving All suppliers")
//	@ApiResponses(value = {
//			@ApiResponse(responseCode = "200", description = "successfull retrieving all suppliers", content = {
//					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
//			@ApiResponse(responseCode = "404", description = "retrieve all suppliers api not found", content = @Content) })
//	@GetMapping
//	public GenericResponse getAllCategories() {
//		GenericResponse response = null;
//		try {
//			response = supplierService.getAllSuppliers();
//			return response;
//		} catch (Exception e) {
//			log.error("ERROR while retrieving supplier-> " + e.getMessage());
//		}
//		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
//	}
//
//	@Operation(summary = "Retrieving suppliers by Id")
//	@ApiResponses(value = {
//			@ApiResponse(responseCode = "200", description = "successfull retrieving all suppliers", content = {
//					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
//			@ApiResponse(responseCode = "404", description = "retrieve all suppliers api not found", content = @Content) })
//	@GetMapping("/{id}")
//	public GenericResponse getSupplierById(@PathVariable String id) {
//		GenericResponse response = null;
//		try {
//			response = supplierService.getSupplierById(id);
//			return response;
//		} catch (Exception e) {
//			log.error("ERROR while retrieving supplier-> " + e.getMessage());
//		}
//		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
//	}
//
//	@Operation(summary = "Retrieving Suppliers by Name")
//	@ApiResponses(value = {
//			@ApiResponse(responseCode = "200", description = "successfull retrieving suppliers", content = {
//					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
//			@ApiResponse(responseCode = "404", description = "retrieve suppliers api not found", content = @Content) })
//	@GetMapping("/name/{name}")
//	public GenericResponse getSupplierByName(@PathVariable String name) {
//		GenericResponse response = null;
//		try {
//			response = supplierService.getSupplierByName(name);
//			return response;
//		} catch (Exception e) {
//			log.error("ERROR while retrieving supplier-> " + e.getMessage());
//		}
//		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
//	}
//
//	@Operation(summary = "Updating Suppliers by Id")
//	@ApiResponses(value = {
//			@ApiResponse(responseCode = "200", description = "successfull updating suppliers", content = {
//					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
//			@ApiResponse(responseCode = "404", description = "update suppliers api not found", content = @Content) })
//	@PutMapping
//	public GenericResponse updateSupplierById(@RequestBody SupplierRequest supplier) {
//		GenericResponse response = null;
//		try {
//			response = supplierService.updateSupplier(supplier);
//			return response;
//		} catch (Exception e) {
//			log.error("ERROR while updating supplier-> " + e.getMessage());
//		}
//		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
//	}
//
//	@Operation(summary = "Deleting Suppliers by Id")
//	@ApiResponses(value = {
//			@ApiResponse(responseCode = "200", description = "successfull deleting suppliers", content = {
//					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
//			@ApiResponse(responseCode = "404", description = "delete supplier api not found", content = @Content) })
//	@DeleteMapping("/{id}")
//	public GenericResponse deleteSupplierById(@PathVariable String id) {
//		GenericResponse response = null;
//		try {
//			response = supplierService.deleteSupplierById(id);
//			return response;
//		} catch (Exception e) {
//			log.error("ERROR while deleting supplier-> " + e.getMessage());
//		}
//		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
//	}
}
