package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.ManufacturerRequest;
import com.platform.parts.response.GenericResponse;

public interface ManufacturerService {

	GenericResponse getAllManufacturers();

	GenericResponse createManufacturer(List<ManufacturerRequest> manufacturerRequestList) throws AlreadyExistException;

	GenericResponse getManufacturerById(String manufacturerId) throws NotFoundException;

	GenericResponse getManufacturerByName(String name) throws NotFoundException;

	GenericResponse updateManufacturer(ManufacturerRequest request) throws NotFoundException;

	GenericResponse deleteManufacturerById(String id) throws NotFoundException;
}
