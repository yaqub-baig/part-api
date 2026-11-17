package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.PartEnquiryRequest;
import com.autoadda.apis.part.response.AcceptQuoteResponse;
import com.autoadda.apis.part.response.PartEnquiryResponse;
import com.autoadda.apis.part.service.PartEnquiryService;
import com.autoadda.apis.part.util.CallerIdentity;
import com.autoadda.apis.part.util.EnquiryStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Part enquiries sent in a garage's name. The caller (X-Auto-Adda-Id) needs the PARTS_PURCHASE permission in the
 * garage; garage-api checks it on every call (403 otherwise).
 */
@RestController
@RequestMapping("/garages/{garageId}/enquiries")
@RequiredArgsConstructor
public class GarageEnquiryController {

    private final PartEnquiryService partEnquiryService;

    @Operation(summary = "Send a part enquiry for the garage (optionally for a part request item of a job card)")
    @PostMapping
    public ResponseEntity<PartEnquiryResponse> create(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                                      @PathVariable Integer garageId,
                                                      @RequestBody PartEnquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partEnquiryService.createForGarage(autoAddaId, garageId, request));
    }

    @Operation(summary = "The garage's enquiries, newest first, with their quotes")
    @GetMapping
    public List<PartEnquiryResponse> list(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                          @PathVariable Integer garageId,
                                          @RequestParam(required = false) EnquiryStatus status) {
        return partEnquiryService.listForGarage(autoAddaId, garageId, status);
    }

    @Operation(summary = "One of the garage's enquiries with its quotes (cheapest first)")
    @GetMapping("/{enquiryId}")
    public PartEnquiryResponse get(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                   @PathVariable Integer garageId, @PathVariable Integer enquiryId) {
        return partEnquiryService.getForGarage(autoAddaId, garageId, enquiryId);
    }

    @Operation(summary = "Cancel an open enquiry")
    @PostMapping("/{enquiryId}/cancel")
    public PartEnquiryResponse cancel(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                      @PathVariable Integer garageId, @PathVariable Integer enquiryId) {
        return partEnquiryService.cancelForGarage(autoAddaId, garageId, enquiryId);
    }

    @Operation(summary = "Accept a quote; returns the inventoryPartId to order")
    @PostMapping("/{enquiryId}/quotes/{quoteId}/accept")
    public AcceptQuoteResponse accept(@RequestHeader(CallerIdentity.HEADER) String autoAddaId,
                                      @PathVariable Integer garageId, @PathVariable Integer enquiryId,
                                      @PathVariable Integer quoteId) {
        return partEnquiryService.acceptForGarage(autoAddaId, garageId, enquiryId, quoteId);
    }
}
