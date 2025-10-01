package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.MakeListPayload;
import com.autoadda.apis.part.request.MakeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.MakeService;
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
@RequestMapping("/makes")
@RequiredArgsConstructor
public class MakeController {
    private final MakeService makeService;

    @Operation(summary = "Creating a Make with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered make details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register Make api not found", content = @Content)})
    @PostMapping
    public GenericResponse createMake(@RequestBody MakeListPayload payload) {
        return makeService.createMake ( payload.getMakeRequestList ( ) );
    }

    @CrossOrigin
    @Operation(summary = "Retrieving All Makes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all makes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all makes api not found", content = @Content)})
    @GetMapping
    public GenericResponse getAllMakes() {
        return makeService.getAllMakes ( );
    }
    @Operation(summary = "Retrieving Makes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all makes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all makes api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getMakeById(@PathVariable String id) {
        return makeService.getMakeById ( id );
    }

    @Operation(summary = "Retrieving Makes by Name")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successfull retrieving makes", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve makes api not found", content = @Content)})
    @GetMapping("/name/{name}")
    public GenericResponse getMakeByName(@PathVariable String name) {
        return makeService.getMakeByName ( name );
    }

    @Operation(summary = "Updating Makes by Id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successfull updating makes", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "update makes api not found", content = @Content)})
    @PutMapping
    public GenericResponse updateMakeById(@RequestBody MakeRequest make) {
        return makeService.updateMake ( make );
    }

    @Operation(summary = "Deleting Makes by Id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successfull deleting makes", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete make api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteMakeById(@PathVariable String id) {
        return makeService.deleteMakeById ( id );
    }
}
