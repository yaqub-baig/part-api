package com.autoadda.apis.part.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.part.entity.Manufacturer;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.ManufacturerRepository;
import com.autoadda.apis.part.request.ManufacturerRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.ManufacturerResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ManufacturerService {

	@Autowired
	private ManufacturerRepository manufacturerRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	/**
	 * Prepares a Manufacturer entity from the given request.
	 *
	 * @param request the ManufacturerRequest containing manufacturer details
	 * @return a Manufacturer entity
	 */
	private Manufacturer prepareManufacturer(ManufacturerRequest request) {
		return Manufacturer.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).build();
	}

	/**
	 * * Retrieves all manufacturers from the repository.
	 * 
	 * @return a GenericResponse containing a list of ManufacturerResponse objects
	 */
	public GenericResponse getAllManufacturers() {
		List<Manufacturer> manufacturers = manufacturerRepository.findAll();
		List<ManufacturerResponse> responseBody = manufacturers.stream()
				.map(m -> ManufacturerResponse.builder().id(m.getId()).name(m.getName()).build())
				.collect(Collectors.toList());

		return GenericResponse.builder().body(responseBody).code("200").message("success").status("success").build();
	}

	/**
	 * Creates a list of manufacturers based on the provided requests.
	 *
	 * @param manufacturerRequestList a list of ManufacturerRequest objects
	 * @return a GenericResponse containing the created manufacturers
	 */
	public GenericResponse createManufacturer(List<ManufacturerRequest> manufacturerRequestList) {
		List<Manufacturer> manufacturerList = new ArrayList<>();

		for (ManufacturerRequest request : manufacturerRequestList) {
			log.info("Validating Manufacturer input");
			partsDataValidator.isManufacturerAlreadyExists(request.getName());

			Manufacturer manufacturer = manufacturerRepository.save(prepareManufacturer(request));
			log.info("Manufacturer created - {}", request);
			manufacturerList.add(manufacturer);
		}

		return GenericResponse.builder().body(manufacturerList).code("201")
				.message("Successfully Created Manufacturer(s)").status("success").build();
	}

	/**
	 * * Retrieves a manufacturer by its ID.
	 * 
	 * @param id the ID of the manufacturer
	 * @return a GenericResponse containing the ManufacturerResponse
	 * @throws NotFoundException
	 */
	public GenericResponse getManufacturerById(String id) throws NotFoundException {
		log.info("Validating Manufacturer ID");
		partsDataValidator.validateManufacturerId(id);

		Manufacturer manufacturer = manufacturerRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Manufacturer not found with ID: " + id));

		ManufacturerResponse response = ManufacturerResponse.builder().id(manufacturer.getId())
				.name(manufacturer.getName()).build();

		return GenericResponse.builder().body(List.of(response)).code("200")
				.message("Successfully Retrieved " + id + " Manufacturer").status("success").build();
	}

	/**
	 * * Retrieves a manufacturer by its name.
	 * 
	 * @param name
	 * @return a GenericResponse containing the Manufacturer entity
	 * @throws NotFoundException
	 */
	public GenericResponse getManufacturerByName(String name) throws NotFoundException {
		log.info("Validating Manufacturer name");
		partsDataValidator.validateManufacturerName(name);

		Manufacturer manufacturer = manufacturerRepository.findByName(name);
		if (manufacturer == null) {
			throw new NotFoundException("Manufacturer not found with name: " + name);
		}

		return GenericResponse.builder().body(manufacturer).code("200")
				.message("Successfully Retrieved " + name + " Manufacturer").status("success").build();
	}

	/**
	 * * Updates an existing manufacturer based on the provided request.
	 * 
	 * @param request the ManufacturerRequest containing updated details
	 * @return a GenericResponse indicating the result of the update operation
	 * @throws NotFoundException
	 */
	public GenericResponse updateManufacturer(ManufacturerRequest request) throws NotFoundException {
		log.info("Validating Manufacturer ID");
		partsDataValidator.validateManufacturerId(request.getId());

		Manufacturer manufacturer = manufacturerRepository.findById(request.getId())
				.orElseThrow(() -> new NotFoundException("Manufacturer not found with ID: " + request.getId()));

		manufacturer.setName(request.getName());
		manufacturerRepository.save(manufacturer);

		return GenericResponse.builder().body("Manufacturer has been Updated").code("200")
				.message("Successfully Updated " + request.getId() + " Manufacturer").status("success").build();
	}

	/**
	 * * Deletes a manufacturer by its ID.
	 * 
	 * @param id
	 * @return a GenericResponse indicating the result of the deletion
	 * @throws NotFoundException
	 */
	public GenericResponse deleteManufacturerById(String id) throws NotFoundException {
		log.info("Validating Manufacturer ID");
		partsDataValidator.validateManufacturerId(id);

		Manufacturer manufacturer = manufacturerRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Manufacturer not found with ID: " + id));

		manufacturerRepository.delete(manufacturer);

		return GenericResponse.builder().body("Manufacturer has been Deleted").code("200")
				.message("Successfully Deleted").status("success").build();
	}
}
