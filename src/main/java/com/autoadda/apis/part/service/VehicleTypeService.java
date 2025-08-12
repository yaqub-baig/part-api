package com.autoadda.apis.part.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.part.entity.VehicleType;
import com.autoadda.apis.part.exception.AlreadyExistException;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.VehicleTypeRepository;
import com.autoadda.apis.part.request.VehicleTypeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.VehicleTypeResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VehicleTypeService {

	@Autowired
	private PartsDataValidator partsDataValidator;

	@Autowired
	private VehicleTypeRepository vehicleTypeRepository;

	/**
	 * This method prepares a VehicleType entity from the VehicleTypeRequest.
	 * 
	 * @param request The VehicleTypeRequest containing the details for the new
	 *                VehicleType.
	 * @return A VehicleType entity with a generated ID and the name from the
	 *         request.
	 */
	private VehicleType prepareVehicleType(VehicleTypeRequest request) {
		return VehicleType.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).build();
	}

	/**
	 * This method retrieves all vehicle types from the repository and returns them
	 * in a GenericResponse.
	 * 
	 * @return A GenericResponse containing a list of VehicleTypeResponse objects or
	 *         an error message if an exception occurs.
	 */
	public GenericResponse getAllVehicleTypes() {
		List<VehicleTypeResponse> responseBody = new ArrayList<>();
		List<VehicleType> vehicleTypes = vehicleTypeRepository.findAll();
		for (VehicleType vehicleType : vehicleTypes) {
			responseBody.add(VehicleTypeResponse.builder().id(vehicleType.getId()).name(vehicleType.getName()).build());
		}
		return GenericResponse.builder().body(responseBody).code("200").message("success").status("success").build();
	}

	/**
	 * This method creates a new vehicle type or a list of vehicle types based on
	 * the provided VehicleTypeRequest objects.
	 * 
	 * @param vehicleTypeRequestList A list of VehicleTypeRequest objects to be
	 *                               created.
	 * @return A GenericResponse containing the created VehicleType objects or an
	 *         error message if an exception occurs.
	 * @throws AlreadyExistException If a vehicle type with the same name already
	 *                               exists.
	 */
	public GenericResponse createVehicleType(List<VehicleTypeRequest> vehicleTypeRequestList)
			throws AlreadyExistException {

		List<VehicleType> vehicleTypeList = new ArrayList<>();
		for (VehicleTypeRequest vehicleTypeRequest : vehicleTypeRequestList) {

			log.info("perform VehicleType input validation");
			partsDataValidator.isVehicleTypeAlreadyExists(vehicleTypeRequest.getName());

			VehicleType vehicleType = vehicleTypeRepository.save(prepareVehicleType(vehicleTypeRequest));
			log.info("VehicleType created - " + vehicleTypeRequest.toString());
			vehicleTypeList.add(vehicleType);
		}

		return GenericResponse.builder().body(vehicleTypeList).code("201").message("successfully Created  VehicleType")
				.status("success").build();

	}

	/**
	 * This method retrieves a vehicle type by its ID and returns it in a
	 * GenericResponse.
	 * 
	 * @param id The ID of the vehicle type to retrieve.
	 * @return A GenericResponse containing the VehicleTypeResponse or an error
	 *         message if an exception occurs.
	 * @throws NotFoundException If the vehicle type with the specified ID is not
	 *                           found.
	 */
	public GenericResponse getVehicleTypeById(String id) throws NotFoundException {
		List<VehicleTypeResponse> responseBody = new ArrayList<>();

		log.info("perform VehicleType input validation");
		partsDataValidator.validateVehicleTypeId(id);
		VehicleType vehicleType = vehicleTypeRepository.findById(id).get();
		if (vehicleType != null && vehicleType.getId() != null) {
			responseBody.add(VehicleTypeResponse.builder().id(vehicleType.getId()).name(vehicleType.getName()).build());
		}
		return GenericResponse.builder().body(responseBody).code("200")
				.message("successfully Retrived " + id + " VehicleType").status("success").build();
	}

	/**
	 * This method retrieves a vehicle type by its name and returns it in a
	 * GenericResponse.
	 * 
	 * @param name The name of the vehicle type to retrieve.
	 * @return A GenericResponse containing the VehicleType or an error message if
	 *         an exception occurs.
	 * @throws NotFoundException If the vehicle type with the specified name is not
	 *                           found.
	 */
	public GenericResponse getVehicleTypeByName(String name) throws NotFoundException {

		log.info("perform VehicleType input validation");
		partsDataValidator.validateVehicleTypeName(name);
		VehicleType vehicleType = vehicleTypeRepository.findByName(name);

		return GenericResponse.builder().body(vehicleType).code("200")
				.message("successfully Retrived " + name + " VehicleType").status("success").build();
	}

	/**
	 * This method updates an existing vehicle type based on the provided
	 * VehicleTypeRequest.
	 * 
	 * @param request The VehicleTypeRequest containing the updated details for the
	 *                vehicle type.
	 * @return A GenericResponse indicating the success or failure of the update
	 *         operation.
	 * @throws NotFoundException If the vehicle type with the specified ID is not
	 *                           found.
	 */
	public GenericResponse updateVehicleType(VehicleTypeRequest request) throws NotFoundException {
		log.info("perform VehicleType input validation");
		partsDataValidator.validateVehicleTypeId(request.getId());
		VehicleType vehicleType = vehicleTypeRepository.findById(request.getId()).get();
		log.info("Updating service status");
		if (vehicleType != null) {
			vehicleType.setName(request.getName());
			vehicleTypeRepository.save(vehicleType);
		}
		return GenericResponse.builder().body("VehicleType is been Updated").code("200")
				.message("successfully Updated " + request.getId() + " VehicleType").status("success").build();
	}

	/**
	 * This method deletes a vehicle type by its ID.
	 * 
	 * @param id The ID of the vehicle type to delete.
	 * @return A GenericResponse indicating the success or failure of the deletion
	 *         operation.
	 * @throws NotFoundException If the vehicle type with the specified ID is not
	 *                           found.
	 */
	public GenericResponse deleteVehicleTypeById(String id) throws NotFoundException {
		log.info("perform VehicleType input validation");
		partsDataValidator.validateVehicleTypeId(id);
		VehicleType vehicleType = vehicleTypeRepository.findById(id).get();
		log.info("Deleting VehicleType by Id");
		if (vehicleType != null) {
			vehicleTypeRepository.delete(vehicleType);
		}
		return GenericResponse.builder().body("VehicleType is been Deleted").code("200").message("successfully Deleted")
				.status("success").build();
	}
}
