package com.autoadda.apis.part.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.part.request.VehicleTypeListPayload;
import com.autoadda.apis.part.request.VehicleTypeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.VehicleTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/vehicletypes")
public class VehicleTypeController {

	@Autowired
	private VehicleTypeService vehicleTypeService;

	@Operation(summary = "Creating a VehicleType with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered vehicleType details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register VehicleType api not found", content = @Content) })
	@PostMapping
	public GenericResponse createVehicleType(@RequestBody VehicleTypeListPayload payload) {
		GenericResponse response = null;
		try {
			response = vehicleTypeService.createVehicleType(payload.getVehicleTypeRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register vehicleType -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@Operation(summary = "Retrieving All vehicleTypes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all vehicleTypes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all vehicleTypes api not found", content = @Content) })
	@GetMapping
	public GenericResponse getAllCategories() {
		GenericResponse response = null;
		try {
			response = vehicleTypeService.getAllVehicleTypes();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving vehicleType-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving vehicleTypes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all vehicleTypes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all vehicleTypes api not found", content = @Content) })
	@GetMapping("/{id}")
	public GenericResponse getVehicleTypeById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = vehicleTypeService.getVehicleTypeById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving vehicleType-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving VehicleTypes by Name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving vehicleTypes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve vehicleTypes api not found", content = @Content) })
	@GetMapping("/name/{name}")
	public GenericResponse getVehicleTypeByName(@PathVariable String name) {
		GenericResponse response = null;
		try {
			response = vehicleTypeService.getVehicleTypeByName(name);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving vehicleType-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Updating VehicleTypes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull updating vehicleTypes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "update vehicleTypes api not found", content = @Content) })
	@PutMapping
	public GenericResponse updateVehicleTypeById(@RequestBody VehicleTypeRequest vehicleType) {
		GenericResponse response = null;
		try {
			response = vehicleTypeService.updateVehicleType(vehicleType);
			return response;
		} catch (Exception e) {
			log.error("ERROR while updating vehicleType-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
	}

	@Operation(summary = "Deleting VehicleTypes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull deleting vehicleTypes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete vehicleType api not found", content = @Content) })
	@DeleteMapping("/{id}")
	public GenericResponse deleteVehicleTypeById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = vehicleTypeService.deleteVehicleTypeById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting vehicleType-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}
}
