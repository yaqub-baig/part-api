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

import com.autoadda.apis.part.request.ModelListPayload;
import com.autoadda.apis.part.request.ModelRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.ModelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/models")
public class ModelController {

	@Autowired
	private ModelService modelService;

	@Operation(summary = "Creating a Model with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered model details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register Model api not found", content = @Content) })
	@PostMapping
	public GenericResponse createModel(@RequestBody ModelListPayload payload) {
		GenericResponse response = null;
		try {
			response = modelService.createModel(payload.getModelRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register model -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@CrossOrigin
	@Operation(summary = "Retrieving All Models")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all models", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all models api not found", content = @Content) })
	@GetMapping
	public GenericResponse getAllModels() {
		GenericResponse response = null;
		try {
			response = modelService.getAllModels();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving model-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Models by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all models", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all models api not found", content = @Content) })
	@GetMapping("/{id}")
	public GenericResponse getModelById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = modelService.getModelById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving model-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Models by Name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving models", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve models api not found", content = @Content) })
	@GetMapping("/name/{name}")
	public GenericResponse getModelByName(@PathVariable String name) {
		GenericResponse response = null;
		try {
			response = modelService.getModelByName(name);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving model-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Updating Models by Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfull updating models", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "update models api not found", content = @Content) })
	@PutMapping
	public GenericResponse updateModelById(@RequestBody ModelRequest model) {
		GenericResponse response = null;
		try {
			response = modelService.updateModel(model);
			return response;
		} catch (Exception e) {
			log.error("ERROR while updating model-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
	}

	@Operation(summary = "Deleting Models by Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfull deleting models", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete model api not found", content = @Content) })
	@DeleteMapping("/{id}")
	public GenericResponse deleteModelById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = modelService.deleteModelById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting model-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}

	@Operation(summary = "Retrieving Models by Make Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving models", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve models api not found", content = @Content) })
	@GetMapping("/make/{makeId}")
	public GenericResponse getModelsByMakeId(@PathVariable String makeId) {
		GenericResponse response = null;
		try {
			response = modelService.getModelsByMakeId(makeId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving model-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Models by Vehicle Type Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving models", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve models api not found", content = @Content) })
	@GetMapping("/vehicletype/{vehicleTypeId}")
	public GenericResponse getModelsByVehicleTypeId(@PathVariable String vehicleTypeId) {
		GenericResponse response = null;
		try {
			response = modelService.getModelsByVehicleTypeId(vehicleTypeId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving model-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}
}
