package com.autoadda.apis.part.controller;


import com.autoadda.apis.part.request.ApproveRejectRequest;
import com.autoadda.apis.part.request.BrandOnboardRequest;
import com.autoadda.apis.part.response.BrandOnboardResponse;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.BrandOnboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandOnboardController {
    private final BrandOnboardService brandOnboardService;

    @PostMapping("/onboard")
    public ResponseEntity<GenericResponse> brandOnboard(@RequestBody BrandOnboardRequest request,
                                                        @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok ( brandOnboardService.brandOnboard ( request, autoAddaId ) );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenericResponse> updateBrand(@PathVariable Integer id, @RequestBody BrandOnboardRequest request) {
        return ResponseEntity.ok ( brandOnboardService.updateBrand ( id, request )
        );
    }

    @GetMapping("/isc/{referenceId}")
    public ResponseEntity<BrandOnboardResponse> getBrandById(@PathVariable String referenceId) {
        return ResponseEntity.ok ( brandOnboardService.getBrandById ( referenceId ) );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<GenericResponse> deleteBrand(@PathVariable Integer id) {
        return ResponseEntity.ok ( brandOnboardService.deleteBrand ( id ) );
    }

    @GetMapping("/all")
    public ResponseEntity<GenericResponse> getAllBrands() {
        return ResponseEntity.ok ( brandOnboardService.getAllBrands ( ) );
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<GenericResponse> approveRequest(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auto-Adda-Id") String adminId) {
        return ResponseEntity.ok ( brandOnboardService.approveRequest ( id, adminId ) );
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<GenericResponse> rejectRequest(
            @PathVariable Integer id,
            @RequestBody ApproveRejectRequest request,
            @RequestHeader(value = "X-Auto-Adda-Id") String adminId) {
        return ResponseEntity.ok ( brandOnboardService.rejectRequest (
                id, adminId, request.getRejectionReason ( ) ) );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<GenericResponse> getRequestsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok ( brandOnboardService.getRequestsByStatus ( status ) );
    }

}

