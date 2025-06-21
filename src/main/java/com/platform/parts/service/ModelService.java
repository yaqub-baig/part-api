package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.ModelRequest;
import com.platform.parts.response.GenericResponse;

public interface ModelService {

	GenericResponse getAllModels();

	GenericResponse createModel(List<ModelRequest> modelRequestList) throws AlreadyExistException;

	GenericResponse getModelById(String Id) throws NotFoundException;

	GenericResponse getModelByName(String name) throws NotFoundException;

	GenericResponse getModelsByMakeId(String makeId) throws NotFoundException;

	GenericResponse getModelsByVehicleTypeId(String vehicleTypeId) throws NotFoundException;

	GenericResponse updateModel(ModelRequest request) throws NotFoundException;

	GenericResponse deleteModelById(String id) throws NotFoundException;
}
