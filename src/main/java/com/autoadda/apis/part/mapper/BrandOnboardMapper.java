package com.autoadda.apis.part.mapper;


import com.autoadda.apis.part.entity.BrandOnboard;
import com.autoadda.apis.part.request.BrandOnboardRequest;
import com.autoadda.apis.part.response.BrandOnboardResponse;
import org.springframework.stereotype.Component;

@Component
public class BrandOnboardMapper {

    public BrandOnboard toEntity(BrandOnboardRequest request) {
        BrandOnboard brandOnboard = new BrandOnboard ( );
        brandOnboard.setBrandName ( request.getBrandName ( ) );
        brandOnboard.setModelName ( request.getModelName ( ) );
        brandOnboard.setEngineType ( request.getEngineType ( ) );
        brandOnboard.setVehicleType ( request.getVehicleType ( ) );
        brandOnboard.setIsChecked ( Boolean.FALSE );
        return brandOnboard;
    }

    public BrandOnboardResponse toResponse(BrandOnboard entity) {
        BrandOnboardResponse response = new BrandOnboardResponse ( );
        response.setId ( entity.getId ( ) );
        response.setBrandName ( entity.getBrandName ( ) );
        response.setModelName ( entity.getModelName ( ) );
        response.setEngineType ( entity.getEngineType ( ) );
        response.setVehicleType ( entity.getVehicleType ( ) );
        response.setRequestReferenceId ( entity.getRequestReferenceId ( ) );
        response.setIsChecked ( entity.getIsChecked ( ) );

        // New workflow fields
        response.setRequestStatus ( entity.getRequestStatus ( ) );
        response.setRequestBy ( entity.getRequestBy ( ) );
        response.setCreatedAt ( entity.getCreatedAt ( ) );
        response.setUpdatedAt ( entity.getUpdatedAt ( ) );
        response.setApprovedBy ( entity.getApprovedBy ( ) );
        response.setApprovedAt ( entity.getApprovedAt ( ) );
        response.setRejectedBy ( entity.getRejectedBy ( ) );
        response.setRejectedAt ( entity.getRejectedAt ( ) );
        response.setRejectionReason ( entity.getRejectionReason ( ) );

        return response;
    }


}
