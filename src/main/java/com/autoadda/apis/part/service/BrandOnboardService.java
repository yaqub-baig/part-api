package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.BrandOnboard;
import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.mapper.BrandOnboardMapper;
import com.autoadda.apis.part.repository.BrandOnboardRepository;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.request.BrandOnboardRequest;
import com.autoadda.apis.part.response.GenericResponse;
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
    private final BrandOnboardRepository brandOnboardRepository;
    private final BrandOnboardMapper brandOnboardMapper;
    private final MakeRepository makeRepository;

    public GenericResponse brandOnboard(BrandOnboardRequest request, String autoAddaId) {
        Make byName = makeRepository.findByName ( request.getBrandName ( ) );
        if ( byName != null ) {
         throw new ResourceNotFoundException ( "brand already exists" );
        }

        BrandOnboard entity = brandOnboardMapper.toEntity ( request );
        entity.setRequestBy ( Integer.parseInt ( autoAddaId ));
        entity.setRequestReferenceId (  generateRequestRefrenceId ());

        BrandOnboard save = brandOnboardRepository.save ( entity );
        return new GenericResponse ( "success", "200", "Your request is successfully taken our team will look into it and add brand if needed",brandOnboardMapper.toResponse ( save ));
    }

    public GenericResponse updateBrand(Integer id, BrandOnboardRequest request) {
        BrandOnboard tempVehicle = brandOnboardRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "update Brand request not found" ) );

        BrandOnboard save = brandOnboardRepository.save ( tempVehicle );
        return new GenericResponse ( "success", "200", "Brand updated Successfully Successfully",brandOnboardMapper.toResponse ( save ));
    }

    private String generateRequestRefrenceId() {
        // Get current year and month
        LocalDateTime now = LocalDateTime.now ( );
        String yearMonth = now.format ( DateTimeFormatter.ofPattern ( "yyMM" ) );

        // Format: RQ-YYMM-XXXX
        String codePrefix = "BR-" + yearMonth + "-";

        // Find the latest code for this prefix
        List<String> latestCodes = brandOnboardRepository.findLatestCodesByPrefix (
                codePrefix );

        int nextSequence = 1;
        if ( ! latestCodes.isEmpty ( ) ) {
            String latestCode = latestCodes.get ( 0 );
            try {
                // Extract the sequence number from the latest code
                String sequencePart = latestCode.substring ( codePrefix.length ( ) );
                nextSequence = Integer.parseInt ( sequencePart ) + 1;
            }
            catch ( NumberFormatException | IndexOutOfBoundsException e ) {
                log.warn ( "Failed to parse sequence number, starting from 1", e );
            }
        }

        // Format the final code with the sequence padded to 4 digits
        return String.format ( "%s%06d", codePrefix, nextSequence );
    }
}
