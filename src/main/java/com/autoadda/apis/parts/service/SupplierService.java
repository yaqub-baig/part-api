package com.autoadda.apis.parts.service;

import com.autoadda.apis.parts.entity.Supplier;
import com.autoadda.apis.parts.exception.AlreadyExistException;
import com.autoadda.apis.parts.exception.NotFoundException;
import com.autoadda.apis.parts.exception.ResourceNotFoundException;
import com.autoadda.apis.parts.repository.MediaFileRepository;
import com.autoadda.apis.parts.repository.SupplierRepository;
import com.autoadda.apis.parts.request.SupplierRequest;
import com.autoadda.apis.parts.util.Status;
import com.autoadda.apis.parts.validator.PartsDataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final MediaFileRepository mediaFileRepository;
    private final LocationService locationService;

    public ResponseEntity<Supplier> createSupplier(SupplierRequest request, String autoAddaId) {
        Supplier supplier = prepareSupplier ( request );
        supplier.setShopLogo ( mediaFileRepository.findById ( request.getShopLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Shop logo not found" ) ) );
        supplier.setShopImages ( mediaFileRepository.findAllById ( request.getShopImageIds ( ) ) );
        supplier.setTradeLicenseAttachment ( mediaFileRepository.findAllById ( request.getTradeLicenseIds ( ) ) );
        supplier.setVatCertificate ( mediaFileRepository.findAllById ( request.getVatCertificateIds ( ) ) );
        supplier.setShopImages ( mediaFileRepository.findAllById ( request.getShopImageIds ( ) ) );
        supplier.setLocation ( locationService.createLocation ( request.getLocation ( ) ) );
        supplier.setSparePartShopOwner ( Integer.parseInt ( autoAddaId ) );
        supplierRepository.save ( supplier );
        return ResponseEntity.ok ( supplier );
    }

    /**
     * Prepare Supplier entity from SupplierRequest
     *
     * @param request
     * @return Supplier
     */
    private Supplier prepareSupplier(SupplierRequest request) {
        Supplier supplier = new Supplier ( );
        supplier.setName ( request.getName ( ) );
        supplier.setSupplierBrandName ( request.getSupplierBrandName ( ) );
        supplier.setSupplierArabicName ( request.getSupplierArabicName ( ) );
        supplier.setTrnNumber ( request.getTrnNumber ( ) );
        supplier.setEmail ( request.getEmail ( ) );
        supplier.setPhoneNumber ( request.getPhoneNumber ( ) );
        supplier.setMobileNumber ( request.getMobileNumber ( ) );
        supplier.setBrands ( request.getBrands ( ) );
        supplier.setModels ( request.getModels ( ) );
        supplier.setWebSite ( request.getWebsite ( ) );
        supplier.setOpeningHours ( request.getOpeningHours ( ) );
        supplier.setClosingHours ( request.getClosingHours ( ) );
        supplier.setWorkingDays ( request.getWorkingDays ( ) );
        supplier.setAccountStatus ( Status.PENDING );
        return supplier;
    }

    public ResponseEntity<List<Supplier>> getSupplier(String autoAddaId) {
        return new ResponseEntity<> ( supplierRepository
                .findBySparePartShopOwner ( Integer.parseInt ( autoAddaId ) )
                .stream ( )
                .filter ( Supplier::isActive )
                .toList ( ), HttpStatus.OK );
    }

    public ResponseEntity<List<Supplier>> getSupplier(Integer supplierId) {
        return new ResponseEntity<> ( supplierRepository
                .findById ( supplierId )
                .stream ( )
                .filter ( Supplier::isActive )
                .toList ( ), HttpStatus.OK );
    }

    public ResponseEntity<List<Supplier>> getAllSuppliers() {

        return new ResponseEntity<> ( supplierRepository
                .findAll ( )
                .stream ( )
                .filter ( Supplier::isActive )
                .toList ( ), HttpStatus.OK );
    }



    public ResponseEntity<Void> deleteSupplierById(Integer id, String autoAddaId) {
        Supplier supplier = supplierRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "Supplier not found" ) );
        Integer ownerId = Integer.parseInt ( autoAddaId );
        if ( !supplier.getSparePartShopOwner ( ).equals ( ownerId ) ) {
            throw  new ForbiddenException ( "You are not authorized to delete this supplier" );
        }
        supplier.setActive ( false );
        supplierRepository.save ( supplier );
        return new ResponseEntity<> ( HttpStatus.OK );
    }

//    public ResponseEntity<Supplier> updateSupplier(SupplierRequest supplier, String autoAddaId, Integer id) {
//
//        return null;
//    }
}
