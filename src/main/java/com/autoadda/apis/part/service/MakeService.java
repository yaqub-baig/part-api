package com.autoadda.apis.part.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.request.MakeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.response.MakeResponse;
import com.autoadda.apis.part.util.GUIDGenerator;
import com.autoadda.apis.part.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MakeService {

	@Autowired
	private MakeRepository makeRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private Make prepareMake(MakeRequest request) {
		return Make.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).build();
	}

	public GenericResponse getAllMakes() {
		List<Make> makes = makeRepository.findAll();
		List<MakeResponse> responseBody = makes.stream()
				.map(make -> MakeResponse.builder().id(make.getId()).name(make.getName()).build())
				.collect(Collectors.toList());

		return GenericResponse.builder().body(responseBody).code("200").message("success").status("success").build();
	}

	public GenericResponse createMake(List<MakeRequest> makeRequestList) {
		List<Make> makeList = new ArrayList<>();

		for (MakeRequest request : makeRequestList) {
			log.info("Performing Make input validation");
			partsDataValidator.isMakeAlreadyExists(request.getName());

			Make make = makeRepository.save(prepareMake(request));
			log.info("Make created - {}", request);
			makeList.add(make);
		}

		return GenericResponse.builder().body(makeList).code("201").message("Successfully Created Make")
				.status("success").build();
	}

	public GenericResponse getMakeById(String id) throws NotFoundException {
		log.info("Validating Make ID");
		partsDataValidator.validateMakeId(id);

		Make make = makeRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Make not found with ID: " + id));

		MakeResponse response = MakeResponse.builder().id(make.getId()).name(make.getName()).build();

		return GenericResponse.builder().body(List.of(response)).code("200")
				.message("Successfully Retrieved " + id + " Make").status("success").build();
	}

	public GenericResponse getMakeByName(String name) throws NotFoundException {
		log.info("Validating Make name");
		partsDataValidator.validateMakeName(name);

		Make make = makeRepository.findByName(name);
		if (make == null) {
			throw new NotFoundException("Make not found with name: " + name);
		}

		return GenericResponse.builder().body(make).code("200").message("Successfully Retrieved " + name + " Make")
				.status("success").build();
	}

	public GenericResponse updateMake(MakeRequest request) throws NotFoundException {
		log.info("Validating Make ID");
		partsDataValidator.validateMakeId(request.getId());

		Make make = makeRepository.findById(request.getId())
				.orElseThrow(() -> new NotFoundException("Make not found with ID: " + request.getId()));

		make.setName(request.getName());
		makeRepository.save(make);

		return GenericResponse.builder().body("Make has been Updated").code("200")
				.message("Successfully Updated " + request.getId() + " Make").status("success").build();
	}

	public GenericResponse deleteMakeById(String id) throws NotFoundException {
		log.info("Validating Make ID");
		partsDataValidator.validateMakeId(id);

		Make make = makeRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Make not found with ID: " + id));

		makeRepository.delete(make);

		return GenericResponse.builder().body("Make has been Deleted").code("200").message("Successfully Deleted")
				.status("success").build();
	}
}
