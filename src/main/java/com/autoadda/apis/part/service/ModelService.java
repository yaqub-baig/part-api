package com.autoadda.apis.part.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.VehicleType;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.repository.VehicleTypeRepository;
import com.autoadda.apis.part.request.ModelRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.ModelResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ModelService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Autowired
    private MakeRepository makeRepository;

    @Autowired
    private PartsDataValidator partsDataValidator;

    /**
	 * Prepares a Model entity from the ModelRequest.
	 *
	 * @param request the ModelRequest containing model details
	 * @return a Model entity
	 */
    private Model prepareModel(ModelRequest request) {
        return Model.builder()
                .id(GUIDGenerator.generateGUID())
                .name(request.getName())
                .fullName(request.getFullName())
                .vehicleTypeId(request.getVehicleTypeId())
                .makeId(request.getMakeId())
                .build();
    }

    /**
	 * Retrieves all models from the repository and prepares a response.
	 *
	 * @return GenericResponse containing a list of ModelResponse
	 * @throws NotFoundException if no models are found
	 */
    public GenericResponse getAllModels() throws NotFoundException {
        List<Model> models = modelRepository.findAll();
        List<ModelResponse> responseBody = new ArrayList<>();

        for (Model model : models) {
            VehicleType vehicleType = vehicleTypeRepository.findById(model.getVehicleTypeId())
                    .orElseThrow(() -> new NotFoundException("VehicleType not found"));
            Make make = makeRepository.findById(model.getMakeId())
                    .orElseThrow(() -> new NotFoundException("Make not found"));

            responseBody.add(ModelResponse.builder()
                    .id(model.getId())
                    .name(model.getName())
                    .vehicleType(vehicleType.getName())
                    .vehicleTypeId(model.getVehicleTypeId())
                    .makerName(make.getName())
                    .makeId(model.getMakeId())
                    .fullName(model.getFullName())
                    .build());
        }

        return GenericResponse.builder()
                .body(responseBody)
                .code("200")
                .message("success")
                .status("success")
                .build();
    }

    /**	 * Creates new models based on the provided list of ModelRequest.
     * 
     * @param modelRequestList
     * @return GenericResponse containing the created models
     */
    public GenericResponse createModel(List<ModelRequest> modelRequestList) {
        List<Model> modelList = new ArrayList<>();

        for (ModelRequest modelRequest : modelRequestList) {
            log.info("Validating model input");
            partsDataValidator.isModelAlreadyExists(modelRequest.getName());

            Model model = modelRepository.save(prepareModel(modelRequest));
            log.info("Model created - {}", modelRequest);
            modelList.add(model);
        }

        return GenericResponse.builder()
                .body(modelList)
                .code("201")
                .message("Successfully Created Model(s)")
                .status("success")
                .build();
    }

    /**	 * Retrieves a model by its ID and prepares a response.
	 *
	 * @param id the ID of the model
	 * @return GenericResponse containing the ModelResponse
	 * @throws NotFoundException if the model or related entities are not found
	 */
    public GenericResponse getModelById(String id) throws NotFoundException {
        log.info("Validating model ID");
        partsDataValidator.validateModelId(id);

        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Model not found with ID: " + id));
        VehicleType vehicleType = vehicleTypeRepository.findById(model.getVehicleTypeId())
                .orElseThrow(() -> new NotFoundException("VehicleType not found"));
        Make make = makeRepository.findById(model.getMakeId())
                .orElseThrow(() -> new NotFoundException("Make not found"));

        ModelResponse response = ModelResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .vehicleType(vehicleType.getName())
                .vehicleTypeId(model.getVehicleTypeId())
                .makerName(make.getName())
                .makeId(model.getMakeId())
                .fullName(model.getFullName())
                .build();

        return GenericResponse.builder()
                .body(List.of(response))
                .code("200")
                .message("Successfully Retrieved " + id + " Model")
                .status("success")
                .build();
    }

    /**	 * Retrieves a model by its name and prepares a response.
     * 
     * @param name
     * @return GenericResponse containing the model
     * @throws NotFoundException 
     */
    public GenericResponse getModelByName(String name) throws NotFoundException {
        log.info("Validating model name");
        partsDataValidator.validateModelName(name);

        Model model = modelRepository.findByName(name);
        if (model == null) {
            throw new NotFoundException("Model not found with name: " + name);
        }

        return GenericResponse.builder()
                .body(model)
                .code("200")
                .message("Successfully Retrieved " + name + " Model")
                .status("success")
                .build();
    }

    /**	 * Updates an existing model based on the provided ModelRequest.
     * 
     * @param request
     * @return GenericResponse containing the update status
     * @throws NotFoundException 
     */
    public GenericResponse updateModel(ModelRequest request) throws NotFoundException {
        log.info("Validating model ID");
        partsDataValidator.validateModelId(request.getId());

        Model model = modelRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Model not found with ID: " + request.getId()));

        model.setName(request.getName());
        model.setFullName(request.getFullName());
        model.setMakeId(request.getMakeId());
        model.setVehicleTypeId(request.getVehicleTypeId());

        modelRepository.save(model);

        return GenericResponse.builder()
                .body("Model has been Updated")
                .code("200")
                .message("Successfully Updated " + request.getId() + " Model")
                .status("success")
                .build();
    }

    /**	 * Deletes a model by its ID and prepares a response.
	 * 
	 * @param id
	 * @return GenericResponse containing the deletion status
     * @throws NotFoundException 
	 */
    public GenericResponse deleteModelById(String id) throws NotFoundException {
        log.info("Validating model ID");
        partsDataValidator.validateModelId(id);

        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Model not found with ID: " + id));

        modelRepository.delete(model);

        return GenericResponse.builder()
                .body("Model has been Deleted")
                .code("200")
                .message("Successfully Deleted")
                .status("success")
                .build();
    }

    /**	 * Retrieves models by their make ID and prepares a response.
     * 
     * @param makeId
     * @return GenericResponse containing the list of models
     * @throws NotFoundException 
     */
    public GenericResponse getModelsByMakeId(String makeId) throws NotFoundException {
        List<Model> models = modelRepository.findByMakeId(makeId);
        List<ModelResponse> responseBody = new ArrayList<>();

        for (Model model : models) {
            VehicleType vehicleType = vehicleTypeRepository.findById(model.getVehicleTypeId())
                    .orElseThrow(() -> new NotFoundException("VehicleType not found"));
            Make make = makeRepository.findById(model.getMakeId())
                    .orElseThrow(() -> new NotFoundException("Make not found"));

            responseBody.add(ModelResponse.builder()
                    .id(model.getId())
                    .name(model.getName())
                    .fullName(model.getFullName())
                    .vehicleType(vehicleType.getName())
                    .vehicleTypeId(model.getVehicleTypeId())
                    .makerName(make.getName())
                    .makeId(model.getMakeId())
                    .build());
        }

        return GenericResponse.builder()
                .body(responseBody)
                .code("200")
                .message("Successfully Retrieved")
                .status("success")
                .build();
    }

    /**	 * Retrieves models by their vehicle type ID and prepares a response.
	 * 
	 * @param vehicleTypeId
	 * @return GenericResponse containing the list of models
     * @throws NotFoundException 
	 */
    public GenericResponse getModelsByVehicleTypeId(String vehicleTypeId) throws NotFoundException {
        List<Model> models = modelRepository.findByVehicleTypeId(vehicleTypeId);
        List<ModelResponse> responseBody = new ArrayList<>();

        for (Model model : models) {
            VehicleType vehicleType = vehicleTypeRepository.findById(model.getVehicleTypeId())
                    .orElseThrow(() -> new NotFoundException("VehicleType not found"));
            Make make = makeRepository.findById(model.getMakeId())
                    .orElseThrow(() -> new NotFoundException("Make not found"));

            responseBody.add(ModelResponse.builder()
                    .id(model.getId())
                    .name(model.getName())
                    .fullName(model.getFullName())
                    .vehicleType(vehicleType.getName())
                    .vehicleTypeId(model.getVehicleTypeId())
                    .makerName(make.getName())
                    .makeId(model.getMakeId())
                    .build());
        }

        return GenericResponse.builder()
                .body(responseBody)
                .code("200")
                .message("Successfully Retrieved")
                .status("success")
                .build();
    }
}

