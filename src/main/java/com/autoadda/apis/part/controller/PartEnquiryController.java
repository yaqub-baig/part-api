package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.PartEnquiryRequest;
import com.autoadda.apis.part.request.PartQuoteRequest;
import com.autoadda.apis.part.response.AcceptQuoteResponse;
import com.autoadda.apis.part.response.PartEnquiryResponse;
import com.autoadda.apis.part.service.PartEnquiryService;
import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.EnquiryRecipientStatus;
import com.autoadda.apis.part.util.EnquiryStatus;
import com.autoadda.apis.part.util.ProductCondition;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Part enquiries ("request a part"). Customer calls take vehicleOwnerId in the path and require the
 * X-Auto-Adda-Id header to be that same user; shop calls take supplierId and require the header to be the
 * shop's owner. Both answer 403 otherwise.
 */
@RestController
@RequestMapping("/enquiries")
@RequiredArgsConstructor
public class PartEnquiryController {

    private static final String USER_HEADER = "X-Auto-Adda-Id";

    private final PartEnquiryService partEnquiryService;

    // ---------------------------------------------------------------- website

    @Operation(summary = "Number of shops an enquiry with these filters would reach")
    @GetMapping("/matching-shops/count")
    public Map<String, Long> countMatchingShops(@RequestParam String makeId,
                                                @RequestParam(required = false) String modelId,
                                                @RequestParam(required = false) Integer year,
                                                @RequestParam(required = false) String categoryId,
                                                @RequestParam(required = false) List<ProductCondition> conditions) {
        return Map.of("count", partEnquiryService.countMatchingShops(makeId, modelId, year, categoryId, conditions));
    }

    @Operation(summary = "Shops covering this vehicle and part (up to 50), to choose from with supplierIds")
    @GetMapping("/matching-shops")
    public List<Map<String, Object>> matchingShops(@RequestParam String makeId,
                                                   @RequestParam(required = false) String modelId,
                                                   @RequestParam(required = false) Integer year,
                                                   @RequestParam(required = false) String categoryId,
                                                   @RequestParam(required = false) List<ProductCondition> conditions) {
        return partEnquiryService.matchingShops(makeId, modelId, year, categoryId, conditions);
    }

    // ---------------------------------------------------------------- customer

    @Operation(summary = "Send a part enquiry to the shops covering the vehicle and part")
    @PostMapping("/owner/{vehicleOwnerId}")
    public ResponseEntity<PartEnquiryResponse> create(@RequestHeader(USER_HEADER) String autoAddaId,
                                                      @PathVariable Integer vehicleOwnerId,
                                                      @RequestBody PartEnquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partEnquiryService.create(autoAddaId, vehicleOwnerId, request));
    }

    @Operation(summary = "A customer's enquiries, newest first, with their quotes")
    @GetMapping("/owner/{vehicleOwnerId}")
    public List<PartEnquiryResponse> listForOwner(@RequestHeader(USER_HEADER) String autoAddaId,
                                                  @PathVariable Integer vehicleOwnerId,
                                                  @RequestParam(required = false) EnquiryStatus status) {
        return partEnquiryService.listForOwner(autoAddaId, vehicleOwnerId, status);
    }

    @Operation(summary = "One enquiry with its quotes (cheapest first)")
    @GetMapping("/owner/{vehicleOwnerId}/{enquiryId}")
    public PartEnquiryResponse getForOwner(@RequestHeader(USER_HEADER) String autoAddaId,
                                           @PathVariable Integer vehicleOwnerId, @PathVariable Integer enquiryId) {
        return partEnquiryService.getForOwner(autoAddaId, vehicleOwnerId, enquiryId);
    }

    @Operation(summary = "Cancel an open enquiry")
    @PostMapping("/owner/{vehicleOwnerId}/{enquiryId}/cancel")
    public PartEnquiryResponse cancel(@RequestHeader(USER_HEADER) String autoAddaId,
                                      @PathVariable Integer vehicleOwnerId, @PathVariable Integer enquiryId) {
        return partEnquiryService.cancel(autoAddaId, vehicleOwnerId, enquiryId);
    }

    @Operation(summary = "Accept a quote; returns the inventoryPartId to add to the cart")
    @PostMapping("/owner/{vehicleOwnerId}/{enquiryId}/quotes/{quoteId}/accept")
    public AcceptQuoteResponse accept(@RequestHeader(USER_HEADER) String autoAddaId,
                                      @PathVariable Integer vehicleOwnerId, @PathVariable Integer enquiryId,
                                      @PathVariable Integer quoteId) {
        return partEnquiryService.accept(autoAddaId, vehicleOwnerId, enquiryId, quoteId);
    }

    // ---------------------------------------------------------------- shop

    @Operation(summary = "Enquiries sent to a shop, newest first (status: NEW, VIEWED, QUOTED, DECLINED; buyerType: CUSTOMER, GARAGE)")
    @GetMapping("/supplier/{supplierId}")
    public List<PartEnquiryResponse> listForShop(@RequestHeader(USER_HEADER) String autoAddaId,
                                                 @PathVariable Integer supplierId,
                                                 @RequestParam(required = false) EnquiryRecipientStatus status,
                                                 @RequestParam(required = false) BuyerType buyerType) {
        return partEnquiryService.listForShop(autoAddaId, supplierId, status, buyerType);
    }

    @Operation(summary = "One enquiry for a shop (marks it viewed)")
    @GetMapping("/supplier/{supplierId}/{enquiryId}")
    public PartEnquiryResponse getForShop(@RequestHeader(USER_HEADER) String autoAddaId,
                                          @PathVariable Integer supplierId, @PathVariable Integer enquiryId) {
        return partEnquiryService.getForShop(autoAddaId, supplierId, enquiryId);
    }

    @Operation(summary = "Send or replace the shop's quote")
    @PostMapping("/supplier/{supplierId}/{enquiryId}/quote")
    public PartEnquiryResponse quote(@RequestHeader(USER_HEADER) String autoAddaId,
                                     @PathVariable Integer supplierId, @PathVariable Integer enquiryId,
                                     @RequestBody PartQuoteRequest request) {
        return partEnquiryService.quote(autoAddaId, supplierId, enquiryId, request);
    }

    @Operation(summary = "Decline an enquiry (only before quoting)")
    @PostMapping("/supplier/{supplierId}/{enquiryId}/decline")
    public PartEnquiryResponse decline(@RequestHeader(USER_HEADER) String autoAddaId,
                                       @PathVariable Integer supplierId, @PathVariable Integer enquiryId) {
        return partEnquiryService.decline(autoAddaId, supplierId, enquiryId);
    }
}
