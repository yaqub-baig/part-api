package com.autoadda.apis.part.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoadda.apis.part.entity.Attribute;
import com.autoadda.apis.part.exception.AlreadyExistException;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.repository.AttributeRepository;
import com.autoadda.apis.part.request.AttributeRequest;
import com.autoadda.apis.part.response.AttributeResponse;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.util.GUIDGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AttributeService {

	@Autowired
	private AttributeRepository attributeRepository;

	private AttributeResponse mapToResponse(Attribute attribute) {

		return AttributeResponse.builder().id(attribute.getId()).name(attribute.getName()).build();
	}

	/**
	 * Retrieves all attributes from the repository and maps them to a response
	 * object.
	 *
	 * @return GenericResponse containing a list of AttributeResponse objects.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getAllAttributes() {
		List<Attribute> attributes = attributeRepository.findAll();

		List<AttributeResponse> responseList = attributes.stream().map(this::mapToResponse).toList();
		return GenericResponse.builder().body(responseList).code("200").message("success").status("success").build();

	}

	/**
	 * Creates a new attribute or a list of attributes based on the provided request
	 * objects.
	 *
	 * @param attributeRequestList List of AttributeRequest objects containing
	 *                             attribute details.
	 * @return GenericResponse containing the created attributes.
	 * @throws AlreadyExistException if an attribute with the same name already
	 *                               exists.
	 */
	@Transactional(readOnly = true)
	public GenericResponse createAttribute(List<AttributeRequest> attributeRequestList) {

		List<Attribute> attributes = attributeRequestList.stream().map(req -> {
			if (attributeRepository.existsByName(req.getName())) {
				throw new AlreadyExistException("Attribute with name '" + req.getName() + "' already exists");
			}

			Attribute attribute = Attribute.builder().id(GUIDGenerator.generateGUID()).name(req.getName()).build();
			return attribute;
		}).toList();
		List<Attribute> saved = attributeRepository.saveAll(attributes);
		return GenericResponse.builder().body(saved).code("201").message("successfully Created Attribute(s)")
				.status("success").build();

	}

	/**
	 * Retrieves an attribute by its ID and maps it to a response object.
	 *
	 * @param id The ID of the attribute to retrieve.
	 * @return GenericResponse containing the AttributeResponse object.
	 * @throws NotFoundException if the attribute with the specified ID does not
	 *                           exist.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getAttributeById(String id) throws NotFoundException {
		Attribute attribute = attributeRepository.findById(id)

				.orElseThrow(() -> new ResourceNotFoundException("Attribute not found with ID: " + id));
		return GenericResponse.builder().status("success").code("200")

				.message("Successfully retrieved Product with ID " + id).body(List.of(mapToResponse(attribute)))
				.build();
	}

	/**
	 * Retrieves an attribute by its name and maps it to a response object.
	 *
	 * @param name The name of the attribute to retrieve.
	 * @return GenericResponse containing the AttributeResponse object.
	 * @throws NotFoundException if the attribute with the specified name does not
	 *                           exist.
	 */
	@Transactional(readOnly = true)
	public GenericResponse getAttributeByName(String name) throws NotFoundException {
		Attribute attribute = attributeRepository.findByName(name)
				.orElseThrow(() -> new ResourceNotFoundException("Attribute not found with Name: " + name));
		return GenericResponse.builder().status("success").code("200")

				.message("Successfully retrieved Product with ID " + name).body(List.of(mapToResponse(attribute)))
				.build();
	}

	/**
	 * Updates an existing attribute based on the provided request object.
	 *
	 * @param updateRequest AttributeRequest object containing the updated details.
	 * @return GenericResponse containing the updated attribute.
	 * @throws NotFoundException if the attribute with the specified ID does not
	 *                           exist.
	 */
	@Transactional(readOnly = true)
	public GenericResponse updateAttribute(AttributeRequest updateRequest) throws NotFoundException {

		log.info("perform Attribute input validation");
		Attribute existing = attributeRepository.findById(updateRequest.getId())

				.orElseThrow(
						() -> new ResourceNotFoundException("Attribute not found with ID: " + updateRequest.getId()));

		if (updateRequest.getName() != null)
			existing.setName(updateRequest.getName());

		Attribute updated = attributeRepository.save(existing);
		return GenericResponse.builder().status("success").code("200")
				.message("Successfully updated Product with ID " + updateRequest.getId()).body(List.of(updated))
				.build();

	}

	/**
	 * Deletes an attribute by its ID.
	 *
	 * @param id The ID of the attribute to delete.
	 * @return GenericResponse indicating the success of the deletion.
	 * @throws NotFoundException if the attribute with the specified ID does not
	 *                           exist.
	 */
	@Transactional(readOnly = true)
	public GenericResponse deleteAttributeById(String id) throws NotFoundException {
		Attribute existing = attributeRepository.findById(id)

				.orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

		attributeRepository.delete(existing);

		return GenericResponse.builder().status("success").code("200")

				.message("Successfully deleted Product with ID " + id).body("Deleted").build();
	}

}
