package com.platform.parts.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.parts.entity.Attribute;
import com.platform.parts.exception.AlreadyExistException;
import com.platform.parts.exception.NotFoundException;
import com.platform.parts.repository.AttributeRepository;
import com.platform.parts.request.AttributeRequest;
import com.platform.parts.response.AttributeResponse;
import com.platform.parts.response.GenericResponse;
import com.platform.parts.service.AttributeService;
import com.platform.parts.util.GUIDGenerator;
import com.platform.parts.validator.PartsDataValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AttributeServiceImpl implements AttributeService {

	@Autowired
	private AttributeRepository attributeRepository;

	@Autowired
	private PartsDataValidator partsDataValidator;

	private Attribute prepareAttribute(AttributeRequest request) {
		return Attribute.builder().id(GUIDGenerator.generateGUID()).name(request.getName()).build();
	}

	@Override
	public GenericResponse getAllAttributes() {
		List<AttributeResponse> responseBody = new ArrayList<>();
		try {
			List<Attribute> categories = attributeRepository.findAll();
			for (Attribute attribute : categories) {
				responseBody.add(AttributeResponse.builder().id(attribute.getId()).name(attribute.getName()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting Attributes");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse createAttribute(List<AttributeRequest> attributeRequestList) throws AlreadyExistException {
		try {
			/*
			 * String jsonString = "{\n" + "  \"Length [mm]\": \"placeholder\",\n" +
			 * "  \"Total Length [mm]\": \"placeholder\",\n" +
			 * "  \"Width [mm]\": \"placeholder\",\n" +
			 * "  \"Height [mm]\": \"placeholder\",\n" +
			 * "  \"Weight [kg]\": \"placeholder\",\n" +
			 * "  \"Weight [g]\": \"placeholder\",\n" +
			 * "  \"Net weight [kg]\": \"placeholder\",\n" +
			 * "  \"Quality\": \"placeholder\",\n" + "  \"Material\": \"placeholder\",\n" +
			 * "  \"Number of Parts [parts]\": \"placeholder\",\n" +
			 * "  \"Diameter from [mm]\": \"placeholder\",\n" +
			 * "  \"Diameter to [mm]\": \"placeholder\",\n" +
			 * "  \"Inner Diameter [mm]\": \"placeholder\",\n" +
			 * "  \"Hub Diameter [mm]\": \"placeholder\",\n" +
			 * "  \"Condition\": \"placeholder\",\n" + "  \"Packaging\": \"placeholder\",\n"
			 * + "  \"Number of tools\": \"placeholder\",\n" +
			 * "  \"Universal\": \"placeholder\",\n" +
			 * "  \"Shaft diameter [mm]\": \"placeholder\",\n" +
			 * "  \"Clutch Type\": \"placeholder\",\n" +
			 * "  \"for vehicles with gross vehicle weight [t]\": \"placeholder\",\n" +
			 * "  \"Spanner Size\": \"placeholder\",\n" +
			 * "  \"Brake Type\": \"placeholder\",\n" +
			 * "  \"Braking System\": \"placeholder\",\n" +
			 * "  \"Fitting Position\": \"placeholder\",\n" +
			 * "  \"Diameter [mm]\": \"placeholder\",\n" +
			 * "  \"Brake Disc Type\": \"placeholder\",\n" +
			 * "  \"Rim Hole Number\": \"placeholder\",\n" +
			 * "  \"Bolt Hole Circle Ø [mm]\": \"placeholder\",\n" +
			 * "  \"Brake Disc Thickness [mm]\": \"placeholder\",\n" +
			 * "  \"Drilled\": \"placeholder\",\n" +
			 * "  \"Centering Diameter [mm]\": \"placeholder\",\n" +
			 * "  \"Number of Holes\": \"placeholder\",\n" +
			 * "  \"Surface\": \"placeholder\",\n" + "  \"Machining\": \"placeholder\",\n" +
			 * "  \"Test Mark\": \"placeholder\",\n" +
			 * "  \"Supplementary Article\": \"placeholder\",\n" +
			 * "  \"Supplementary Info\": \"placeholder\",\n" +
			 * "  \"WVA Number\": \"placeholder\",\n" +
			 * "  \"Wear Warning Contact\": \"placeholder\",\n" +
			 * "  \"Thickness [mm]\": \"placeholder\",\n" +
			 * "  \"Fulfils ECE norm\": \"placeholder\",\n" +
			 * "  \"Quantity Unit\": \"placeholder\",\n" +
			 * "  \"Rated Voltage [V]\": \"placeholder\",\n" +
			 * "  \"Packaging width [cm]\": \"placeholder\",\n" +
			 * "  \"Packaging height [cm]\": \"placeholder\",\n" +
			 * "  \"Packaging Depth [cm]\": \"placeholder\",\n" +
			 * "  \"Manufactured in\": \"placeholder\",\n" +
			 * "  \"Inner brake drum diameter [mm]\": \"placeholder\",\n" +
			 * "  \"Thread Size 1\": \"placeholder\",\n" +
			 * "  \"Thread Size 2\": \"placeholder\",\n" +
			 * "  \"Thread Type\": \"placeholder\",\n" +
			 * "  \"Model Year From\": \"placeholder\",\n" +
			 * "  \"Model Year To\": \"placeholder\",\n" +
			 * "  \"Capacity [litre]\": \"placeholder\",\n" +
			 * "  \"DOT specification\": \"placeholder\",\n" +
			 * "  \"Oil\": \"placeholder\",\n" + "  \"Recommendations\": \"placeholder\",\n"
			 * + "  \"Observe service information\": \"placeholder\",\n" +
			 * "  \"Chemical Properties\": \"placeholder\",\n" +
			 * "  \"Packing Type\": \"placeholder\",\n" +
			 * "  \"Manufacturer Restriction\": \"placeholder\",\n" +
			 * "  \"Color\": \"placeholder\",\n" + "  \"Product line\": \"placeholder\",\n"
			 * + "  \"Version\": \"placeholder\",\n" +
			 * "  \"Oil Viscosity Classification SAE\": \"placeholder\",\n" +
			 * "  \"Vehicle type\": \"placeholder\",\n" +
			 * "  \"Transmission Type\": \"placeholder\",\n" +
			 * "  \"Capacity [ml]\": \"placeholder\",\n" +
			 * "  \"Concentrate\": \"placeholder\",\n" +
			 * "  \"Mixing Ratio\": \"placeholder\",\n" + "  \"Season\": \"placeholder\",\n"
			 * + "  \"Compatibility\": \"placeholder\"\n" + "}";
			 * 
			 * // Parse the JSON string into a JSONObject JSONObject jsonObject = new
			 * JSONObject(jsonString);
			 * 
			 * // Retrieve and print all the keys Iterator<String> keys = jsonObject.keys();
			 */
			// Attribute attribute = new Attribute();
			// while (keys.hasNext()) {
			// String key = keys.next();
			// request.setName(key);

			List<Attribute> attributeList = new ArrayList<>();
			for (AttributeRequest attributeRequest : attributeRequestList) {
				partsDataValidator.isAttributeAlreadyExists(attributeRequest.getName());
				Attribute attribute = attributeRepository.save(prepareAttribute(attributeRequest));
				attributeList.add(attribute);
			}

//			log.info("perform Attribute input validation");
//			partsDataValidator.isAttributeAlreadyExists(request.getName());
//
//			Attribute attribute = attributeRepository.save(prepareAttribute(request));
//			log.info("Attribute created - " + request.toString());
			return GenericResponse.builder().body(attributeList).code("201")
					.message("successfully Created Attribute(s)").status("success").build();
		} catch (AlreadyExistException exc) {
			return GenericResponse.builder().status("failed").message("Resource Already Existed").code("400").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating attribute");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getAttributeById(String id) throws NotFoundException {
		List<AttributeResponse> responseBody = new ArrayList<>();
		try {

			log.info("perform Attribute input validation");
			partsDataValidator.validateAttributeId(id);
			Attribute attribute = attributeRepository.findById(id).get();
			if (attribute != null && attribute.getId() != null) {
				responseBody.add(AttributeResponse.builder().id(attribute.getId()).name(attribute.getName()).build());
			}
			return GenericResponse.builder().body(responseBody).code("200")
					.message("successfully Retrived " + id + " Attribute").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting attribute By Id");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse getAttributeByName(String name) throws NotFoundException {
		try {

			log.info("perform Attribute input validation");
			partsDataValidator.validateAttributeName(name);
			Attribute attribute = attributeRepository.findByName(name);

			return GenericResponse.builder().body(attribute).code("200")
					.message("successfully Retrived " + name + " Attribute").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting attribute By Name");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse updateAttribute(AttributeRequest request) throws NotFoundException {
		try {
			log.info("perform Attribute input validation");
			partsDataValidator.validateAttributeId(request.getId());
			Attribute attribute = attributeRepository.findById(request.getId()).get();
			if (attribute != null) {
				attribute.setName(request.getName());
				attributeRepository.save(attribute);
			}
			return GenericResponse.builder().body("Attribute is been Updated").code("200")
					.message("successfully Updated " + request.getId() + " Attribute").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while updating Attribute");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

	@Override
	public GenericResponse deleteAttributeById(String id) throws NotFoundException {
		try {
			log.info("perform Attribute input validation");
			partsDataValidator.validateAttributeId(id);
			Attribute attribute = attributeRepository.findById(id).get();
			log.info("Deleting Attribute by Id");
			if (attribute != null) {
				attributeRepository.delete(attribute);
			}
			return GenericResponse.builder().body("Attribute is been Deleted").code("200")
					.message("successfully Deleted").status("success").build();
		} catch (NotFoundException exc) {
			return GenericResponse.builder().status("failed").message("Resource Not Found").code("404").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while deleting Attribute");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

}
