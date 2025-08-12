package com.autoadda.apis.part.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.part.entity.Engine;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.EngineRepository;
import com.autoadda.apis.part.request.EngineRequest;
import com.autoadda.apis.part.response.EngineResponse;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EngineService {

	@Autowired
	private EngineRepository engineRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	/**
	 * Prepares an Engine entity from the provided EngineRequest.
	 *
	 * @param request The EngineRequest containing engine details.
	 * @return A new Engine entity.
	 */
	private Engine prepareEngine(EngineRequest request) {
		return Engine.builder().id(GUIDGenerator.generateGUID()).fullName(request.getFullName())
				.transmissionType(request.getTransmissionType()).engine(request.getEngine())
				.fuelType(request.getFuelType()).modelId(request.getModelId()).build();
	}

	/**
	 * Retrieves all engines from the repository and maps them to a response object.
	 *
	 * @return GenericResponse containing a list of EngineResponse objects.
	 */
	public GenericResponse getAllEngines() {
		List<Engine> engines = engineRepository.findAll();
		List<EngineResponse> responseBody = engines.stream()
				.map(engine -> EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
						.transmissionType(engine.getTransmissionType()).modelId(engine.getModelId())
						.engine(engine.getEngine()).fuelType(engine.getFuelType()).build())
				.collect(Collectors.toList());

		return GenericResponse.builder().body(responseBody).code("200").message("success").status("success").build();
	}

	/**
	 * * Creates a new engine or a list of engines based on the provided
	 * EngineRequest
	 * 
	 * @param engineRequestList
	 * @return
	 */
	public GenericResponse createEngine(List<EngineRequest> engineRequestList) {
		List<Engine> engineList = new ArrayList<>();

		for (EngineRequest request : engineRequestList) {
			log.info("Performing engine validation");
			partsDataValidator.isEngineAlreadyExists(request.getFullName());

			Engine engine = engineRepository.save(prepareEngine(request));
			log.info("Engine created - {}", request);
			engineList.add(engine);
		}

		return GenericResponse.builder().body(engineList).code("201").message("Successfully created engine(s)")
				.status("success").build();
	}

	/**
	 * * Retrieves an engine by its ID and maps it to a response object.
	 *
	 * @param id The ID of the engine to retrieve.
	 * @return GenericResponse containing the EngineResponse object.
	 * @throws NotFoundException if the engine with the given ID does not exist.
	 */
	public GenericResponse getEngineById(String id) throws NotFoundException {
		log.info("Validating engine ID");
		partsDataValidator.validateEngineId(id);

		Engine engine = engineRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Engine not found with id: " + id));

		EngineResponse response = EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
				.transmissionType(engine.getTransmissionType()).modelId(engine.getModelId()).engine(engine.getEngine())
				.fuelType(engine.getFuelType()).build();

		return GenericResponse.builder().body(List.of(response)).code("200")
				.message("Successfully retrieved engine " + id).status("success").build();
	}

	/**
	 * * Retrieves an engine by its full name and maps it to a response object.
	 * 
	 * @param name
	 * @return
	 * @throws NotFoundException if the engine with the given name does not exist.
	 */
	public GenericResponse getEngineByName(String name) throws NotFoundException {
		log.info("Validating engine name");
		partsDataValidator.validateEngineFullName(name);

		Engine engine = engineRepository.findByFullName(name);
		if (engine == null) {
			throw new NotFoundException("Engine not found with name: " + name);
		}

		return GenericResponse.builder().body(engine).code("200").message("Successfully retrieved engine: " + name)
				.status("success").build();
	}

	/**
	 * * Updates an existing engine based on the provided EngineRequest.
	 * 
	 * @param request
	 * @return
	 * @throws NotFoundException if the engine with the specified ID does not exist.
	 */
	public GenericResponse updateEngine(EngineRequest request) throws NotFoundException {
		log.info("Validating engine ID");
		partsDataValidator.validateEngineId(request.getId());

		Engine engine = engineRepository.findById(request.getId())
				.orElseThrow(() -> new NotFoundException("Engine not found with id: " + request.getId()));

		engine.setFullName(request.getFullName());
		engine.setModelId(request.getModelId());
		engine.setEngine(request.getEngine());
		engine.setFuelType(request.getFuelType());
		engine.setTransmissionType(request.getTransmissionType());
		engineRepository.save(engine);

		return GenericResponse.builder().body("Engine has been updated").code("200")
				.message("Successfully updated engine: " + request.getId()).status("success").build();
	}

	/**
	 * * Deletes an engine by its ID.
	 * 
	 * @param id The ID of the engine to delete.
	 * @return GenericResponse indicating the result of the deletion.
	 * @throws NotFoundException if the engine with the specified ID does not exist.
	 */
	public GenericResponse deleteEngineById(String id) throws NotFoundException {
		log.info("Validating engine ID");
		partsDataValidator.validateEngineId(id);

		Engine engine = engineRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Engine not found with id: " + id));

		engineRepository.delete(engine);

		return GenericResponse.builder().body("Engine has been deleted").code("200").message("Successfully deleted")
				.status("success").build();
	}

	/**
	 * * Retrieves engines by model ID and maps them to a response object.
	 * 
	 * @param modelId
	 * @return
	 */
	public GenericResponse getEnginesByModelId(String modelId) {
		List<Engine> engines = engineRepository.findByModelId(modelId);
		List<EngineResponse> responseBody = engines.stream()
				.map(engine -> EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
						.transmissionType(engine.getTransmissionType()).modelId(engine.getModelId())
						.engine(engine.getEngine()).fuelType(engine.getFuelType()).build())
				.collect(Collectors.toList());

		return GenericResponse.builder().body(responseBody).code("200").message("Successfully retrieved")
				.status("success").build();
	}

	/**
	 * * Retrieves engines by fuel type and maps them to a response object. * This
	 * method filters engines based on the specified fuel type.
	 * 
	 * @param fuelType
	 * @return
	 */
	public GenericResponse getEnginesByFuelType(String fuelType) {
		List<Engine> engines = engineRepository.findByFuelType(fuelType);
		List<EngineResponse> responseBody = engines.stream()
				.map(engine -> EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
						.transmissionType(engine.getTransmissionType()).modelId(engine.getModelId())
						.engine(engine.getEngine()).fuelType(engine.getFuelType()).build())
				.collect(Collectors.toList());

		return GenericResponse.builder().body(responseBody).code("200").message("Successfully retrieved")
				.status("success").build();
	}

	/**
	 * * Retrieves engines by transmission type and maps them to a response object.
	 * * This method filters engines based on the specified transmission type.
	 * 
	 * @param transmissionType
	 * @return
	 */
	public GenericResponse getEnginesByTransmissionType(String transmissionType) {
		List<Engine> engines = engineRepository.findByTransmissionType(transmissionType);
		List<EngineResponse> responseBody = engines.stream()
				.map(engine -> EngineResponse.builder().id(engine.getId()).fullName(engine.getFullName())
						.transmissionType(engine.getTransmissionType()).modelId(engine.getModelId())
						.engine(engine.getEngine()).fuelType(engine.getFuelType()).build())
				.collect(Collectors.toList());

		return GenericResponse.builder().body(responseBody).code("200").message("Successfully retrieved")
				.status("success").build();
	}
}
