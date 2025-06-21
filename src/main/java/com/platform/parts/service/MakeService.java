package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.MakeRequest;
import com.platform.parts.response.GenericResponse;

public interface MakeService {

	GenericResponse getAllMakes();

	GenericResponse createMake(List<MakeRequest> makeRequestList) throws AlreadyExistException;

	GenericResponse getMakeById(String makeId) throws NotFoundException;

	GenericResponse getMakeByName(String name) throws NotFoundException;

	GenericResponse updateMake(MakeRequest request) throws NotFoundException;

	GenericResponse deleteMakeById(String id) throws NotFoundException;
}
