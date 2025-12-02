package com.autoadda.apis.part.service;


import com.autoadda.apis.part.clients.AuthClient;
import com.autoadda.apis.part.entity.BrandOnboard;
import com.autoadda.apis.part.entity.Engine;
import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.mapper.BrandOnboardMapper;
import com.autoadda.apis.part.repository.BrandOnboardRepository;
import com.autoadda.apis.part.repository.EngineRepository;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.request.BrandOnboardRequest;
import com.autoadda.apis.part.request.SmsRequest;
import com.autoadda.apis.part.response.BrandOnboardResponse;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.util.BrandRequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandOnboardService {
    public static final String SUCCESS = "success";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String FAILED_TO_SEND_SMS_NOTIFICATION_FOR_APPROVED_REQUEST = "Failed to send SMS notification for approved request {}: {}";
    private final BrandOnboardRepository brandOnboardRepository;
    private final BrandOnboardMapper brandOnboardMapper;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final EngineRepository engineRepository;
    private final AuthClient authClient;

    public GenericResponse brandOnboard(BrandOnboardRequest request, String autoAddaId) {
        Make brand = makeRepository.findByName ( request.getBrandName ( ) );
        if ( brand != null ) {
            Model existingModel = modelRepository.findByName ( request.getModelName ( ) );
            if ( existingModel != null && existingModel.getMakeId ( ).equals ( brand.getId ( ) ) ) {
                    // Check if engine type exists for this model
                    List<Engine> enginesForModel = engineRepository.findByModelId ( existingModel.getId ( ) );
                    boolean engineTypeExists = enginesForModel.stream ( )
                            .anyMatch ( engine -> engine.getFuelType ( ).equalsIgnoreCase ( request.getEngineType ( ) ) );
                    if ( engineTypeExists ) {
                        throw new ResourceNotFoundException ( "Brand, model, and engine type already exist" );
                    }
                }

        }

        BrandOnboard entity = brandOnboardMapper.toEntity ( request );
        entity.setRequestBy ( Integer.parseInt ( autoAddaId ) );
        entity.setRequestReferenceId ( generateRequestReferenceId ( entity ) );

        BrandOnboard save = brandOnboardRepository.save ( entity );
        return new GenericResponse (SUCCESS, "200", "Your request is successfully taken our team will look into it and add brand if needed", brandOnboardMapper.toResponse ( save ) );
    }

    public GenericResponse updateBrand(Integer id, BrandOnboardRequest request) {
        BrandOnboard tempVehicle = brandOnboardRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "update Brand request not found" ) );
        tempVehicle.setBrandName ( request.getBrandName ( ) );
        tempVehicle.setModelName ( request.getModelName ( ) );
        tempVehicle.setEngineType ( request.getEngineType ( ) );
        BrandOnboard save = brandOnboardRepository.save ( tempVehicle );
        return new GenericResponse ( SUCCESS, "200", "Brand updated Successfully", brandOnboardMapper.toResponse ( save ) );
    }

    public String generateRequestReferenceId(BrandOnboard entity) {
        // Get current year and month
        LocalDateTime now = LocalDateTime.now ( );
        String yearMonth = now.format ( DateTimeFormatter.ofPattern ( "yyMM" ) );

        // Get brand name and model name (keep original names for frontend parsing)
        String brandName = entity.getBrandName ( ) != null ? entity.getBrandName ( ).trim ( ) : UNKNOWN;
        String modelName = entity.getModelName ( ) != null ? entity.getModelName ( ).trim ( ) : UNKNOWN;
        String engineType = entity.getEngineType ( ) != null ? entity.getEngineType ( ).trim ( ) : UNKNOWN;

        String globalPrefix = String.format ( "BR-%s-", yearMonth );
        List<String> allCodes = brandOnboardRepository.findAllCodesForGlobalSequence ( globalPrefix );

        int globalSequence = 1;
        if ( ! allCodes.isEmpty ( ) ) {
            // Find the highest sequence number across all brand-model combinations
            int maxSequence = 0;
            for (String code : allCodes) {
                try {
                    // Extract sequence from pattern: BRAND_MODEL_BR-YYMM-XXXX
                    String[] parts = code.split ( "-" );
                    if ( parts.length >= 2 ) {
                        int sequence = Integer.parseInt ( parts[parts.length - 1] );
                        maxSequence = Math.max ( maxSequence, sequence );
                    }
                }
                catch ( NumberFormatException e ) {
                    // Ignore invalid formats
                }
            }
            globalSequence = maxSequence + 1;
        }

        return String.format ( "%s_%s_%s_BR-%s-%04d", brandName.toUpperCase ( ), modelName.toUpperCase ( ), engineType.toUpperCase ( ), yearMonth, globalSequence );

    }

    public BrandOnboardResponse getBrandById(String referenceId) {
        return brandOnboardMapper.toResponse ( brandOnboardRepository.findByRequestReferenceId ( referenceId ).orElseThrow ( () -> new ResourceNotFoundException ( "Brand not found" ) ) );
    }

    public GenericResponse getAllBrands() {
        List<BrandOnboard> brands = brandOnboardRepository.findAll ( );
        List<BrandOnboardResponse> responses = brands.stream ( )
                .map ( brandOnboardMapper::toResponse )
                .toList ( );
        return new GenericResponse ( SUCCESS, "200", "Brands retrieved successfully", responses );
    }

    public GenericResponse deleteBrand(Integer id) {
        brandOnboardRepository.deleteById(id);
        return new GenericResponse ( SUCCESS, "200", "Brands request resolved successfully",null );
    }

    public GenericResponse approveRequest(Integer id, String adminId) {
        BrandOnboard request = brandOnboardRepository.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Brand onboard request not found" ) );

        if ( request.getRequestStatus ( ) != BrandRequestStatus.PENDING ) {
            throw new IllegalStateException ( "Only pending requests can be approved" );
        }

        request.setRequestStatus ( BrandRequestStatus.APPROVED );
        request.setApprovedBy ( Integer.parseInt ( adminId ) );
        request.setApprovedAt ( LocalDateTime.now ( ) );

        BrandOnboard saved = brandOnboardRepository.save ( request );

        // Send SMS notification to user
        sendApprovalSms ( saved );

        return new GenericResponse ( SUCCESS, "200",
                "Brand request approved successfully. Admin can now manually create Make/Model/Engine records.",
                brandOnboardMapper.toResponse ( saved ) );
    }

    public GenericResponse rejectRequest(Integer id, String adminId, String rejectionReason) {
        if ( rejectionReason == null || rejectionReason.trim ( ).isEmpty ( ) ) {
            throw new IllegalArgumentException ( "Rejection reason is required" );
        }

        BrandOnboard request = brandOnboardRepository.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Brand onboard request not found" ) );

        if ( request.getRequestStatus ( ) != BrandRequestStatus.PENDING ) {
            throw new IllegalStateException ( "Only pending requests can be rejected" );
        }

        request.setRequestStatus ( com.autoadda.apis.part.util.BrandRequestStatus.REJECTED );
        request.setRejectedBy ( Integer.parseInt ( adminId ) );
        request.setRejectedAt ( LocalDateTime.now ( ) );
        request.setRejectionReason ( rejectionReason );

        BrandOnboard saved = brandOnboardRepository.save ( request );

        // Send SMS notification to user
        sendRejectionSms ( saved );

        return new GenericResponse ( SUCCESS, "200",
                "Brand request rejected successfully",
                brandOnboardMapper.toResponse ( saved ) );
    }

    public GenericResponse getRequestsByStatus(String status) {
        com.autoadda.apis.part.util.BrandRequestStatus requestStatus;
        try {
            requestStatus = com.autoadda.apis.part.util.BrandRequestStatus.valueOf ( status.toUpperCase ( ) );
        }
        catch ( IllegalArgumentException e ) {
            throw new IllegalArgumentException ( "Invalid status: " + status +
                    ". Valid values are: PENDING, APPROVED, REJECTED" );
        }

        List<BrandOnboard> requests = brandOnboardRepository
                .findByRequestStatusOrderByCreatedAtDesc ( requestStatus );
        List<BrandOnboardResponse> responses = requests.stream ( )
                .map ( brandOnboardMapper::toResponse )
                .toList ( );

        return new GenericResponse ( SUCCESS, "200",
                "Requests retrieved successfully", responses );
    }

    /**
     * Send SMS notification to user when their brand request is approved
     *
     * @param brandOnboard the approved brand onboard request
     */
    private void sendApprovalSms(BrandOnboard brandOnboard) {
        try {
            String message = String.format (
                    "Dear User, Your brand request for %s %s (%s) has been APPROVED! " +
                            "Reference ID: %s. The brand will be available in our system soon. Thank you for your contribution!",
                    brandOnboard.getBrandName ( ),
                    brandOnboard.getModelName ( ),
                    brandOnboard.getEngineType ( ),
                    brandOnboard.getRequestReferenceId ( )
            );

            SmsRequest smsRequest = new SmsRequest ( null, message );
            GenericResponse genericResponse = authClient.sendSms(brandOnboard.getRequestBy(), smsRequest);
            boolean isSuccess = genericResponse.getStatus().equals("200");
            if (!isSuccess) {
                log.error (FAILED_TO_SEND_SMS_NOTIFICATION_FOR_APPROVED_REQUEST,
                        brandOnboard.getRequestReferenceId ( ), genericResponse.getMessage ( ) );
            }
            log.info ( "SMS notification sent successfully for approved request: {}", brandOnboard.getRequestReferenceId ( ) );
        }
        catch ( Exception e ) {
            log.error (FAILED_TO_SEND_SMS_NOTIFICATION_FOR_APPROVED_REQUEST,
                    brandOnboard.getRequestReferenceId ( ), e.getMessage ( ) );
        }
    }

    /**
     * Send SMS notification to user when their brand request is rejected
     *
     * @param brandOnboard the rejected brand onboard request
     */
    private void sendRejectionSms(BrandOnboard brandOnboard) {
        try {
            String message = String.format (
                    "Dear User, Your brand request for %s %s (%s) has been REJECTED. " +
                            "Reference ID: %s. Reason: %s. Please contact support for more details.",
                    brandOnboard.getBrandName ( ),
                    brandOnboard.getModelName ( ),
                    brandOnboard.getEngineType ( ),
                    brandOnboard.getRequestReferenceId ( ),
                    brandOnboard.getRejectionReason ( )
            );

            SmsRequest smsRequest = new SmsRequest ( null, message );
            GenericResponse genericResponse = authClient.sendSms ( brandOnboard.getRequestBy ( ), smsRequest );
            boolean isSuccess = genericResponse.getStatus().equals("200");
            if (!isSuccess) {
                log.error (FAILED_TO_SEND_SMS_NOTIFICATION_FOR_APPROVED_REQUEST,
                        brandOnboard.getRequestReferenceId ( ), genericResponse.getMessage ( ) );
            }
            log.info ( "SMS notification sent successfully for rejected request: {}", brandOnboard.getRequestReferenceId ( ) );
        }
        catch ( Exception e ) {
            log.error ( "Failed to send SMS notification for rejected request {}: {}",
                    brandOnboard.getRequestReferenceId ( ), e.getMessage ( ) );
            // Don't throw exception - SMS failure shouldn't fail the rejection process
        }
    }
}