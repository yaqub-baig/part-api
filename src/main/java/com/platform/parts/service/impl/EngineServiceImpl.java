package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.Engine;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.EngineRepository;
import com.platform.parts.request.EngineRequest;
import com.platform.parts.response.EngineResponse;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.service.EngineService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EngineServiceImpl implements EngineService {

	@Autowired
	private EngineRepository engineRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private Engine prepareEngine(EngineRequest request) {
		return Engine.builder().id(GUIDGenerator.generateGUID()).fullName(request.getFullName())
				.transmissionType(request.getTransmissionType()).engine(request.getEngine())
				.fuelType(request.getFuelType()).modelId(request.getModelId()).build();
	}

	@Override
	public GenericResponse getAllEngines() {
		List<EngineResponse> responseBody = new ArrayList<>();
		try {
			List<Engine> categories = engineRepository.findAll();
			for (Engine engine : categories) {
				responseBody.add(EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
						.transmissionType(engine.getTransmissionType()).modelId(engine.getModelId())
						.engine(engine.getEngine()).fuelType(engine.getFuelType()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting engines");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse createEngine(List<EngineRequest> engineRequestList) throws AlreadyExistException {
		try {

			List<Engine> engineList = new ArrayList<>();
			for (EngineRequest engineRequest : engineRequestList) {

				log.info("perform Engine input validation");
				partsDataValidator.isEngineAlreadyExists(engineRequest.getFullName());

				Engine engine = engineRepository.save(prepareEngine(engineRequest));
				log.info("Engine created - " + engineRequest.toString());
				engineList.add(engine);
			}

			return GenericResponse.builder().body(engineList).code("201").message("successfully Created Engine(s)")
					.status("success").build();
		} catch (AlreadyExistException exc) {
			return GenericResponse.builder().status("failed").message("Resource Already Existed").code("400").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating parts engine");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getEngineById(String id) throws NotFoundException {
		List<EngineResponse> responseBody = new ArrayList<>();
		try {

			log.info("perform Engine input validation");
			partsDataValidator.validateEngineId(id);
			Engine engine = engineRepository.findById(id).get();
			if (engine != null && engine.getId() != null) {
				responseBody.add(EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
						.transmissionType(engine.getTransmissionType()).modelId(engine.getModelId())
						.engine(engine.getEngine()).fuelType(engine.getFuelType()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + id + " Engine").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting engine By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getEngineByName(String name) throws NotFoundException {
		try {

			log.info("perform Engine input validation");
			partsDataValidator.validateEngineFullName(name);
			Engine engine = engineRepository.findByFullName(name);

			return GenericResponse.builder().body(engine).code("200")
					.message("successfully Retrived " + name + " Engine").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting engine By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse updateEngine(EngineRequest request) throws NotFoundException {
		try {
			log.info("perform Engine input validation");
			partsDataValidator.validateEngineId(request.getId());
			Engine engine = engineRepository.findById(request.getId()).get();
			if (engine != null) {
				engine.setFullName(request.getFullName());
				engine.setModelId(request.getModelId());
				engine.setEngine(request.getEngine());
				engine.setFuelType(request.getFuelType());
				engine.setTransmissionType(request.getTransmissionType());
				engineRepository.save(engine);
			}
			return GenericResponse.builder().body("Engine is been Updated").code("200")
					.message("successfully Updated " + request.getId() + " Engine").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating Engine");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteEngineById(String id) throws NotFoundException {
		try {
			log.info("perform Engine input validation");
			partsDataValidator.validateEngineId(id);
			Engine engine = engineRepository.findById(id).get();
			log.info("Deleting Engine by Id");
			if (engine != null) {
				engineRepository.delete(engine);
			}
			return GenericResponse.builder().body("Engine is been Deleted").code("200").message("successfully Deleted")
					.status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting Engine");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getEnginesByModelId(String modelId) {

		List<EngineResponse> responseBody = new ArrayList<>();
		try {
			List<Engine> engines = engineRepository.findByModelId(modelId);

			for (Engine engine : engines) {
				responseBody.add(EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
						.transmissionType(engine.getTransmissionType()).fullName(engine.getFullName())
						.modelId(engine.getModelId()).engine(engine.getEngine()).fuelType(engine.getFuelType())
						.build());
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
	public GenericResponse getEnginesByFuelType(String fuelType) {

		List<EngineResponse> responseBody = new ArrayList<>();
		try {
			List<Engine> engines = engineRepository.findByFuelType(fuelType);

			for (Engine engine : engines) {
				responseBody.add(EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
						.transmissionType(engine.getTransmissionType()).fullName(engine.getFullName())
						.modelId(engine.getModelId()).engine(engine.getEngine()).fuelType(engine.getFuelType())
						.build());
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
	public GenericResponse getEnginesByTransmissionType(String transmissionType) {

		List<EngineResponse> responseBody = new ArrayList<>();
		try {
			List<Engine> engines = engineRepository.findByTransmissionType(transmissionType);

			for (Engine engine : engines) {
				responseBody.add(EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
						.transmissionType(engine.getTransmissionType()).fullName(engine.getFullName())
						.modelId(engine.getModelId()).engine(engine.getEngine()).fuelType(engine.getFuelType())
						.build());
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
