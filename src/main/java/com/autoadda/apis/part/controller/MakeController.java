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

import com.autoadda.apis.part.request.MakeListPayload;
import com.autoadda.apis.part.request.MakeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.MakeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/makes")
public class MakeController {

	@Autowired
	private MakeService makeService;

	@Operation(summary = "Creating a Make with basic details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered make details", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "register Make api not found", content = @Content) })
	@PostMapping
	public GenericResponse createMake(@RequestBody MakeListPayload payload) {
		GenericResponse response = null;
		try {
			response = makeService.createMake(payload.getMakeRequestList());
			return response;
		} catch (Exception e) {
			log.error("ERROR while register make -> " + e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}

	@CrossOrigin
	@Operation(summary = "Retrieving All Makes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all makes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all makes api not found", content = @Content) })
	@GetMapping
	public GenericResponse getAllMakes() {
		GenericResponse response = null;
		try {
			response = makeService.getAllMakes();
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving make-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Makes by Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successfull retrieving all makes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve all makes api not found", content = @Content) })
	@GetMapping("/{id}")
	public GenericResponse getMakeById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = makeService.getMakeById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving make-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Retrieving Makes by Name")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfull retrieving makes", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "retrieve makes api not found", content = @Content) })
	@GetMapping("/name/{name}")
	public GenericResponse getMakeByName(@PathVariable String name) {
		GenericResponse response = null;
		try {
			response = makeService.getMakeByName(name);
			return response;
		} catch (Exception e) {
			log.error("ERROR while retrieving make-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to retrieve").build();
	}

	@Operation(summary = "Updating Makes by Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfull updating makes", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "update makes api not found", content = @Content) })
	@PutMapping
	public GenericResponse updateMakeById(@RequestBody MakeRequest make) {
		GenericResponse response = null;
		try {
			response = makeService.updateMake(make);
			return response;
		} catch (Exception e) {
			log.error("ERROR while updating make-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to update").build();
	}

	@Operation(summary = "Deleting Makes by Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfull deleting makes", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "delete make api not found", content = @Content) })
	@DeleteMapping("/{id}")
	public GenericResponse deleteMakeById(@PathVariable String id) {
		GenericResponse response = null;
		try {
			response = makeService.deleteMakeById(id);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting make-> " + e.getMessage());
		}
		return GenericResponse.builder().status("failed").code("500").message("Unable to delete").build();
	}
}
