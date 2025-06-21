package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.VehicleType;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.VehicleTypeRepository;
import com.platform.parts.request.VehicleTypeRequest;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.response.VehicleTypeResponse;
import com.platform.parts.service.VehicleTypeService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VehicleTypeServiceImpl implements VehicleTypeService {

	@Autowired
	private PartsDataValidator partsDataValidator;

	@Autowired
	private VehicleTypeRepository vehicleTypeRepository;

	private VehicleType prepareVehicleType(VehicleTypeRequest request) {
		return VehicleType.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).build();
	}

	@Override
	public GenericResponse getAllVehicleTypes() {
		List<VehicleTypeResponse> responseBody = new ArrayList<>();
		try {
			List<VehicleType> vehicleTypes = vehicleTypeRepository.findAll();
			for (VehicleType vehicleType : vehicleTypes) {
				responseBody
						.add(VehicleTypeResponse.builder().id(vehicleType.getId()).name(vehicleType.getName()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting vehicleTypes");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse createVehicleType(List<VehicleTypeRequest> vehicleTypeRequestList)
			throws AlreadyExistException {
		try {

			List<VehicleType> vehicleTypeList = new ArrayList<>();
			for (VehicleTypeRequest vehicleTypeRequest : vehicleTypeRequestList) {

				log.info("perform VehicleType input validation");
				partsDataValidator.isVehicleTypeAlreadyExists(vehicleTypeRequest.getName());

				VehicleType vehicleType = vehicleTypeRepository.save(prepareVehicleType(vehicleTypeRequest));
				log.info("VehicleType created - " + vehicleTypeRequest.toString());
				vehicleTypeList.add(vehicleType);
			}

			return GenericResponse.builder().body(vehicleTypeList).code("201")
					.message("successfully Created  VehicleType").status("success").build();
		} catch (AlreadyExistException exc) {
			return GenericResponse.builder().status("failed").message("Resource Already Existed").code("400").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating parts vehicleType");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getVehicleTypeById(String id) throws NotFoundException {
		List<VehicleTypeResponse> responseBody = new ArrayList<>();
		try {

			log.info("perform VehicleType input validation");
			partsDataValidator.validateVehicleTypeId(id);
			VehicleType vehicleType = vehicleTypeRepository.findById(id).get();
			if (vehicleType != null && vehicleType.getId() != null) {
				responseBody
						.add(VehicleTypeResponse.builder().id(vehicleType.getId()).name(vehicleType.getName()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + id + " VehicleType").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting vehicleType By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getVehicleTypeByName(String name) throws NotFoundException {
		try {

			log.info("perform VehicleType input validation");
			partsDataValidator.validateVehicleTypeName(name);
			VehicleType vehicleType = vehicleTypeRepository.findByName(name);

			return GenericResponse.builder().body(vehicleType).code("200")
					.message("successfully Retrived " + name + " VehicleType").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting vehicleType By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse updateVehicleType(VehicleTypeRequest request) throws NotFoundException {
		try {
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
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating VehicleType");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteVehicleTypeById(String id) throws NotFoundException {
		try {
			log.info("perform VehicleType input validation");
			partsDataValidator.validateVehicleTypeId(id);
			VehicleType vehicleType = vehicleTypeRepository.findById(id).get();
			log.info("Deleting VehicleType by Id");
			if (vehicleType != null) {
				vehicleTypeRepository.delete(vehicleType);
			}
			return GenericResponse.builder().body("VehicleType is been Deleted").code("200")
					.message("successfully Deleted").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting VehicleType");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}
}
