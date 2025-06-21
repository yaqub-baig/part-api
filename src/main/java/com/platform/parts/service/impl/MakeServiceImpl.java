package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.Make;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.MakeRepository;
import com.platform.parts.request.MakeRequest;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.response.MakeResponse;
import com.platform.parts.service.MakeService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MakeServiceImpl implements MakeService {

	@Autowired
	private MakeRepository makeRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private Make prepareMake(MakeRequest request) {
		return Make.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).build();
	}

	@Override
	public GenericResponse getAllMakes() {
		List<MakeResponse> responseBody = new ArrayList<>();
		try {
			List<Make> categories = makeRepository.findAll();
			for (Make make : categories) {
				responseBody.add(MakeResponse.builder().id(make.getId()).name(make.getName()).build());
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
	public GenericResponse createMake(List<MakeRequest> makeRequestList) throws AlreadyExistException {
		try {

			List<Make> makeList = new ArrayList<>();
			for (MakeRequest makeRequest : makeRequestList) {

				log.info("perform Make input validation");
				partsDataValidator.isMakeAlreadyExists(makeRequest.getName());

				Make make = makeRepository.save(prepareMake(makeRequest));
				log.info("Make created - " + makeRequest.toString());
				makeList.add(make);
			}

			return GenericResponse.builder().body(makeList).code("201").message("successfully Created  Make")
					.status("success").build();
		} catch (AlreadyExistException exc) {
			return GenericResponse.builder().status("failed").message("Resource Already Existed").code("400").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating parts make");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getMakeById(String id) throws NotFoundException {
		List<MakeResponse> responseBody = new ArrayList<>();
		try {

			log.info("perform Make input validation");
			partsDataValidator.validateMakeId(id);
			Make make = makeRepository.findById(id).get();
			if (make != null && make.getId() != null) {
				responseBody.add(MakeResponse.builder().id(make.getId()).name(make.getName()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + id + " Make").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting make By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getMakeByName(String name) throws NotFoundException {
		try {

			log.info("perform Make input validation");
			partsDataValidator.validateMakeName(name);
			Make make = makeRepository.findByName(name);

			return GenericResponse.builder().body(make).code("200").message("successfully Retrived " + name + " Make")
					.status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting make By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse updateMake(MakeRequest request) throws NotFoundException {
		try {
			log.info("perform Make input validation");
			partsDataValidator.validateMakeId(request.getId());
			Make make = makeRepository.findById(request.getId()).get();
			if (make != null) {
				make.setName(request.getName());
				makeRepository.save(make);
			}
			return GenericResponse.builder().body("Make is been Updated").code("200")
					.message("successfully Updated " + request.getId() + " Make").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating Make");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteMakeById(String id) throws NotFoundException {
		try {
			log.info("perform Make input validation");
			partsDataValidator.validateMakeId(id);
			Make make = makeRepository.findById(id).get();
			log.info("Deleting Make by Id");
			if (make != null) {
				makeRepository.delete(make);
			}
			return GenericResponse.builder().body("Make is been Deleted").code("200").message("successfully Deleted")
					.status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting Make");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

}
