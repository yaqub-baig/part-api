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

import com.autoadda.apis.part.request.ManufacturerListPayload;
import com.autoadda.apis.part.request.ManufacturerRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.ManufacturerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/manufacturers")
public class ManufacturerController {

	@Autowired
	private ManufacturerService manufacturerService;

	@Operation(summary = "Creating a Manufacturer with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered manufacturer details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register Manufacturer api not found", content = @Content) })
	@PostMapping
	public GenericResponse createManufacturer(@RequestBody ManufacturerListPayload payload) {
		GenericResponse response = null;
		try {
			response = manufacturerService.createManufacturer(payload.getManufacturerRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register manufacturer -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@CrossOrigin
	@Operation(summary = "Retrieving All Manufacturers")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all manufacturers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all manufacturers api not found", content = @Content) })
	@GetMapping
	public GenericResponse getAllManufacturers() {
		GenericResponse response = null;
		try {
			response = manufacturerService.getAllManufacturers();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving manufacturer-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Manufacturers by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all manufacturers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all manufacturers api not found", content = @Content) })
	@GetMapping("/{id}")
	public GenericResponse getManufacturerById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = manufacturerService.getManufacturerById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving manufacturer-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Manufacturers by Name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving manufacturers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve manufacturers api not found", content = @Content) })
	@GetMapping("/name/{name}")
	public GenericResponse getManufacturerByName(@PathVariable String name) {
		GenericResponse response = null;
		try {
			response = manufacturerService.getManufacturerByName(name);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving manufacturer-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Updating Manufacturers by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull updating manufacturers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "update manufacturers api not found", content = @Content) })
	@PutMapping
	public GenericResponse updateManufacturerById(@RequestBody ManufacturerRequest manufacturer) {
		GenericResponse response = null;
		try {
			response = manufacturerService.updateManufacturer(manufacturer);
			return response;
		} catch (Exception e) {
			log.error("ERROR while updating manufacturer-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
	}

	@Operation(summary = "Deleting Manufacturers by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull deleting manufacturers", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete manufacturer api not found", content = @Content) })
	@DeleteMapping("/{id}")
	public GenericResponse deleteManufacturerById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = manufacturerService.deleteManufacturerById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting manufacturer-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}
}
