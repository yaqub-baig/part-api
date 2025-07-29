package com.autoadda.apis.parts.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.parts.entity.Location;
import com.autoadda.apis.parts.entity.Supplier;
import com.autoadda.apis.parts.exception.AlreadyExistException;
import com.autoadda.apis.parts.exception.NotFoundException;
import com.autoadda.apis.parts.repository.LocationRepository;
import com.autoadda.apis.parts.repository.SupplierRepository;
import com.autoadda.apis.parts.request.LocationRequest;
import com.autoadda.apis.parts.request.SupplierRequest;
import com.autoadda.apis.parts.response.GenericResponse;
import com.autoadda.apis.parts.response.SupplierResponse;
import com.autoadda.apis.parts.util.GUIDGenerator;
import com.autoadda.apis.parts.validator.PartsDataValidator;

import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SupplierService {

	@Autowired
	private PartsDataValidator partsDataValidator;

	@Autowired
	private SupplierRepository supplierRepository;

	@Autowired
	private LocationRepository locationRepository;

	/**
	 * Prepare Supplier entity from SupplierRequest
	 * 
	 * @param request
	 * @return Supplier
	 */
	private Supplier prepareSupplier(SupplierRequest request) {
		return Supplier.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).website(request.getWebsite())
				.email(request.getEmail()).googleMapLink(request.getGoogleMapLink())
				.locationId(request.getLocation().getId()).phoneNumber(request.getPhoneNumber())
				.remarks(request.getRemarks()).timings(request.getTimings()).build();
	}

	/**
	 * Prepare Location entity from LocationRequest
	 * 
	 * @param request
	 * @return Location
	 */
	private Location prepareLocation(LocationRequest request) {
		return Location.builder().id(GUIDGenerator.generateGUID()).city(request.getCity()).country(request.getCountry())
				.county(request.getCounty()).postalCode(request.getPostalCode()).state(request.getState())
				.streetAddress1(request.getStreetAddress1()).streetAddress2(request.getStreetAddress2())
				.type(request.getType()).build();
	}

	/**
	 * Get all suppliers
	 * 
	 * @return GenericResponse
	 */
	@Transactional
	public GenericResponse getAllSuppliers() {
		List<SupplierResponse> responseBody = new ArrayList<>();
		List<Supplier> suppliers = supplierRepository.findAll();
		for (Supplier supplier : suppliers) {
			responseBody.add(SupplierResponse.builder().id(supplier.getId()).name(supplier.getName())
					.website(supplier.getWebsite()).email(supplier.getEmail())
					.googleMapLink(supplier.getGoogleMapLink()).locationId(supplier.getLocationId())
					.phoneNumber(supplier.getPhoneNumber()).remarks(supplier.getRemarks())
					.timings(supplier.getTimings()).build());
		}
		return GenericResponse.builder().body(responseBody).code("200").message("success").status("success").build();
	}

	/**
	 * Create a new supplier
	 * 
	 * @param supplierRequestList
	 * @return GenericResponse
	 * @throws AlreadyExistException
	 */
	@Transactional
	public GenericResponse createSupplier(List<SupplierRequest> supplierRequestList) throws AlreadyExistException {

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

	}

	/**
	 * Get supplier by ID
	 * 
	 * @param id
	 * @return GenericResponse
	 * @throws NotFoundException
	 */
	@Transactional
	public GenericResponse getSupplierById(String id) throws NotFoundException {
		List<SupplierResponse> responseBody = new ArrayList<>();

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

	}

	/**
	 * Get supplier by name
	 * 
	 * @param name
	 * @return GenericResponse
	 * @throws NotFoundException
	 */
	@Transactional
	public GenericResponse getSupplierByName(String name) throws NotFoundException {
		List<SupplierResponse> responseBody = new ArrayList<>();

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
	}

	/**
	 * Update supplier details
	 * 
	 * @param request
	 * @return GenericResponse
	 * @throws NotFoundException
	 */
	@Transactional
	public GenericResponse updateSupplier(SupplierRequest request) throws NotFoundException {
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

	}

	/**
	 * Delete supplier by ID
	 * 
	 * @param id
	 * @return GenericResponse
	 * @throws NotFoundException
	 */
	@Transactional
	public GenericResponse deleteSupplierById(String id) throws NotFoundException {
		log.info("perform Supplier input validation");
		partsDataValidator.validateSupplierId(id);
		Supplier supplier = supplierRepository.findById(id).get();
		log.info("Deleting Supplier by Id");
		if (supplier != null) {
			supplierRepository.delete(supplier);
		}
		return GenericResponse.builder().body("Supplier is been Deleted").code("200").message("successfully Deleted")
				.status("success").build();
	}
}
