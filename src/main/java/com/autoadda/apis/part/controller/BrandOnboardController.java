package com.autoadda.apis.part.controller;


import com.autoadda.apis.part.request.BrandOnboardRequest;
import com.autoadda.apis.part.response.BrandOnboardResponse;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.BrandOnboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandOnboardController {
    private final BrandOnboardService brandOnboardService;
    @PostMapping("/onboard")
    public ResponseEntity<GenericResponse> brandOnboard(@RequestBody BrandOnboardRequest request,
                                                        @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId)
    {        return ResponseEntity.ok (brandOnboardService.brandOnboard ( request, autoAddaId ));
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<GenericResponse> updateBrand(@PathVariable Integer id, @RequestBody BrandOnboardRequest request){
        return ResponseEntity.ok (brandOnboardService.updateBrand ( id, request )
        );
    }
    @GetMapping("/isc/{referenceId}")
    public ResponseEntity<BrandOnboardResponse> getBrandById(@PathVariable String referenceId){
        return ResponseEntity.ok (brandOnboardService.getBrandById ( referenceId ));
    }

}

