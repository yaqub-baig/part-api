package com.autoadda.apis.parts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.parts.request.SupplierListPayload;
import com.autoadda.apis.parts.request.SupplierRequest;
import com.autoadda.apis.parts.response.GenericResponse;
import com.autoadda.apis.parts.service.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1")
public class SupplierController {

	@Autowired
	private SupplierService supplierService;

	@Operation(summary = "Creating a Supplier with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered supplier details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register Supplier api not found", content = @Content) })
	@PostMapping("suppliers")
	public GenericResponse createSupplier(@RequestBody SupplierListPayload payload) {
		GenericResponse response = null;
		try {
			response = supplierService.createSupplier(payload.getSupplierRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register supplier -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@Operation(summary = "Retrieving All suppliers")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all suppliers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all suppliers api not found", content = @Content) })
	@GetMapping("suppliers")
	public GenericResponse getAllCategories() {
		GenericResponse response = null;
		try {
			response = supplierService.getAllSuppliers();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving supplier-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving suppliers by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all suppliers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all suppliers api not found", content = @Content) })
	@GetMapping("suppliers/{id}")
	public GenericResponse getSupplierById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = supplierService.getSupplierById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving supplier-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Suppliers by Name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving suppliers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve suppliers api not found", content = @Content) })
	@GetMapping("suppliers/name/{name}")
	public GenericResponse getSupplierByName(@PathVariable String name) {
		GenericResponse response = null;
		try {
			response = supplierService.getSupplierByName(name);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving supplier-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Updating Suppliers by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull updating suppliers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "update suppliers api not found", content = @Content) })
	@PutMapping("suppliers")
	public GenericResponse updateSupplierById(@RequestBody SupplierRequest supplier) {
		GenericResponse response = null;
		try {
			response = supplierService.updateSupplier(supplier);
			return response;
		} catch (Exception e) {
			log.error("ERROR while updating supplier-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
	}

	@Operation(summary = "Deleting Suppliers by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull deleting suppliers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete supplier api not found", content = @Content) })
	@DeleteMapping("suppliers/{id}")
	public GenericResponse deleteSupplierById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = supplierService.deleteSupplierById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting supplier-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}
}
