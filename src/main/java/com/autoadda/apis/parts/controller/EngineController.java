package com.autoadda.apis.parts.controller;

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

import com.autoadda.apis.parts.request.EngineListPayload;
import com.autoadda.apis.parts.request.EngineRequest;
import com.autoadda.apis.parts.response.GenericResponse;
import com.autoadda.apis.parts.service.EngineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1")
public class EngineController {

	@Autowired
	private EngineService engineService;

	@Operation(summary = "Creating a Engine with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered engine details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register Engine api not found", content = @Content) })
	@PostMapping("engines")
	public GenericResponse createEngine(@RequestBody EngineListPayload paylaod) {
		GenericResponse response = null;
		try {
			response = engineService.createEngine(paylaod.getEngineRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register engine -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@CrossOrigin
	@Operation(summary = "Retrieving All Engines")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all engines", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all engines api not found", content = @Content) })
	@GetMapping("engines")
	public GenericResponse getAllEngines() {
		GenericResponse response = null;
		try {
			response = engineService.getAllEngines();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving engine-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Engines by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all engines", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all engines api not found", content = @Content) })
	@GetMapping("engines/{id}")
	public GenericResponse getEngineById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = engineService.getEngineById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving engine-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Engines by Name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving engines", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve engines api not found", content = @Content) })
	@GetMapping("engines/name/{name}")
	public GenericResponse getEngineByName(@PathVariable String name) {
		GenericResponse response = null;
		try {
			response = engineService.getEngineByName(name);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving engine-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Updating Engines by Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfull updating engines", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "update engines api not found", content = @Content) })
	@PutMapping("engines")
	public GenericResponse updateEngineById(@RequestBody EngineRequest engine) {
		GenericResponse response = null;
		try {
			response = engineService.updateEngine(engine);
			return response;
		} catch (Exception e) {
			log.error("ERROR while updating engine-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
	}

	@Operation(summary = "Deleting Engines by Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfull deleting engines", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete engine api not found", content = @Content) })
	@DeleteMapping("engines/{id}")
	public GenericResponse deleteEngineById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = engineService.deleteEngineById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting engine-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}

	@Operation(summary = "Retrieving Engines by Model Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving engines", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve engines api not found", content = @Content) })
	@GetMapping("engines/model/{modelId}")
	public GenericResponse getEnginesByModelId(@PathVariable String modelId) {
		GenericResponse response = null;
		try {
			response = engineService.getEnginesByModelId(modelId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving engine-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Engines by Fuel Type")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving engines", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve engines api not found", content = @Content) })
	@GetMapping("engines/fueltype/{fuelType}")
	public GenericResponse getEnginesByFuelTypeId(@PathVariable String fuelType) {
		GenericResponse response = null;
		try {
			response = engineService.getEnginesByFuelType(fuelType);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving engine-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Engines by Transmission Type")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving engines", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve engines api not found", content = @Content) })
	@GetMapping("engines/transmissiontype/{transmissionType}")
	public GenericResponse getEnginesByTransmissionTypeId(@PathVariable String transmissionType) {
		GenericResponse response = null;
		try {
			response = engineService.getEnginesByTransmissionType(transmissionType);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving engine-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}
}
