package com.autoadda.apis.part.mapper;


import com.autoadda.apis.part.entity.BrandOnboard;
import com.autoadda.apis.part.request.BrandOnboardRequest;
import com.autoadda.apis.part.response.BrandOnboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandOnboardMapper {

public BrandOnboard toEntity(BrandOnboardRequest request){
    BrandOnboard brandOnboard = new BrandOnboard ( );
    brandOnboard.setBrandName ( request.getBrandName ( ) );
    brandOnboard.setModelName ( request.getModelName ( ) );
    brandOnboard.setEngineType ( request.getEngineType ( ) );
    brandOnboard.setVehicleType ( request.getVehicleType ( ) );
    brandOnboard.setIsChecked ( Boolean.FALSE );
        return brandOnboard;
}

    public BrandOnboardResponse toResponse(BrandOnboard entity) {
        BrandOnboardResponse brandOnboardResponse = new BrandOnboardResponse ();
        brandOnboardResponse.setId(entity.getId() != null ? entity.getId() : null);
        brandOnboardResponse.setBrandName(entity.getBrandName() != null ? entity.getBrandName() : null);
        brandOnboardResponse.setModelName(entity.getModelName() != null ? entity.getModelName() : null);
        brandOnboardResponse.setEngineType(entity.getEngineType() != null ? entity.getEngineType() : null);
        brandOnboardResponse.setVehicleType(entity.getVehicleType() != null ? entity.getVehicleType() : null);
        brandOnboardResponse.setRequestReferenceId(entity.getRequestReferenceId() != null ? entity.getRequestReferenceId() : null);
        brandOnboardResponse.setIsChecked(entity.getIsChecked() != null ? entity.getIsChecked() : null);
        return brandOnboardResponse;
    }


}
