package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.AttributeRequest;
import com.platform.parts.response.GenericResponse;

public interface AttributeService {

	GenericResponse getAllAttributes();

	GenericResponse createAttribute(List<AttributeRequest> attributeRequestList) throws AlreadyExistException;

	GenericResponse getAttributeById(String attributeId) throws NotFoundException;

	GenericResponse getAttributeByName(String name) throws NotFoundException;

	GenericResponse updateAttribute(AttributeRequest request) throws NotFoundException;

	GenericResponse deleteAttributeById(String id) throws NotFoundException;
}
