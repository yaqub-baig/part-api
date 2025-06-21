package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.EngineRequest;
import com.platform.parts.response.GenericResponse;

public interface EngineService {

	GenericResponse getAllEngines();

	GenericResponse createEngine(List<EngineRequest> engineRequestList) throws AlreadyExistException;

	GenericResponse getEngineById(String Id) throws NotFoundException;

	GenericResponse getEngineByName(String name) throws NotFoundException;

	GenericResponse getEnginesByModelId(String modelId) throws NotFoundException;

	GenericResponse getEnginesByFuelType(String fuelType) throws NotFoundException;

	GenericResponse getEnginesByTransmissionType(String transmissionType) throws NotFoundException;

	GenericResponse updateEngine(EngineRequest request) throws NotFoundException;

	GenericResponse deleteEngineById(String id) throws NotFoundException;
}
