package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.Location;
import com.platform.parts.entity.Supplier;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.LocationRepository;
import com.platform.parts.repository.SupplierRepository;
import com.platform.parts.request.LocationRequest;
import com.platform.parts.request.SupplierRequest;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.response.SupplierResponse;
import com.platform.parts.service.SupplierService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SupplierServiceImpl implements SupplierService {

	@Autowired
	private PartsDataValidator partsDataValidator;

	@Autowired
	private SupplierRepository supplierRepository;

	@Autowired
	private LocationRepository locationRepository;

	private Supplier prepareSupplier(SupplierRequest request) {
		return Supplier.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).website(request.getWebsite())
				.email(request.getEmail()).googleMapLink(request.getGoogleMapLink())
				.locationId(request.getLocation().getId()).phoneNumber(request.getPhoneNumber())
				.remarks(request.getRemarks()).timings(request.getTimings()).build();
	}

	private Location prepareLocation(LocationRequest request) {
		return Location.builder().id(GUIDGenerator.generateGUID()).city(request.getCity()).country(request.getCountry())
				.county(request.getCounty()).postalCode(request.getPostalCode()).state(request.getState())
				.streetAddress1(request.getStreetAddress1()).streetAddress2(request.getStreetAddress2())
				.type(request.getType()).build();
	}

	@Override
	public GenericResponse getAllSuppliers() {
		List<SupplierResponse> responseBody = new ArrayList<>();
		try {
			List<Supplier> suppliers = supplierRepository.findAll();
			for (Supplier supplier : suppliers) {
				responseBody.add(SupplierResponse.builder().id(supplier.getId()).name(supplier.getName())
						.website(supplier.getWebsite()).email(supplier.getEmail())
						.googleMapLink(supplier.getGoogleMapLink()).locationId(supplier.getLocationId())
						.phoneNumber(supplier.getPhoneNumber()).remarks(supplier.getRemarks())
						.timings(supplier.getTimings()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting suppliers");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse createSupplier(List<SupplierRequest> supplierRequestList) throws AlreadyExistException {
		try {

			List<Supplier> supplierList = new ArrayList<>();
			for (SupplierRequest supplierRequest : supplierRequestList) {

				log.info("perform Supplier input validation");
				partsDataValidator.isSupplierAlreadyExists(supplierRequest.getName());

				Location location = locationRepository.save(prepareLocation(supplierRequest.getLocation()));
				log.info("Location created - " + location.toString());
				supplierRequest.getLocation().setId(location.getId());

				Supplier supplier = supplierRepository.save(prepareSupplier(supplierRequest));
				log.info("Supplier created - " + supplierRequest.toString());
				supplierList.add(supplier);
			}

			return GenericResponse.builder().body(supplierList).code("201").message("successfully Created  Supplier")
					.status("success").build();
		} catch (AlreadyExistException exc) {
			return GenericResponse.builder().status("failed").message("Resource Already Existed").code("400").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating parts supplier");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getSupplierById(String id) throws NotFoundException {
		List<SupplierResponse> responseBody = new ArrayList<>();
		try {

			log.info("perform Supplier input validation");
			partsDataValidator.validateSupplierId(id);
			Supplier supplier = supplierRepository.findById(id).get();
			if (supplier != null && supplier.getId() != null) {
				responseBody.add(SupplierResponse.builder().id(supplier.getId()).name(supplier.getName())
						.website(supplier.getWebsite()).email(supplier.getEmail())
						.googleMapLink(supplier.getGoogleMapLink()).locationId(supplier.getLocationId())
						.phoneNumber(supplier.getPhoneNumber()).remarks(supplier.getRemarks())
						.timings(supplier.getTimings()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + id + " Supplier").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting supplier By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getSupplierByName(String name) throws NotFoundException {
		List<SupplierResponse> responseBody = new ArrayList<>();
		try {

			List<Supplier> suppliers = supplierRepository.findByNameContaining(name);
			for (Supplier supplier : suppliers) {
				responseBody.add(SupplierResponse.builder().id(supplier.getId()).name(supplier.getName())
						.website(supplier.getWebsite()).email(supplier.getEmail())
						.googleMapLink(supplier.getGoogleMapLink()).locationId(supplier.getLocationId())
						.phoneNumber(supplier.getPhoneNumber()).remarks(supplier.getRemarks())
						.timings(supplier.getTimings()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + name + " Supplier").status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting supplier By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse updateSupplier(SupplierRequest request) throws NotFoundException {
		try {
			log.info("perform Supplier input validation");
			partsDataValidator.validateSupplierId(request.getId());
			Supplier supplier = supplierRepository.findById(request.getId()).get();
			log.info("Updating service status");
			if (supplier != null) {
				supplier.setName(request.getName());
				supplier.setTimings(request.getTimings());
				supplier.setEmail(request.getEmail());
				supplier.setGoogleMapLink(request.getGoogleMapLink());
				supplier.setLocationId(request.getLocation().getId());
				supplier.setPhoneNumber(request.getPhoneNumber());
				supplier.setWebsite(request.getWebsite());
				supplierRepository.save(supplier);
			}
			return GenericResponse.builder().body("Supplier is been Updated").code("200")
					.message("successfully Updated " + request.getId() + " Supplier").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating Supplier");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteSupplierById(String id) throws NotFoundException {
		try {
			log.info("perform Supplier input validation");
			partsDataValidator.validateSupplierId(id);
			Supplier supplier = supplierRepository.findById(id).get();
			log.info("Deleting Supplier by Id");
			if (supplier != null) {
				supplierRepository.delete(supplier);
			}
			return GenericResponse.builder().body("Supplier is been Deleted").code("200")
					.message("successfully Deleted").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting Supplier");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}
}
