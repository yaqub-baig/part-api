package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.AttributeListPayload;
import com.autoadda.apis.part.request.AttributeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.AttributeService;
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
@RequestMapping("/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;

    @Operation(summary = "Creating a Attribute with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered attribute details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register Attribute api not found", content = @Content)})
    @PostMapping
    public GenericResponse createAttribute(@RequestBody AttributeListPayload payload) {

        return attributeService.createAttribute ( payload.getAttributeRequestList ( ) );

    }

    @CrossOrigin
    @Operation(summary = "Retrieving All Attributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all attributes api not found", content = @Content)})
    @GetMapping
    public GenericResponse getAllAttributes() {
        return attributeService.getAllAttributes ( );
    }

    @Operation(summary = "Retrieving Attributes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all attributes api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getAttributeById(@PathVariable String id) {
        return attributeService.getAttributeById ( id );
    }

    @Operation(summary = "Retrieving Attributes by Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve attributes api not found", content = @Content)})
    @GetMapping("/name/{name}")
    public GenericResponse getAttributeByName(@PathVariable String name) {
        return attributeService.getAttributeByName ( name );
    }

    @Operation(summary = "Updating Attributes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull updating attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "update attributes api not found", content = @Content)})
    @PutMapping
    public GenericResponse updateAttributeById(@RequestBody AttributeRequest attribute) {
        return attributeService.updateAttribute ( attribute );
    }

    @Operation(summary = "Deleting Attributes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull deleting attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete attribute api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteAttributeById(@PathVariable String id) {
        return attributeService.deleteAttributeById ( id );
    }
}
