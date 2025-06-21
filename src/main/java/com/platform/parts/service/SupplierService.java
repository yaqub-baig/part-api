package com.platform.parts.service;

import java.util.List;

import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.request.SupplierRequest;
import com.platform.parts.response.GenericResponse;

public interface SupplierService {
	
	GenericResponse getAllSuppliers();

	GenericResponse createSupplier(List<SupplierRequest> supplierRequestList) throws AlreadyExistException;

	GenericResponse getSupplierById(String categoryId) throws NotFoundException;

	GenericResponse getSupplierByName(String name) throws NotFoundException;

	GenericResponse updateSupplier(SupplierRequest request) throws NotFoundException;

	GenericResponse deleteSupplierById(String id) throws NotFoundException;

}
