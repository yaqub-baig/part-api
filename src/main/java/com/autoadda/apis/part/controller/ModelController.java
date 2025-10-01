package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.ModelListPayload;
import com.autoadda.apis.part.request.ModelRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @Operation(summary = "Creating a Model with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered model details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register Model api not found", content = @Content)})
    @PostMapping
    public GenericResponse createModel(@RequestBody ModelListPayload payload) {
        return modelService.createModel ( payload.getModelRequestList ( ) );
    }

    @CrossOrigin
    @Operation(summary = "Retrieving All Models")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all models", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all models api not found", content = @Content)})
    @GetMapping
    public GenericResponse getAllModels() {
        return modelService.getAllModels ( );
    }

    @Operation(summary = "Retrieving Models by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all models", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all models api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getModelById(@PathVariable String id) {
        return modelService.getModelById ( id );
    }

    @Operation(summary = "Retrieving Models by Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving models", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve models api not found", content = @Content)})
    @GetMapping("/name/{name}")
    public GenericResponse getModelByName(@PathVariable String name) {
        return modelService.getModelByName ( name );
    }

    @Operation(summary = "Updating Models by Id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successfull updating models", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "update models api not found", content = @Content)})
    @PutMapping
    public GenericResponse updateModelById(@RequestBody ModelRequest model) {
        return modelService.updateModel ( model );
    }

    @Operation(summary = "Deleting Models by Id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successfull deleting models", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete model api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteModelById(@PathVariable String id) {
        return modelService.deleteModelById ( id );
    }

    @Operation(summary = "Retrieving Models by Make Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving models", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve models api not found", content = @Content)})
    @GetMapping("/make/{makeId}")
    public GenericResponse getModelsByMakeId(@PathVariable String makeId) {
        return modelService.getModelsByMakeId ( makeId );
    }

    @Operation(summary = "Retrieving Models by Vehicle Type Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving models", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve models api not found", content = @Content)})
    @GetMapping("/vehicletype/{vehicleTypeId}")
    public GenericResponse getModelsByVehicleTypeId(@PathVariable String vehicleTypeId) {
        return modelService.getModelsByVehicleTypeId ( vehicleTypeId );
    }
}
