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
        return categoryAttributeService.getCategoryAttributes ( );
    }

    @Operation(summary = "Retrieving Category Attributes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all category attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all category attributes api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getCategoryAttributeById(@PathVariable String id) {
        return categoryAttributeService.getCategoryAttributesById ( id );
    }

    @Operation(summary = "Retrieving Categories by Category Id and Attribute Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving category attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve category attributes api not found", content = @Content)})
    @GetMapping("/categoryid/{categoryId}/attributeid/{attributeId}")
    public GenericResponse getCategoryAttributeByCategoryIdAndAttributeId(@PathVariable String categoryId,
                                                                          @PathVariable String attributeId) {
        return categoryAttributeService.getCategoryAttributesByCategoryIdAndAttributeId ( categoryId, attributeId );
    }

    @Operation(summary = "Retrieving Category Attributes by Category Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving category attributes", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve category attributes api not found", content = @Content)})
    @GetMapping("/categoryid/{categoryId}")
    public GenericResponse getCategoryAttributeByCategoryId(@PathVariable String categoryId) {
        return categoryAttributeService.getCategoryAttributesByCategoryId ( categoryId );

    }

    @Operation(summary = "Creating a Category with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered category details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register PartCategory api not found", content = @Content)})
    @PostMapping("")
    public GenericResponse createCategoryAttribute(@RequestBody CategoryAttributeListPayload payload) {
        return categoryAttributeService.createCategoryAttribute ( payload.getCategoryAttributeRequestList ( ) );
    }

    @Operation(summary = "Deleting Category Attribute by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull deleting category attribute", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete category attribute api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteCategoryAttributeById(@PathVariable String id) {
        return categoryAttributeService.deleteCategoryAttributeById ( id );
    }
}
