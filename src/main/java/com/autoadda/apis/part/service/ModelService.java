package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.MediaFile;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.VehicleType;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.repository.VehicleTypeRepository;
import com.autoadda.apis.part.request.ModelRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.ModelResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelService {


    public static final String MODEL = " Model";
    public static final String MODEL_NOT_FOUND_WITH_ID = "Model not found with ID: ";
    public static final String VEHICLE_TYPE_NOT_FOUND = "VehicleType not found";
    public static final String VALIDATING_MODEL_ID = "Validating model ID";
    public static final String MAKE_NOT_FOUND = "Make not found";
    public static final String SUCCESS = "success";
    private final ModelRepository modelRepository;


    private final VehicleTypeRepository vehicleTypeRepository;


    private final MakeRepository makeRepository;


    private final MediaFileRepository mediaFileRepository;


    private final PartsDataValidator partsDataValidator;

    /**
     * Prepares a Model entity from the ModelRequest.
     *
     * @param request the ModelRequest containing model details
     * @return a Model entity
     */
    private Model prepareModel(ModelRequest request) {
        Model model = Model.builder ( )
                .id ( GUIDGenerator.generateGUID ( ) )
                .name ( request.getName ( ) )
                .fullName ( request.getFullName ( ) )
                .vehicleTypeId ( request.getVehicleTypeId ( ) )
                .makeId ( request.getMakeId ( ) )
                .build ( );
        if ( request.getImageIds ( ) != null && ! request.getImageIds ( ).isEmpty ( ) ) {
            List<MediaFile> images = mediaFileRepository.findAllById ( request.getImageIds ( ) );
            model.setImages ( images );
        }
        return model;
    }

    private ModelResponse buildModelResponse(Model model, VehicleType vehicleType, Make make) {
        return ModelResponse.builder ( )
                .id ( model.getId ( ) )
                .name ( model.getName ( ) )
                .fullName ( model.getFullName ( ) )
                .vehicleType ( vehicleType.getName ( ) )
                .vehicleTypeId ( model.getVehicleTypeId ( ) )
                .makerName ( make.getName ( ) )
                .makeId ( model.getMakeId ( ) )
                .images ( model.getImages ( ) )
                .build ( );
    }

    /**
     * Retrieves all models from the repository and prepares a response.
     *
     * @return GenericResponse containing a list of ModelResponse
     * @throws NotFoundException if no models are found
     */
    @Transactional(readOnly = true)
    public GenericResponse getAllModels() throws NotFoundException {
        List<Model> models = modelRepository.findAll ( );
        List<ModelResponse> responseBody = new ArrayList<> ( );

        for (Model model : models) {
            VehicleType vehicleType = vehicleTypeRepository.findById ( model.getVehicleTypeId ( ) )
                    .orElseThrow ( () -> new NotFoundException (VEHICLE_TYPE_NOT_FOUND) );
            Make make = makeRepository.findById ( model.getMakeId ( ) )
                    .orElseThrow ( () -> new NotFoundException (MAKE_NOT_FOUND) );

            responseBody.add ( buildModelResponse ( model, vehicleType, make ) );
        }

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message (SUCCESS)
                .status (SUCCESS)
                .build ( );
    }

    /**
     * Creates new models based on the provided list of ModelRequest.
     *
     * @param modelRequestList
     * @return GenericResponse containing the created models
     */
    @Transactional
    public GenericResponse createModel(List<ModelRequest> modelRequestList) {
        List<ModelResponse> responseBody = new ArrayList<> ( );

        for (ModelRequest modelRequest : modelRequestList) {
            log.info ( "Validating model input" );
            partsDataValidator.isModelAlreadyExists ( modelRequest.getName ( ) );
            partsDataValidator.validateVehicleTypeId ( modelRequest.getVehicleTypeId ( ) );
            partsDataValidator.validateMakeId ( modelRequest.getMakeId ( ) );
            Model model = modelRepository.save ( prepareModel ( modelRequest ) );
            log.info ( "Model created - {}", modelRequest );

            VehicleType vehicleType = vehicleTypeRepository.findById ( model.getVehicleTypeId ( ) )
                    .orElseThrow ( () -> new NotFoundException (VEHICLE_TYPE_NOT_FOUND) );
            Make make = makeRepository.findById ( model.getMakeId ( ) )
                    .orElseThrow ( () -> new NotFoundException (MAKE_NOT_FOUND) );
            responseBody.add ( buildModelResponse ( model, vehicleType, make ) );
        }

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "201" )
                .message ( "Successfully Created Model(s)" )
                .status (SUCCESS)
                .build ( );
    }

    /**
     * Retrieves a model by its ID and prepares a response.
     *
     * @param id the ID of the model
     * @return GenericResponse containing the ModelResponse
     * @throws NotFoundException if the model or related entities are not found
     */
    @Transactional(readOnly = true)
    public GenericResponse getModelById(String id) throws NotFoundException {
        log.info (VALIDATING_MODEL_ID);
        partsDataValidator.validateModelId ( id );

        Model model = modelRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( MODEL_NOT_FOUND_WITH_ID + id ) );
        VehicleType vehicleType = vehicleTypeRepository.findById ( model.getVehicleTypeId ( ) )
                .orElseThrow ( () -> new NotFoundException (VEHICLE_TYPE_NOT_FOUND) );
        Make make = makeRepository.findById ( model.getMakeId ( ) )
                .orElseThrow ( () -> new NotFoundException (MAKE_NOT_FOUND) );

        ModelResponse response = buildModelResponse ( model, vehicleType, make );

        return GenericResponse.builder ( )
                .body ( List.of ( response ) )
                .code ( "200" )
                .message ( "Successfully Retrieved " + id + MODEL)
                .status (SUCCESS)
                .build ( );
    }

    /**
     * Retrieves a model by its name and prepares a response.
     *
     * @param name
     * @return GenericResponse containing the model
     * @throws NotFoundException
     */
    @Transactional(readOnly = true)
    public GenericResponse getModelByName(String name) throws NotFoundException {
        log.info ( "Validating model name" );
        partsDataValidator.validateModelName ( name );

        Model model = modelRepository.findByName ( name );
        if ( model == null ) {
            throw new NotFoundException ( "Model not found with name: " + name );
        }

        VehicleType vehicleType = vehicleTypeRepository.findById ( model.getVehicleTypeId ( ) )
                .orElseThrow ( () -> new NotFoundException (VEHICLE_TYPE_NOT_FOUND) );
        Make make = makeRepository.findById ( model.getMakeId ( ) )
                .orElseThrow ( () -> new NotFoundException (MAKE_NOT_FOUND) );

        return GenericResponse.builder ( )
                .body ( buildModelResponse ( model, vehicleType, make ) )
                .code ( "200" )
                .message ( "Successfully Retrieved " + name + MODEL )
                .status (SUCCESS)
                .build ( );
    }

    /**
     * Updates an existing model based on the provided ModelRequest.
     *
     * @param request
     * @return GenericResponse containing the update status
     * @throws NotFoundException
     */
    @Transactional
    public GenericResponse updateModel(ModelRequest request) throws NotFoundException {
        log.info (VALIDATING_MODEL_ID);
        partsDataValidator.validateModelId ( request.getId ( ) );

        Model model = modelRepository.findById ( request.getId ( ) )
                .orElseThrow ( () -> new NotFoundException ( MODEL_NOT_FOUND_WITH_ID + request.getId ( ) ) );

        if ( request.getName ( ) != null ) {
            model.setName ( request.getName ( ) );
        }
        if ( request.getFullName ( ) != null ) {
            model.setFullName ( request.getFullName ( ) );
        }
        if ( request.getMakeId ( ) != null ) {
            model.setMakeId ( request.getMakeId ( ) );
        }
        if ( request.getVehicleTypeId ( ) != null ) {
            model.setVehicleTypeId ( request.getVehicleTypeId ( ) );
        }
        if ( request.getImageIds ( ) != null && ! request.getImageIds ( ).isEmpty ( ) ) {
            List<MediaFile> images = takeOutMediaFileFromRequestImageIds ( request.getImageIds ( ) );
            model.getImages ( ).clear ( );
            model.getImages ( ).addAll ( images );
        }

        modelRepository.save ( model );

        return GenericResponse.builder ( )
                .body ( "Model has been Updated" )
                .code ( "200" )
                .message ( "Successfully Updated " + request.getId ( ) + MODEL )
                .status (SUCCESS)
                .build ( );
    }

    private List<MediaFile> takeOutMediaFileFromRequestImageIds(List<Long> imageIds) {
        List<MediaFile> mediaFiles = new ArrayList<> ( );
        for (Long imageId : imageIds) {
            mediaFiles.add ( mediaFileRepository.findById ( imageId )
                    .orElseThrow ( () -> new ResourceNotFoundException ( "media File not found" ) ) );
        }
        return mediaFiles;
    }

    /**
     * Deletes a model by its ID and prepares a response.
     *
     * @param id
     * @return GenericResponse containing the deletion status
     * @throws NotFoundException
     */
    @Transactional
    public GenericResponse deleteModelById(String id) throws NotFoundException {
        log.info (VALIDATING_MODEL_ID);
        partsDataValidator.validateModelId ( id );

        Model model = modelRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( MODEL_NOT_FOUND_WITH_ID + id ) );

        modelRepository.delete ( model );

        return GenericResponse.builder ( )
                .body ( "Model has been Deleted" )
                .code ( "200" )
                .message ( "Successfully Deleted" )
                .status (SUCCESS)
                .build ( );
    }

    /**
     * Removes all images attached to a model. orphanRemoval deletes the
     * MediaFile rows; on-disk files are not removed (matches existing
     * /mediafiles/delete-image behavior).
     */
    @Transactional
    public GenericResponse deleteAllImagesByModelId(String id) throws NotFoundException {
        log.info (VALIDATING_MODEL_ID);
        partsDataValidator.validateModelId ( id );

        Model model = modelRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( MODEL_NOT_FOUND_WITH_ID + id ) );

        int removed = model.getImages ( ).size ( );
        model.getImages ( ).clear ( );
        modelRepository.save ( model );

        return GenericResponse.builder ( )
                .body ( removed + " image(s) removed from model " + id )
                .code ( "200" )
                .message ( "Successfully cleared images" )
                .status (SUCCESS)
                .build ( );
    }

    /**
     * Retrieves models by their make ID and prepares a response.
     *
     * @param makeId
     * @return GenericResponse containing the list of models
     * @throws NotFoundException
     */
    @Transactional(readOnly = true)
    public GenericResponse getModelsByMakeId(String makeId) throws NotFoundException {
        List<Model> models = modelRepository.findByMakeId ( makeId );
        List<ModelResponse> responseBody = new ArrayList<> ( );

        for (Model model : models) {
            VehicleType vehicleType = vehicleTypeRepository.findById ( model.getVehicleTypeId ( ) )
                    .orElseThrow ( () -> new NotFoundException (VEHICLE_TYPE_NOT_FOUND) );
            Make make = makeRepository.findById ( model.getMakeId ( ) )
                    .orElseThrow ( () -> new NotFoundException (MAKE_NOT_FOUND) );

            responseBody.add ( buildModelResponse ( model, vehicleType, make ) );
        }

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "Successfully Retrieved" )
                .status (SUCCESS)
                .build ( );
    }

    /**
     * Retrieves models by their vehicle type ID and prepares a response.
     *
     * @param vehicleTypeId
     * @return GenericResponse containing the list of models
     * @throws NotFoundException
     */
    @Transactional(readOnly = true)
    public GenericResponse getModelsByVehicleTypeId(String vehicleTypeId) throws NotFoundException {
        List<Model> models = modelRepository.findByVehicleTypeId ( vehicleTypeId );
        List<ModelResponse> responseBody = new ArrayList<> ( );

        for (Model model : models) {
            VehicleType vehicleType = vehicleTypeRepository.findById ( model.getVehicleTypeId ( ) )
                    .orElseThrow ( () -> new NotFoundException (VEHICLE_TYPE_NOT_FOUND) );
            Make make = makeRepository.findById ( model.getMakeId ( ) )
                    .orElseThrow ( () -> new NotFoundException (MAKE_NOT_FOUND) );

            responseBody.add ( buildModelResponse ( model, vehicleType, make ) );
        }

        return GenericResponse.builder ( )
                .body ( responseBody )
                .code ( "200" )
                .message ( "Successfully Retrieved" )
                .status (SUCCESS)
                .build ( );
    }
}
