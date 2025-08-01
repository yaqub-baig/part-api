package com.autoadda.apis.parts.service;

import com.autoadda.apis.parts.entity.Location;
import com.autoadda.apis.parts.entity.MediaFile;
import com.autoadda.apis.parts.entity.Supplier;
import com.autoadda.apis.parts.exception.ResourceNotFoundException;
import com.autoadda.apis.parts.repository.MediaFileRepository;
import com.autoadda.apis.parts.repository.SupplierRepository;
import com.autoadda.apis.parts.request.SupplierRequest;
import com.autoadda.apis.parts.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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


    public ResponseEntity<String> deleteSupplierById(Integer id, String autoAddaId) {
        Supplier supplier = supplierRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "Supplier not found" ) );
        Integer ownerId = Integer.parseInt ( autoAddaId );
        if ( ! supplier.getSparePartShopOwner ( ).equals ( ownerId ) ) {
            throw new ForbiddenException ( "You are not authorized to delete this supplier" );
        }
        supplier.setActive ( false );
        supplierRepository.save ( supplier );
        return new ResponseEntity<> ("Deleted successfully", HttpStatus.OK );
    }

    public ResponseEntity<Supplier> updateSupplier(SupplierRequest request, String autoAddaId, Integer id) {
        Supplier existing = supplierRepository.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Supplier not found with id: " + id ) );


        if ( request.getLocation ( ) != null ) {
            Location existingGarageLocation = existing.getLocation ( );
            Location location = locationService.updateLocation ( existingGarageLocation.getId ( ), request.getLocation ( ) );
            existing.setLocation ( location );
        }
        if ( request.getBrands ( ) != null ) {
            existing.setBrands ( request.getBrands ( ) );
        }
        if ( request.getModels ( ) != null ) {
            existing.setModels ( request.getModels ( ) );
        }
        if ( request.getSupplierArabicName ( ) != null ) {
            existing.setSupplierArabicName ( request.getSupplierArabicName ( ) );
        }

        if(request.getOpeningHours() != null) {
            existing.setOpeningHours ( request.getOpeningHours ( ) );
        }
        if(request.getClosingHours() != null) {
            existing.setClosingHours ( request.getClosingHours ( ) );
        }
        if(request.getWorkingDays() != null) {
            existing.setWorkingDays ( request.getWorkingDays ( ) );
        }
        if ( request.getEmail ( ) != null ) {
            existing.setEmail ( request.getEmail ( ) );
        }

        if ( request.getSupplierBrandName ( ) != null ) {
            existing.setSupplierBrandName ( request.getSupplierBrandName ( ) );
        }
        if ( request.getTrnNumber ( ) != null ) {
            existing.setTrnNumber ( request.getTrnNumber ( ) );
        }
        if ( request.getPhoneNumber ( ) != null ) {
            existing.setPhoneNumber ( request.getPhoneNumber ( ) );
        }
        if ( request.getWebsite ( ) != null ) {
            existing.setWebSite ( request.getWebsite ( ) );
        }
        if ( request.getName ( ) != null ) {
            existing.setName ( request.getName ( ) );
        }
        if ( request.getMobileNumber ( ) != null ) {
            existing.setMobileNumber ( request.getMobileNumber ( ) );
        }

        List<Long> filesToCleanup = new ArrayList<> ( );

        // Handle garage logo update
        if ( request.getShopLogoId ( ) != null ) {
            Long prevLogoId = existing.getShopLogo ( ) != null ? existing.getShopLogo ( ).getId ( ) : null;
            MediaFile mediaFile = mediaFileRepository.findById ( request.getShopLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
            existing.setShopLogo ( mediaFile );
            // Schedule old logo for cleanup if changed
            if ( prevLogoId != null && ! prevLogoId.equals ( mediaFile.getId ( ) ) ) {
                filesToCleanup.add ( prevLogoId );
            }

        }


        if ( request.getTradeLicenseIds ( ) != null && ! request.getTradeLicenseIds ( ).isEmpty ( ) ) {

            filesToCleanup.addAll ( existing.getTradeLicenseAttachment ( ).stream ( ).map ( MediaFile::getId ).toList ( ) );
            existing.getTradeLicenseAttachment ( ).clear ( );
            request.getTradeLicenseIds ( ).forEach ( mediaFileId -> {
                MediaFile mediaFile = mediaFileRepository.findById ( mediaFileId ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                existing.getTradeLicenseAttachment ( ).add ( mediaFile );
            } );
        }


        if ( request.getShopImageIds ( ) != null && ! request.getShopImageIds ( ).isEmpty ( ) ) {
            filesToCleanup.addAll ( existing.getShopImages ( ).stream ( ).map ( MediaFile::getId ).toList ( ) );
            existing.getShopImages ( ).clear ( );
            request.getShopImageIds ( ).forEach ( mediaFileId -> {
                MediaFile mediaFile = mediaFileRepository.findById ( mediaFileId ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                existing.getShopImages ( ).add ( mediaFile );
            } );
        }

        if ( request.getVatCertificateIds ( ) != null && ! request.getVatCertificateIds ( ).isEmpty ( ) ) {
            filesToCleanup.addAll ( existing.getVatCertificate ( ).stream ( ).map ( MediaFile::getId ).toList ( ) );
            existing.getVatCertificate ( ).clear ( );
            request.getVatCertificateIds ( ).forEach ( mediaFileId -> {
                MediaFile mediaFile = mediaFileRepository.findById ( mediaFileId ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                existing.getVatCertificate ( ).add ( mediaFile );
            } );
        }
        if ( ! filesToCleanup.isEmpty ( ) ) {
            scheduleMediaFileCleanup ( filesToCleanup );
        }


        return new ResponseEntity<> ( supplierRepository.save ( existing ), HttpStatus.OK );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
        // Small delay to ensure transaction is committed
        try {
            Thread.sleep ( 3000 );

            for (Long fileId : mediaFileIds) {
                try {
                    // Check if file is still referenced elsewhere before deleting
                    if ( ! isMediaFileStillReferenced ( fileId ) ) {
                        mediaFileRepository.deleteById ( fileId );
                        log.info ( "Successfully cleaned up media file: {}", fileId );
                    } else {
                        log.info ( "Media file {} still referenced, skipping cleanup", fileId );
                    }
                }
                catch ( Exception e ) {
                    log.warn ( "Failed to cleanup media file {}: {}", fileId, e.getMessage ( ) );
                }
            }
        }
        catch ( InterruptedException e ) {
            Thread.currentThread ( ).interrupt ( );
            log.warn ( "Media file cleanup interrupted" );
        }
    }

    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        // Check if this media file is still referenced by any garage
        // This is a safety check to prevent deleting files that might still be in use
        long count = supplierRepository.countByShopLogoId ( mediaFileId );
        return count > 0;
    }

}
