package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.CategoryAttributeListPayload;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.CategoryAttributeService;
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
@RequestMapping("categoryattributes")
@RequiredArgsConstructor
public class CategoryAttributeController {

    private final CategoryAttributeService categoryAttributeService;

    @CrossOrigin
    @Operation(summary = "Retrieving All Top Level Category Attributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all category attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all category attributes api not found", content = @Content)})
    @GetMapping
    public GenericResponse getCategoryAttributes() {
        GenericResponse response = null;
        try {
            response = categoryAttributeService.getCategoryAttributes ( );
            return response;
        }
        catch ( Exception e ) {
            log.error ( "ERROR while retrieving category attributes -> " + e.getMessage ( ) );
        }
        return GenericResponse.builder ( ).status ( "failed" ).code ( "500" ).message ( "Unable to retrieve" ).build ( );
    }

    @Operation(summary = "Retrieving Category Attributes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all category attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all category attributes api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getCategoryAttributeById(@PathVariable String id) {
        GenericResponse response = null;
        try {
            response = categoryAttributeService.getCategoryAttributesById ( id );
            return response;
        }
        catch ( Exception e ) {
            log.error ( "ERROR while retrieving category attributes-> " + e.getMessage ( ) );
        }
        return GenericResponse.builder ( ).status ( "failed" ).code ( "500" ).message ( "Unable to retrieve" ).build ( );
    }

    @Operation(summary = "Retrieving Categories by Category Id and Attribute Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving category attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve category attributes api not found", content = @Content)})
    @GetMapping("/categoryid/{categoryId}/attributeid/{attributeId}")
    public GenericResponse getCategoryAttributeByCategoryIdAndAttributeId(@PathVariable String categoryId,
                                                                          @PathVariable String attributeId) {
        GenericResponse response = null;
        try {
            response = categoryAttributeService.getCategoryAttributesByCategoryIdAndAttributeId ( categoryId,
                    attributeId );
            return response;
        }
        catch ( Exception e ) {
            log.error ( "ERROR while retrieving category attribute-> " + e.getMessage ( ) );
        }
        return GenericResponse.builder ( ).status ( "failed" ).code ( "500" ).message ( "Unable to retrieve" ).build ( );
    }

    @Operation(summary = "Retrieving Category Attributes by Category Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving category attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve category attributes api not found", content = @Content)})
    @GetMapping("/categoryid/{categoryId}")
    public GenericResponse getCategoryAttributeByCategoryId(@PathVariable String categoryId) {
        GenericResponse response = null;
        try {
            response = categoryAttributeService.getCategoryAttributesByCategoryId ( categoryId );
            return response;
        }
        catch ( Exception e ) {
            log.error ( "ERROR while retrieving category attribute-> " + e.getMessage ( ) );
        }
        return GenericResponse.builder ( ).status ( "failed" ).code ( "500" ).message ( "Unable to retrieve" ).build ( );
    }

    @Operation(summary = "Creating a Category with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered category details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register PartCategory api not found", content = @Content)})
    @PostMapping("")
    public GenericResponse createCategoryAttribute(@RequestBody CategoryAttributeListPayload payload) {
        GenericResponse response = null;
        try {

            response = categoryAttributeService.createCategoryAttribute ( payload.getCategoryAttributeRequestList ( ) );
            return response;
        }
        catch ( Exception e ) {
            log.error ( "ERROR while register category attribute-> " + e.getMessage ( ) );
            return GenericResponse.builder ( ).status ( "failed" ).code ( "500" ).message ( e.getMessage ( ) ).build ( );
        }
    }

    @Operation(summary = "Deleting Category Attribute by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull deleting category attribute", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete category attribute api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteCategoryAttributeById(@PathVariable String id) {
        GenericResponse response = null;
        try {
            response = categoryAttributeService.deleteCategoryAttributeById ( id );
            return response;
        }
        catch ( Exception e ) {
            log.error ( "ERROR while deleting category attribute -> " + e.getMessage ( ) );
        }
        return GenericResponse.builder ( ).status ( "failed" ).code ( "500" ).message ( "Unable to delete" ).build ( );
    }
}
