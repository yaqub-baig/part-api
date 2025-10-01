package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.ManufacturerListPayload;
import com.autoadda.apis.part.request.ManufacturerRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.ManufacturerService;
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
@RequestMapping("/manufacturers")
@RequiredArgsConstructor
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    @Operation(summary = "Creating a Manufacturer with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered manufacturer details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register Manufacturer api not found", content = @Content)})
    @PostMapping
    public GenericResponse createManufacturer(@RequestBody ManufacturerListPayload payload) {
        return manufacturerService.createManufacturer ( payload.getManufacturerRequestList ( ) );
    }

    @CrossOrigin
    @Operation(summary = "Retrieving All Manufacturers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all manufacturers", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all manufacturers api not found", content = @Content)})
    @GetMapping
    public GenericResponse getAllManufacturers() {
        return manufacturerService.getAllManufacturers ( );
    }

    @Operation(summary = "Retrieving Manufacturers by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all manufacturers", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all manufacturers api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getManufacturerById(@PathVariable String id) {
        return manufacturerService.getManufacturerById ( id );
    }

    @Operation(summary = "Retrieving Manufacturers by Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving manufacturers", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve manufacturers api not found", content = @Content)})
    @GetMapping("/name/{name}")
    public GenericResponse getManufacturerByName(@PathVariable String name) {
        return manufacturerService.getManufacturerByName ( name );
    }

    @Operation(summary = "Updating Manufacturers by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull updating manufacturers", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "update manufacturers api not found", content = @Content)})
    @PutMapping
    public GenericResponse updateManufacturerById(@RequestBody ManufacturerRequest manufacturer) {
        return manufacturerService.updateManufacturer ( manufacturer );
    }

    @Operation(summary = "Deleting Manufacturers by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull deleting manufacturers", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete manufacturer api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteManufacturerById(@PathVariable String id) {
        return manufacturerService.deleteManufacturerById ( id );
    }
}
