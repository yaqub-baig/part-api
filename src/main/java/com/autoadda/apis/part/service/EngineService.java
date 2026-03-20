package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Engine;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.EngineRepository;
import com.autoadda.apis.part.request.EngineRequest;
import com.autoadda.apis.part.response.EngineResponse;
import com.autoadda.apis.part.response.GenericResponse;
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
public class EngineService {


    public static final String SUCCESSFULLY_RETRIEVED = "Successfully retrieved";
    public static final String VALIDATING_ENGINE_ID = "Validating engine ID";
    public static final String SUCCESS = "success";
    public static final String ENGINE_NOT_FOUND_WITH_ID = "Engine not found with id: ";
    private final EngineRepository engineRepository;


    private final PartsDataValidator partsDataValidator;

    /**
     * Prepares an Engine entity from the provided EngineRequest.
     *
     * @param request The EngineRequest containing engine details.
     * @return A new Engine entity.
     */
    private Engine prepareEngine(EngineRequest request) {
        return Engine.builder ( ).id ( GUIDGenerator.generateGUID ( ) ).fullName ( request.getFullName ( ) )
                .transmissionType ( request.getTransmissionType ( ) ).engineCapacity( request.getEngineCapacity( ) )
                .fuelType ( request.getFuelType ( ) ).modelId ( request.getModelId ( ) ).build ( );
    }

    /**
     * Retrieves all engines from the repository and maps them to a response object.
     *
     * @return GenericResponse containing a list of EngineResponse objects.
     */
    public GenericResponse getAllEngines() {
        List<Engine> engines = engineRepository.findAll ( );
        List<EngineResponse> responseBody = engines.stream ( )
                .map ( engine -> EngineResponse.builder ( ).id ( engine.getId ( ) ).fullName ( engine.getFullName ( ) )
                        .transmissionType ( engine.getTransmissionType ( ) ).modelId ( engine.getModelId ( ) )
                        .engineCapacity( engine.getEngineCapacity( ) ).fuelType ( engine.getFuelType ( ) ).build ( ) )
                .toList ( );

        return GenericResponse.builder ( ).body ( responseBody ).code ( "200" ).message (SUCCESS).status ( SUCCESS ).build ( );
    }

    /**
     * * Creates a new engine or a list of engines based on the provided
     * EngineRequest
     *
     * @param engineRequestList List of EngineRequest objects.
     * @return GenericResponse containing a list of EngineResponse objects.
     */
    public GenericResponse createEngine(List<EngineRequest> engineRequestList) {
        List<Engine> engineList = new ArrayList<> ( );

        for (EngineRequest request : engineRequestList) {
            log.info ( "Performing engine validation" );
            partsDataValidator.isEngineAlreadyExists ( request.getFullName ( ), request.getTransmissionType ( ), request.getFuelType ( ) );

            Engine engine = engineRepository.save ( prepareEngine ( request ) );
            log.info ( "Engine created - {}", request );
            engineList.add ( engine );
        }

        return GenericResponse.builder ( ).body ( engineList ).code ( "201" ).message ( "Successfully created engine(s)" )
                .status ( SUCCESS ).build ( );
    }

    /**
     * * Retrieves an engine by its ID and maps it to a response object.
     *
     * @param id The ID of the engine to retrieve.
     * @return GenericResponse containing the EngineResponse object.
     * @throws NotFoundException if the engine with the given ID does not exist.
     */
    public GenericResponse getEngineById(String id) throws NotFoundException {
        log.info (VALIDATING_ENGINE_ID);
        partsDataValidator.validateEngineId ( id );

        Engine engine = engineRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( ENGINE_NOT_FOUND_WITH_ID + id ) );

        EngineResponse response = EngineResponse.builder ( ).id ( engine.getId ( ) ).fullName ( engine.getFullName ( ) )
                .transmissionType ( engine.getTransmissionType ( ) ).modelId ( engine.getModelId ( ) ).engineCapacity( engine.getEngineCapacity( ) )
                .fuelType ( engine.getFuelType ( ) ).build ( );

        return GenericResponse.builder ( ).body ( List.of ( response ) ).code ( "200" )
                .message ( "Successfully retrieved engine " + id ).status ( SUCCESS ).build ( );
    }

    /**
     * * Retrieves an engine by its full name and maps it to a response object.
     *
     * @param name engine name
     * @return GenericResponse containing the EngineResponse object.
     * @throws NotFoundException if the engine with the given name does not exist.
     */
    public GenericResponse getEngineByName(String name) throws NotFoundException {
        log.info ( "Validating engine name" );
        partsDataValidator.validateEngineFullName ( name );

        Engine engine = engineRepository.findByFullName ( name );
        if ( engine == null ) {
            throw new NotFoundException ( "Engine not found with name: " + name );
        }

        return GenericResponse.builder ( ).body ( engine ).code ( "200" ).message ( "Successfully retrieved engine: " + name )
                .status ( SUCCESS ).build ( );
    }

    /**
     * * Updates an existing engine based on the provided EngineRequest.
     *
     * @param request The EngineRequest containing engine details.
     * @return GenericResponse containing the updated EngineResponse object.
     * @throws NotFoundException if the engine with the specified ID does not exist.
     */
    public GenericResponse updateEngine(EngineRequest request) throws NotFoundException {
        log.info ( VALIDATING_ENGINE_ID );
        partsDataValidator.validateEngineId ( request.getId ( ) );

        Engine engine = engineRepository.findById ( request.getId ( ) )
                .orElseThrow ( () -> new NotFoundException ( ENGINE_NOT_FOUND_WITH_ID + request.getId ( ) ) );

        engine.setFullName ( request.getFullName ( ) );
        engine.setModelId ( request.getModelId ( ) );
        engine.setEngineCapacity( request.getEngineCapacity( ) );
        engine.setFuelType ( request.getFuelType ( ) );
        engine.setTransmissionType ( request.getTransmissionType ( ) );
        engineRepository.save ( engine );

        return GenericResponse.builder ( ).body ( "Engine has been updated" ).code ( "200" )
                .message ( "Successfully updated engine: " + request.getId ( ) ).status ( SUCCESS ).build ( );
    }

    /**
     * * Deletes an engine by its ID.
     *
     * @param id The ID of the engine to delete.
     * @return GenericResponse indicating the result of the deletion.
     * @throws NotFoundException if the engine with the specified ID does not exist.
     */
    public GenericResponse deleteEngineById(String id) throws NotFoundException {
        log.info ( VALIDATING_ENGINE_ID );
        partsDataValidator.validateEngineId ( id );

        Engine engine = engineRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( ENGINE_NOT_FOUND_WITH_ID + id ) );

        engineRepository.delete ( engine );

        return GenericResponse.builder ( ).body ( "Engine has been deleted" ).code ( "200" ).message ( "Successfully deleted" )
                .status ( SUCCESS ).build ( );
    }

    /**
     * * Retrieves engines by model ID and maps them to a response object.
     *
     * @param modelId model ID
     * @return GenericResponse containing a list of EngineResponse objects.
     */
    public GenericResponse getEnginesByModelId(String modelId) {
        List<Engine> engines = engineRepository.findByModelId ( modelId );
        List<EngineResponse> responseBody = engines.stream ( )
                .map ( engine -> EngineResponse.builder ( ).id ( engine.getId ( ) ).fullName ( engine.getFullName ( ) )
                        .transmissionType ( engine.getTransmissionType ( ) ).modelId ( engine.getModelId ( ) )
                        .engineCapacity( engine.getEngineCapacity( ) ).fuelType ( engine.getFuelType ( ) ).build ( ) )
                .toList ( );

        return GenericResponse.builder ( ).body ( responseBody ).code ( "200" ).message (SUCCESSFULLY_RETRIEVED)
                .status ( SUCCESS ).build ( );
    }

    /**
     * * Retrieves engines by fuel type and maps them to a response object. * This
     * method filters engines based on the specified fuel type.
     *
     * @param fuelType fuel type of vehicle
     * @return GenericResponse containing a list of EngineResponse objects.
     */
    public GenericResponse getEnginesByFuelType(String fuelType) {
        List<Engine> engines = engineRepository.findByFuelType ( fuelType );
        List<EngineResponse> responseBody = engines.stream ( )
                .map ( engine -> EngineResponse.builder ( ).id ( engine.getId ( ) ).fullName ( engine.getFullName ( ) )
                        .transmissionType ( engine.getTransmissionType ( ) ).modelId ( engine.getModelId ( ) )
                        .engineCapacity( engine.getEngineCapacity( ) ).fuelType ( engine.getFuelType ( ) ).build ( ) )
                .toList ( );

        return GenericResponse.builder ( ).body ( responseBody ).code ( "200" ).message ( SUCCESSFULLY_RETRIEVED )
                .status ( SUCCESS ).build ( );
    }

    /**
     * * Retrieves engines by transmission type and maps them to a response object.
     * * This method filters engines based on the specified transmission type.
     *
     * @param transmissionType transmissionType of vehicle
     * @return GenericResponse containing a list of EngineResponse objects.
     */
    public GenericResponse getEnginesByTransmissionType(String transmissionType) {
        List<Engine> engines = engineRepository.findByTransmissionType ( transmissionType );
        List<EngineResponse> responseBody = engines.stream ( )
                .map ( engine -> EngineResponse.builder ( ).id ( engine.getId ( ) ).fullName ( engine.getFullName ( ) )
                        .transmissionType ( engine.getTransmissionType ( ) ).modelId ( engine.getModelId ( ) )
                        .engineCapacity( engine.getEngineCapacity( ) ).fuelType ( engine.getFuelType ( ) ).build ( ) )
                .toList ( ) ;

        return GenericResponse.builder ( ).body ( responseBody ).code ( "200" ).message ( SUCCESSFULLY_RETRIEVED )
                .status ( SUCCESS ).build ( );
    }
}
