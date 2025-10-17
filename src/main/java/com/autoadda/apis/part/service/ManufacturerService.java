package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Manufacturer;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.repository.ManufacturerRepository;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.request.ManufacturerRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.ManufacturerResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManufacturerService {

    private static final String VALIDATING_MANUFACTURER_ID = "Validating Manufacturer ID";
    private static final String MANUFACTURER_NOT_FOUND_WITH_ID = "Manufacturer not found with ID: ";
    private static final String MANUFACTURER = " Manufacturer";
    private static final String SUCCESS = "success";
    private final ManufacturerRepository manufacturerRepository;
    private final MediaFileRepository mediaFileRepository;
    private final PartsDataValidator partsDataValidator;

    /**
     * Prepares a Manufacturer entity from the given request.
     *
     * @param request the ManufacturerRequest containing manufacturer details
     * @return a Manufacturer entity
     */
    private Manufacturer prepareManufacturer(ManufacturerRequest request) {
        return Manufacturer.builder ( ).id ( GUIDGenerator.generateGUID ( ) ).name ( request.getName ( ) ).build ( );
    }

    /**
     * * Retrieves all manufacturers from the repository.
     *
     * @return a GenericResponse containing a list of ManufacturerResponse objects
     */
    public GenericResponse getAllManufacturers() {
        List<Manufacturer> manufacturers = manufacturerRepository.findAll ( );
        List<ManufacturerResponse> responseBody = manufacturers.stream ( )
                .map ( m -> ManufacturerResponse.builder ( ).id ( m.getId ( ) ).name ( m.getName ( ) ).build ( ) )
                .toList ( );

        return GenericResponse.builder ( ).body ( responseBody ).code ( "200" ).message ( SUCCESS ).status ( SUCCESS ).build ( );
    }

    /**
     * Creates a list of manufacturers based on the provided requests.
     *
     * @param manufacturerRequestList a list of ManufacturerRequest objects
     * @return a GenericResponse containing the created manufacturers
     */
    public GenericResponse createManufacturer(List<ManufacturerRequest> manufacturerRequestList) {
        List<Manufacturer> manufacturerList = new ArrayList<> ( );

        for (ManufacturerRequest request : manufacturerRequestList) {
            log.info ( "Validating Manufacturer input" );
            partsDataValidator.isManufacturerAlreadyExists ( request.getName ( ) );

            Manufacturer entity = prepareManufacturer ( request );
            if ( request.getLogoId ( ) != null ) {
                entity.setLogo ( mediaFileRepository.findById ( request.getLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "logo image not found" ) ) );
            }
            Manufacturer manufacturer = manufacturerRepository.save ( entity );
            log.info ( "Manufacturer created - {}", request );
            manufacturerList.add ( manufacturer );
        }
        List<ManufacturerResponse> manufacturerResponses = manufacturerList.stream ( ).map ( manufacturer ->
                ManufacturerResponse.builder ( )
                        .id ( manufacturer.getId ( ) )
                        .logo ( manufacturer.getLogo ( ) != null ? manufacturer.getLogo ( ) : null )
                        .name ( manufacturer.getName ( ) )
                        .build ( )
        ).toList ( );
        return GenericResponse.builder ( ).body ( manufacturerResponses ).code ( "201" )
                .message ( "Successfully Created Manufacturer(s)" ).status ( SUCCESS ).build ( );
    }

    /**
     * Retrieves a manufacturer by its ID.
     *
     * @param id the ID of the manufacturer
     * @return a GenericResponse containing the ManufacturerResponse
     * @throws NotFoundException if the manufacturer is not found
     */
    public GenericResponse getManufacturerById(String id) throws NotFoundException {
        log.info ( VALIDATING_MANUFACTURER_ID );
        partsDataValidator.validateManufacturerId ( id );

        Manufacturer manufacturer = manufacturerRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( MANUFACTURER_NOT_FOUND_WITH_ID + id ) );

        ManufacturerResponse response = ManufacturerResponse.builder ( ).id ( manufacturer.getId ( ) )
                .logo ( manufacturer.getLogo ( ) != null ? manufacturer.getLogo ( ) : null )
                .name ( manufacturer.getName ( ) ).build ( );

        return GenericResponse.builder ( ).body ( List.of ( response ) ).code ( "200" )
                .message ( "Successfully Retrieved " + id + MANUFACTURER ).status ( SUCCESS ).build ( );
    }

    /**
     * Retrieves a manufacturer by its name.
     *
     * @param name the name of the manufacturer
     * @return a GenericResponse containing the Manufacturer entity
     * @throws NotFoundException if the manufacturer is not found
     */
    public GenericResponse getManufacturerByName(String name) throws NotFoundException {
        log.info ( "Validating Manufacturer name" );
        partsDataValidator.validateManufacturerName ( name );

        Manufacturer manufacturer = manufacturerRepository.findByName ( name );
        if ( manufacturer == null ) {
            throw new NotFoundException ( "Manufacturer not found with name: " + name );
        }

        return GenericResponse.builder ( ).body ( manufacturer ).code ( "200" )
                .message ( "Successfully Retrieved " + name + MANUFACTURER ).status ( SUCCESS ).build ( );
    }

    /**
     * Updates an existing manufacturer based on the provided request.
     *
     * @param request the ManufacturerRequest containing updated details
     * @return a GenericResponse indicating the result of the update operation
     * @throws NotFoundException if the manufacturer is not found
     */
    public GenericResponse updateManufacturer(ManufacturerRequest request) throws NotFoundException {
        log.info ( VALIDATING_MANUFACTURER_ID );
        partsDataValidator.validateManufacturerId ( request.getId ( ) );

        Manufacturer manufacturer = manufacturerRepository.findById ( request.getId ( ) )
                .orElseThrow ( () -> new NotFoundException ( MANUFACTURER_NOT_FOUND_WITH_ID + request.getId ( ) ) );

        if ( request.getName ( ) != null ) {
            manufacturer.setName ( request.getName ( ) );
        }

        if ( request.getLogoId ( ) != null ) {
            manufacturer.setLogo ( mediaFileRepository.findById ( request.getLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "logo image not found" ) ) );
        }
        manufacturerRepository.save ( manufacturer );

        return GenericResponse.builder ( ).body ( "Manufacturer has been Updated" ).code ( "200" )
                .message ( "Successfully Updated " + request.getId ( ) + MANUFACTURER ).status ( SUCCESS ).build ( );
    }

    /**
     * * Deletes a manufacturer by its ID.
     *
     * @param id the ID of the manufacturer to delete
     * @return a GenericResponse indicating the result of the deletion
     * @throws NotFoundException if the manufacturer is not found
     */
    public GenericResponse deleteManufacturerById(String id) throws NotFoundException {
        log.info ( VALIDATING_MANUFACTURER_ID );
        partsDataValidator.validateManufacturerId ( id );

        Manufacturer manufacturer = manufacturerRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( MANUFACTURER_NOT_FOUND_WITH_ID + id ) );

        manufacturerRepository.delete ( manufacturer );

        return GenericResponse.builder ( ).body ( "Manufacturer has been Deleted" ).code ( "200" )
                .message ( "Successfully Deleted" ).status ( SUCCESS ).build ( );
    }
}
