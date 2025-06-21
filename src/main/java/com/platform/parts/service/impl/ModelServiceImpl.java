package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.Make;
import com.platform.parts.entity.Model;
import com.platform.parts.entity.VehicleType;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.MakeRepository;
import com.platform.parts.repository.ModelRepository;
import com.platform.parts.repository.VehicleTypeRepository;
import com.platform.parts.request.ModelRequest;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.response.ModelResponse;
import com.platform.parts.service.ModelService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ModelServiceImpl implements ModelService {

	@Autowired
	private ModelRepository modelRepository;

	@Autowired
	private VehicleTypeRepository vehicleTypeRepository;

	@Autowired
	private MakeRepository makeRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private Model prepareModel(ModelRequest request) {
		return Model.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).fullName(request.getFullName())
				.vehicleTypeId(request.getVehicleTypeId()).makeId(request.getMakeId()).build();
	}

	@Override
	public GenericResponse getAllModels() {
		List<ModelResponse> responseBody = new ArrayList<>();
		try {
			List<Model> models = modelRepository.findAll();
			for (Model model : models) {
				VehicleType vehicleType = vehicleTypeRepository.findById(model.getVehicleTypeId()).get();
				Make make = makeRepository.findById(model.getMakeId()).get();
				responseBody.add(ModelResponse.builder().id(model.getId()).name(model.getName())
						.vehicleType(vehicleType.getName()).vehicleTypeId(model.getVehicleTypeId())
						.makerName(make.getName()).makeId(model.getMakeId()).fullName(model.getFullName()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting models");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse createModel(List<ModelRequest> modelRequestList) throws AlreadyExistException {
		try {

			List<Model> modelList = new ArrayList<>();
			for (ModelRequest modelRequest : modelRequestList) {

				log.info("perform Model input validation");
				partsDataValidator.isModelAlreadyExists(modelRequest.getName());

				Model model = modelRepository.save(prepareModel(modelRequest));
				log.info("Model created - " + modelRequest.toString());
				modelList.add(model);
			}

			return GenericResponse.builder().body(modelList).code("201").message("successfully Created Model(s)")
					.status("success").build();
		} catch (AlreadyExistException exc) {
			return GenericResponse.builder().status("failed").message("Resource Already Existed").code("400").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating parts model");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getModelById(String id) throws NotFoundException {
		List<ModelResponse> responseBody = new ArrayList<>();
		try {

			log.info("perform Model input validation");
			partsDataValidator.validateModelId(id);
			Model model = modelRepository.findById(id).get();
			if (model != null && model.getId() != null) {
				VehicleType vehicleType = vehicleTypeRepository.findById(model.getVehicleTypeId()).get();
				Make make = makeRepository.findById(model.getMakeId()).get();
				responseBody.add(ModelResponse.builder().id(model.getId()).name(model.getName())
						.vehicleType(vehicleType.getName()).makerName(make.getName())
						.vehicleTypeId(model.getVehicleTypeId()).makeId(model.getMakeId()).fullName(model.getFullName())
						.build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + id + " Model").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting model By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getModelByName(String name) throws NotFoundException {
		try {

			log.info("perform Model input validation");
			partsDataValidator.validateModelName(name);
			Model model = modelRepository.findByName(name);

			return GenericResponse.builder().body(model).code("200").message("successfully Retrived " + name + " Model")
					.status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting model By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse updateModel(ModelRequest request) throws NotFoundException {
		try {
			log.info("perform Model input validation");
			partsDataValidator.validateModelId(request.getId());
			Model model = modelRepository.findById(request.getId()).get();
			if (model != null) {
				model.setName(request.getName());
				model.setFullName(request.getFullName());
				model.setMakeId(request.getMakeId());
				model.setVehicleTypeId(request.getVehicleTypeId());

				modelRepository.save(model);
			}
			return GenericResponse.builder().body("Model is been Updated").code("200")
					.message("successfully Updated " + request.getId() + " Model").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating Model");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteModelById(String id) throws NotFoundException {
		try {
			log.info("perform Model input validation");
			partsDataValidator.validateModelId(id);
			Model model = modelRepository.findById(id).get();
			log.info("Deleting Model by Id");
			if (model != null) {
				modelRepository.delete(model);
			}
			return GenericResponse.builder().body("Model is been Deleted").code("200").message("successfully Deleted")
					.status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting Model");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getModelsByMakeId(String makeId) {

		List<ModelResponse> responseBody = new ArrayList<>();
		try {
			List<Model> models = modelRepository.findByMakeId(makeId);

			for (Model model : models) {
				VehicleType vehicleType = vehicleTypeRepository.findById(model.getVehicleTypeId()).get();
				Make make = makeRepository.findById(model.getMakeId()).get();
				responseBody.add(ModelResponse.builder().id(model.getId()).name(model.getName())
						.makerName(make.getName()).fullName(model.getFullName()).vehicleType(vehicleType.getName())
						.makeId(model.getMakeId()).vehicleTypeId(model.getVehicleTypeId()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Part");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getModelsByVehicleTypeId(String vehicleTypeId) {

		List<ModelResponse> responseBody = new ArrayList<>();
		try {
			List<Model> models = modelRepository.findByVehicleTypeId(vehicleTypeId);

			for (Model model : models) {
				VehicleType vehicleType = vehicleTypeRepository.findById(model.getVehicleTypeId()).get();
				Make make = makeRepository.findById(model.getMakeId()).get();
				responseBody.add(ModelResponse.builder().id(model.getId()).name(model.getName())
						.makerName(make.getName()).fullName(model.getFullName()).vehicleType(vehicleType.getName())
						.makeId(model.getMakeId()).vehicleTypeId(model.getVehicleTypeId()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("successfully Retrieved")
					.status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while searching Part");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}
}
