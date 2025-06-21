package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.Manufacturer;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.ManufacturerRepository;
import com.platform.parts.request.ManufacturerRequest;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.response.ManufacturerResponse;
import com.platform.parts.service.ManufacturerService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ManufacturerServiceImpl implements ManufacturerService {

	@Autowired
	private ManufacturerRepository manufacturerRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private Manufacturer prepareManufacturer(ManufacturerRequest request) {
		return Manufacturer.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).build();
	}

	@Override
	public GenericResponse getAllManufacturers() {
		List<ManufacturerResponse> responseBody = new ArrayList<>();
		try {
			List<Manufacturer> categories = manufacturerRepository.findAll();
			for (Manufacturer manufacturer : categories) {
				responseBody.add(
						ManufacturerResponse.builder().id(manufacturer.getId()).name(manufacturer.getName()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting categories");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse createManufacturer(List<ManufacturerRequest> manufacturerRequestList)
			throws AlreadyExistException {
		try {

			List<Manufacturer> manufacturerList = new ArrayList<>();
			for (ManufacturerRequest manufacturerRequest : manufacturerRequestList) {

				log.info("perform Manufacturer input validation");
				partsDataValidator.isManufacturerAlreadyExists(manufacturerRequest.getName());

				Manufacturer manufacturer = manufacturerRepository.save(prepareManufacturer(manufacturerRequest));
				log.info("Manufacturer created - " + manufacturerRequest.toString());
				manufacturerList.add(manufacturer);
			}
			return GenericResponse.builder().body(manufacturerList).code("201")
					.message("successfully Created  Manufacturer(s)").status("success").build();
		} catch (AlreadyExistException exc) {
			return GenericResponse.builder().status("failed").message("Resource Already Existed").code("400").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating parts manufacturer");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getManufacturerById(String id) throws NotFoundException {
		List<ManufacturerResponse> responseBody = new ArrayList<>();
		try {

			log.info("perform Manufacturer input validation");
			partsDataValidator.validateManufacturerId(id);
			Manufacturer manufacturer = manufacturerRepository.findById(id).get();
			if (manufacturer != null && manufacturer.getId() != null) {
				responseBody.add(
						ManufacturerResponse.builder().id(manufacturer.getId()).name(manufacturer.getName()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + id + " Manufacturer").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting manufacturer By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getManufacturerByName(String name) throws NotFoundException {
		try {

			log.info("perform Manufacturer input validation");
			partsDataValidator.validateManufacturerName(name);
			Manufacturer manufacturer = manufacturerRepository.findByName(name);

			return GenericResponse.builder().body(manufacturer).code("200")
					.message("successfully Retrived " + name + " Manufacturer").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting manufacturer By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse updateManufacturer(ManufacturerRequest request) throws NotFoundException {
		try {
			log.info("perform Manufacturer input validation");
			partsDataValidator.validateManufacturerId(request.getId());
			Manufacturer manufacturer = manufacturerRepository.findById(request.getId()).get();
			if (manufacturer != null) {
				manufacturer.setName(request.getName());
				manufacturerRepository.save(manufacturer);
			}
			return GenericResponse.builder().body("Manufacturer is been Updated").code("200")
					.message("successfully Updated " + request.getId() + " Manufacturer").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating Manufacturer");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteManufacturerById(String id) throws NotFoundException {
		try {
			log.info("perform Manufacturer input validation");
			partsDataValidator.validateManufacturerId(id);
			Manufacturer manufacturer = manufacturerRepository.findById(id).get();
			log.info("Deleting Manufacturer by Id");
			if (manufacturer != null) {
				manufacturerRepository.delete(manufacturer);
			}
			return GenericResponse.builder().body("Manufacturer is been Deleted").code("200")
					.message("successfully Deleted").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting Manufacturer");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

}
