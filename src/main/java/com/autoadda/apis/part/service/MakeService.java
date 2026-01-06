package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.request.MakeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.MakeResponse;
import com.autoadda.apis.part.response.ManufacturerResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MakeService {


    public static final String MAKE_NOT_FOUND_WITH_ID = "Make not found with ID: ";
    public static final String SUCCESS = "success";
    public static final String MAKE = " Make";
    public static final String VALIDATING_MAKE_ID = "Validating Make ID";
    private final MakeRepository makeRepository;
    private final PartsDataValidator partsDataValidator;
    private final MediaFileRepository mediaFileRepository;

    private Make prepareMake(MakeRequest request) {
        return Make.builder ( ).id ( GUIDGenerator.generateGUID ( ) ).name ( request.getName ( ) ).build ( );
    }

    @Cacheable(value = "makes", key = "'allMakes'")
    public GenericResponse getAllMakes() {
        List<Make> makes = makeRepository.findAll ( );
        List<MakeResponse> responseBody = makes.stream ( )
                .map ( make -> MakeResponse.builder ( ).id ( make.getId ( ) ).name ( make.getName ( ) ).logo(make.getLogo()!=null?make.getLogo ():null).build ( ) )
                .sorted(Comparator.comparing(MakeResponse::getName))
                .toList ();
        return GenericResponse.builder ( ).body ( responseBody ).code ( "200" ).message (SUCCESS).status ( SUCCESS ).build ( );
    }
    @CacheEvict(value = "makes", key = "'allMakes'")
    public GenericResponse createMake(List<MakeRequest> makeRequestList) {
        List<Make> makeList = new ArrayList<> ( );

        for (MakeRequest request : makeRequestList) {
            log.info ( "Performing Make input validation" );
            partsDataValidator.isMakeAlreadyExists ( request.getName ( ) );

            Make entity = prepareMake(request);
            if(request.getLogoId()!=null){
                entity.setLogo(mediaFileRepository.findById(request.getLogoId()).orElseThrow(()->new ResourceNotFoundException("not found")));
            }
            Make make = makeRepository.save (entity);
            log.info ( "Make created - {}", request );
            makeList.add ( make );
        }  List<MakeResponse> makeResponseList = makeList.stream ( ).map (manufacturer ->
                MakeResponse.builder ( )
                        .id ( manufacturer.getId ( ) )
                        .logo ( manufacturer.getLogo ( ) != null ? manufacturer.getLogo ( ) : null )
                        .name ( manufacturer.getName ( ) )
                        .build ( )
        ).toList ( );

        return GenericResponse.builder ( ).body ( makeResponseList ).code ( "201" ).message ( "Successfully Created Make" )
                .status ( SUCCESS ).build ( );
    }

    public GenericResponse getMakeById(String id) throws NotFoundException {
        log.info ( VALIDATING_MAKE_ID );
        partsDataValidator.validateMakeId ( id );

        Make make = makeRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( MAKE_NOT_FOUND_WITH_ID + id ) );

        MakeResponse response = MakeResponse.builder ( ).id ( make.getId ( ) ).logo ( make.getLogo ( ) != null ? make.getLogo ( ) : null ).name ( make.getName ( ) ).build ( );

        return GenericResponse.builder ( ).body ( List.of ( response ) ).code ( "200" )
                .message ( "Successfully Retrieved " + id + MAKE).status ( SUCCESS ).build ( );
    }

    public GenericResponse getMakeByName(String name) throws NotFoundException {
        log.info ( "Validating Make name" );
        partsDataValidator.validateMakeName ( name );

        Make make = makeRepository.findByName ( name );
        if ( make == null ) {
            throw new NotFoundException ( "Make not found with name: " + name );
        }

        return GenericResponse.builder ( ).body ( make ).code ( "200" ).message ( "Successfully Retrieved " + name + MAKE )
                .status ( SUCCESS ).build ( );
    }
    @CacheEvict(value = "makes", key = "'allMakes'")
    public GenericResponse updateMake(MakeRequest request) throws NotFoundException {
        log.info ( VALIDATING_MAKE_ID );
        partsDataValidator.validateMakeId ( request.getId ( ) );

        Make make = makeRepository.findById ( request.getId ( ) )
                .orElseThrow ( () -> new NotFoundException ( MAKE_NOT_FOUND_WITH_ID + request.getId ( ) ) );

        if ( request.getLogoId ( ) != null ) {
            make.setLogo ( mediaFileRepository.findById ( request.getLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "logo image not found" ) ) );
        }

        if ( request.getName ( ) != null ) {
            make.setName ( request.getName ( ) );
        }
        makeRepository.save ( make );

        return GenericResponse.builder ( ).body ( "Make has been Updated" ).code ( "200" )
                .message ( "Successfully Updated " + request.getId ( ) + MAKE ).status ( SUCCESS ).build ( );
    }
    @CacheEvict(value = "makes", key = "'allMakes'")
    public GenericResponse deleteMakeById(String id) throws NotFoundException {
        log.info (VALIDATING_MAKE_ID);
        partsDataValidator.validateMakeId ( id );

        Make make = makeRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( MAKE_NOT_FOUND_WITH_ID + id ) );
        makeRepository.delete ( make );

        return GenericResponse.builder ( ).body ( "Make has been Deleted" ).code ( "200" ).message ( "Successfully Deleted" )
                .status ( SUCCESS ).build ( );
    }
}
