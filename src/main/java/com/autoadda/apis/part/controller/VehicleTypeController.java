package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.VehicleTypeListPayload;
import com.autoadda.apis.part.request.VehicleTypeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.VehicleTypeService;
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
@RequestMapping("/vehicletypes")
@RequiredArgsConstructor
public class VehicleTypeController {


    private final VehicleTypeService vehicleTypeService;

    @Operation(summary = "Creating a VehicleType with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered vehicleType details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register VehicleType api not found", content = @Content)})
    @PostMapping
    public GenericResponse createVehicleType(@RequestBody VehicleTypeListPayload payload) {
        return vehicleTypeService.createVehicleType ( payload.getVehicleTypeRequestList ( ) );
    }

    @Operation(summary = "Retrieving All vehicleTypes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all vehicleTypes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all vehicleTypes api not found", content = @Content)})
    @GetMapping
    public GenericResponse getAllCategories() {
        return vehicleTypeService.getAllVehicleTypes ( );
    }

    @Operation(summary = "Retrieving vehicleTypes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all vehicleTypes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all vehicleTypes api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getVehicleTypeById(@PathVariable String id) {
        return vehicleTypeService.getVehicleTypeById ( id );
    }

    @Operation(summary = "Retrieving VehicleTypes by Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving vehicleTypes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve vehicleTypes api not found", content = @Content)})
    @GetMapping("/name/{name}")
    public GenericResponse getVehicleTypeByName(@PathVariable String name) {
        return vehicleTypeService.getVehicleTypeByName ( name );
    }

    @Operation(summary = "Updating VehicleTypes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull updating vehicleTypes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "update vehicleTypes api not found", content = @Content)})
    @PutMapping
    public GenericResponse updateVehicleTypeById(@RequestBody VehicleTypeRequest vehicleType) {
        return vehicleTypeService.updateVehicleType ( vehicleType );
    }

    @Operation(summary = "Deleting VehicleTypes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull deleting vehicleTypes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete vehicleType api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteVehicleTypeById(@PathVariable String id) {
        return vehicleTypeService.deleteVehicleTypeById ( id );

    }
}
