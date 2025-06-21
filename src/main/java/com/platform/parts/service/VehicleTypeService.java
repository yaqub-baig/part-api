package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.VehicleTypeRequest;
import com.platform.parts.response.GenericResponse;

public interface VehicleTypeService {

	GenericResponse getAllVehicleTypes();

	GenericResponse createVehicleType(List<VehicleTypeRequest> vehicleTypeRequestList) throws AlreadyExistException;

	GenericResponse getVehicleTypeById(String categoryId) throws NotFoundException;

	GenericResponse getVehicleTypeByName(String name) throws NotFoundException;

	GenericResponse updateVehicleType(VehicleTypeRequest request) throws NotFoundException;

	GenericResponse deleteVehicleTypeById(String id) throws NotFoundException;

}
