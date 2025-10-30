package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.EngineListPayload;
import com.autoadda.apis.part.request.EngineRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.EngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/engines")
@RequiredArgsConstructor
public class EngineController {

    private final EngineService engineService;

    @Operation(summary = "Creating a Engine with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered engine details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register Engine api not found", content = @Content)})
    @PostMapping
    public GenericResponse createEngine(@RequestBody EngineListPayload paylaod) {
        return engineService.createEngine ( paylaod.getEngineRequestList ( ) );
    }


    @Operation(summary = "Retrieving All Engines")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all engines", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all engines api not found", content = @Content)})
    @GetMapping
    public GenericResponse getAllEngines() {
        return engineService.getAllEngines ( );

    }

    @Operation(summary = "Retrieving Engines by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all engines", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all engines api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getEngineById(@PathVariable String id) {
        return engineService.getEngineById ( id );
    }

    @Operation(summary = "Retrieving Engines by Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving engines", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve engines api not found", content = @Content)})
    @GetMapping("/name/{name}")
    public GenericResponse getEngineByName(@PathVariable String name) {
        return engineService.getEngineByName ( name );
    }

    @Operation(summary = "Updating Engines by Id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successfull updating engines", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "update engines api not found", content = @Content)})
    @PutMapping
    public GenericResponse updateEngineById(@RequestBody EngineRequest engine) {
        return engineService.updateEngine ( engine );
    }

    @Operation(summary = "Deleting Engines by Id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successfull deleting engines", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete engine api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteEngineById(@PathVariable String id) {
        return engineService.deleteEngineById ( id );
    }

    @Operation(summary = "Retrieving Engines by Model Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving engines", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve engines api not found", content = @Content)})
    @GetMapping("/model/{modelId}")
    public GenericResponse getEnginesByModelId(@PathVariable String modelId) {
        return engineService.getEnginesByModelId ( modelId );
    }

    @Operation(summary = "Retrieving Engines by Fuel Type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving engines", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve engines api not found", content = @Content)})
    @GetMapping("/fueltype/{fuelType}")
    public GenericResponse getEnginesByFuelTypeId(@PathVariable String fuelType) {
        return engineService.getEnginesByFuelType ( fuelType );
    }

    @Operation(summary = "Retrieving Engines by Transmission Type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving engines", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve engines api not found", content = @Content)})
    @GetMapping("/transmissiontype/{transmissionType}")
    public GenericResponse getEnginesByTransmissionTypeId(@PathVariable String transmissionType) {
        return engineService.getEnginesByTransmissionType ( transmissionType );
    }
}
