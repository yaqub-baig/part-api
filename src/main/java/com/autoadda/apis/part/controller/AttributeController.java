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

import com.autoadda.apis.part.request.AttributeListPayload;
import com.autoadda.apis.part.request.AttributeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.AttributeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/attributes")
public class AttributeController {

	@Autowired
	private AttributeService attributeService;

	@Operation(summary = "Creating a Attribute with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered attribute details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register Attribute api not found", content = @Content) })
	@PostMapping
	public GenericResponse createAttribute(@RequestBody AttributeListPayload payload) {
		GenericResponse response = null;
		try {
			response = attributeService.createAttribute(payload.getAttributeRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register attribute -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@CrossOrigin
	@Operation(summary = "Retrieving All Attributes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all attributes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all attributes api not found", content = @Content) })
	@GetMapping
	public GenericResponse getAllAttributes() {
		GenericResponse response = null;
		try {
			response = attributeService.getAllAttributes();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving attribute-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Attributes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all attributes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all attributes api not found", content = @Content) })
	@GetMapping("/{id}")
	public GenericResponse getAttributeById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = attributeService.getAttributeById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving attribute-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Attributes by Name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving attributes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve attributes api not found", content = @Content) })
	@GetMapping("/name/{name}")
	public GenericResponse getAttributeByName(@PathVariable String name) {
		GenericResponse response = null;
		try {
			response = attributeService.getAttributeByName(name);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving attribute-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Updating Attributes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull updating attributes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "update attributes api not found", content = @Content) })
	@PutMapping
	public GenericResponse updateAttributeById(@RequestBody AttributeRequest attribute) {
		GenericResponse response = null;
		try {
			response = attributeService.updateAttribute(attribute);
			return response;
		} catch (Exception e) {
			log.error("ERROR while updating attribute-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
	}

	@Operation(summary = "Deleting Attributes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull deleting attributes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete attribute api not found", content = @Content) })
	@DeleteMapping("/{id}")
	public GenericResponse deleteAttributeById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = attributeService.deleteAttributeById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting attribute-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}
}
