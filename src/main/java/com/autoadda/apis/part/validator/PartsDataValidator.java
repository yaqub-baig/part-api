package com.autoadda.apis.part.validator;

import com.autoadda.apis.part.entity.*;
import com.autoadda.apis.part.exception.AlreadyExistException;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartsDataValidator {
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final EngineRepository engineRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ProductRepository productRepository;
    private final AttributeRepository attributeRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;
    private final ProductAttributeRepository productAttributeRepository;

    // Category Validation
    public void isCategoryAlreadyExists(String name) throws AlreadyExistException {
        Optional<Category> byName = categoryRepository.findByName ( name );
        if ( byName.isPresent ( ) )
            throw new AlreadyExistException ( "Category already exist.." );
    }

    public void validateCategoryId(String id) throws NotFoundException {
        log.info ( "Category Id: " + id );
        Optional<Category> optCategory = categoryRepository.findById ( id );

        if ( ! optCategory.isPresent () ) {
            throw new NotFoundException ( "Catagory not found" );
        }
    }

    public void validateCategoryName(String name) {
        categoryRepository.findByName ( name ).orElseThrow ( () -> new NotFoundException ( "Category not found" ) );


    }

    // Supplier Validation
    public void isSupplierAlreadyExists(String name) throws AlreadyExistException {
        Supplier supplier = supplierRepository.findByName ( name );

        if ( null != supplier ) {
            throw new AlreadyExistException ( "Supplier already exist.." );
        }
    }

//	public void validateSupplierId(String id) throws NotFoundException {
//		Optional<Supplier> optSupplier = supplierRepository.findById(id);
//		if (!optSupplier.isPresent()) {
//			throw new NotFoundException("Supplier not found");
//		}
//	}

    public void validateSupplierName(String name) throws NotFoundException {
        Supplier supplier = supplierRepository.findByName ( name );

        if ( null == supplier ) {
            throw new NotFoundException ( "Supplier not found" );
        }
    }

    // Model Validation
    public void validateMakeId(String id) throws NotFoundException {
        Optional<Make> optMake = makeRepository.findById ( id );
        if ( ! optMake.isPresent ( ) ) {
            throw new NotFoundException ( "Make not found" );
        }
    }

    public void validateMakeName(String name) throws NotFoundException {
        Make make = makeRepository.findByName ( name );

        if ( null == make ) {
            throw new NotFoundException ( "Make not found" );
        }
    }

    public void isMakeAlreadyExists(String name) throws AlreadyExistException {
        Make make = makeRepository.findByName ( name );

        if ( null != make ) {
            throw new AlreadyExistException ( "Make already exist.." );
        }
    }

    // Vehicle Type Validation
    public void validateVehicleTypeId(String id) throws NotFoundException {
        Optional<VehicleType> optVehicleType = vehicleTypeRepository.findById ( id );
        if ( ! optVehicleType.isPresent ( ) ) {
            throw new NotFoundException ( "Vehicle Type not found" );
        }
    }

    public void validateVehicleTypeName(String name) throws NotFoundException {
        VehicleType vehicleType = vehicleTypeRepository.findByName ( name );

        if ( null == vehicleType ) {
            throw new NotFoundException ( "Vehicle Type not found" );
        }
    }

    public void isVehicleTypeAlreadyExists(String name) throws AlreadyExistException {
        VehicleType vehicleType = vehicleTypeRepository.findByName ( name );

        if ( null != vehicleType ) {
            throw new AlreadyExistException ( "Vehicle Type already exist.." );
        }
    }

    // Model Validation
    public void validateModelId(String id) throws NotFoundException {
        Optional<Model> optModel = modelRepository.findById ( id );
        if ( ! optModel.isPresent ( ) ) {
            throw new NotFoundException ( "Model not found" );
        }
    }

    public void validateModelName(String name) throws NotFoundException {
        Model model = modelRepository.findByName ( name );

        if ( null == model ) {
            throw new NotFoundException ( "Model not found" );
        }
    }

    public void isModelAlreadyExists(String name) throws AlreadyExistException {
        Model model = modelRepository.findByName ( name );

        if ( null != model ) {
            throw new AlreadyExistException ( "Model already exist.." );
        }
    }

    // Engine Validation
    public void validateEngineId(String id) throws NotFoundException {
        Optional<Engine> optEngine = engineRepository.findById ( id );
        if ( ! optEngine.isPresent ( ) ) {
            throw new NotFoundException ( "Engine not found" );
        }
    }

    public void validateEngineFullName(String name) throws NotFoundException {
        Engine engine = engineRepository.findByFullName ( name );

        if ( null == engine ) {
            throw new NotFoundException ( "Engine not found" );
        }
    }

    public void isEngineAlreadyExists(String name) throws AlreadyExistException {
        Engine engine = engineRepository.findByFullName ( name );

        if ( null != engine ) {
            throw new AlreadyExistException ( "Engine already exist.." );
        }
    }

    // Manufacturer Validation
    public void validateManufacturerId(String id) throws NotFoundException {
        Optional<Manufacturer> optManufacturer = manufacturerRepository.findById ( id );
        if ( ! optManufacturer.isPresent ( ) ) {
            throw new NotFoundException ( "Manufacturer not found" );
        }
    }

    public void validateManufacturerName(String name) throws NotFoundException {
        Manufacturer manufacturer = manufacturerRepository.findByName ( name );

        if ( null == manufacturer ) {
            throw new NotFoundException ( "Manufacturer not found" );
        }
    }

    public void isManufacturerAlreadyExists(String name) throws AlreadyExistException {
        Manufacturer manufacturer = manufacturerRepository.findByName ( name );

        if ( null != manufacturer ) {
            throw new AlreadyExistException ( "Manufacturer already exist.." );
        }
    }

    // Product Validation
    public void validateProductId(String id) throws NotFoundException {
        Optional<Product> optProduct = productRepository.findById ( id );
        if ( ! optProduct.isPresent ( ) ) {
            throw new NotFoundException ( "Product not found" );
        }
    }

    public void validateProductName(String name) throws NotFoundException {
        Product product = productRepository.findByName ( name );

        if ( null == product ) {
            throw new NotFoundException ( "Product not found" );
        }
    }

    public void isProductAlreadyExists(String name) throws AlreadyExistException {
        Product product = productRepository.findByName ( name );

        if ( null != product ) {
            throw new AlreadyExistException ( "Product already exist.." );
        }
    }

    // Attribute Validation
    public void validateAttributeId(String id) throws NotFoundException {
        Optional<Attribute> optAttribute = attributeRepository.findById ( id );
        if ( ! optAttribute.isPresent ( ) ) {
            throw new NotFoundException ( "Attribute not found" );
        }
    }

    public void validateAttributeName(String name) throws NotFoundException {
        Optional<Attribute> attribute = attributeRepository.findByName ( name );

        if ( null == attribute ) {
            throw new NotFoundException ( "Attribute not found" );
        }
    }

    public void isAttributeAlreadyExists(String name) throws AlreadyExistException {
        Optional<Attribute> attribute = attributeRepository.findByName ( name );

        if ( null != attribute ) {
            throw new AlreadyExistException ( "Attribute already exist.." );
        }
    }

    // Category Attribute Validation
    public void validateCategoryIdAndAttributeId(String categoryId, String attributeId) throws NotFoundException {
        CategoryAttribute categoryAttribute = categoryAttributeRepository.findByCategoryIdAndAttributeId ( categoryId,
                attributeId );
        if ( categoryAttribute == null ) {
            throw new NotFoundException ( "Category Attribute not found" );
        }
    }

    public void validateCategoryAttributeId(String id) throws NotFoundException {
        Optional<CategoryAttribute> optCategoryAttribute = categoryAttributeRepository.findById ( id );
        if ( ! optCategoryAttribute.isPresent ( ) ) {
            throw new NotFoundException ( "Category Attribute not found" );
        }
    }

    // Product Attribute Validation
    public void validateProductAttributeId(String id) throws NotFoundException {
        Optional<ProductAttribute> optProductAttribute = productAttributeRepository.findById ( id );
        if ( ! optProductAttribute.isPresent ( ) ) {
            throw new NotFoundException ( "Product Attribute not found" );
        }
    }
}
