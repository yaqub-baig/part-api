package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Location;
import com.autoadda.apis.part.entity.MediaFile;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.AlreadyExistException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.request.SupplierRequest;
import com.autoadda.apis.part.util.Status;
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
        validTradingLicense ( request );
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

    private void validTradingLicense(SupplierRequest supplier) {
        supplierRepository.findByTradeLicenceNumber ( supplier.getTradeLicenceNumber ( ) ).ifPresent ( supplier1 -> {
            throw new AlreadyExistException ( "Supplier with this trade license already exists" );
        } );
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
        supplier.setTradeLicenceNumber ( request.getTradeLicenceNumber ( ) );
        supplier.setTrnNumber ( request.getTrnNumber ( ) );
        supplier.setEmail ( request.getEmail ( ) );
        supplier.setPhoneNumber ( request.getPhoneNumber ( ) );
        supplier.setMobileNumber ( request.getMobileNumber ( ) );
        supplier.setBrands ( request.getBrands ( ) );
        supplier.setModels ( request.getModels ( ) );
        supplier.setWebsite ( request.getWebsite ( ) );
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
        return new ResponseEntity<> ( "Deleted successfully", HttpStatus.OK );
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

        if ( request.getTradeLicenceNumber ( ) != null ) {
            validTradingLicense ( request );
            existing.setTradeLicenceNumber ( request.getTradeLicenceNumber ( ) );
        }

        if ( request.getOpeningHours ( ) != null ) {
            existing.setOpeningHours ( request.getOpeningHours ( ) );
        }
        if ( request.getClosingHours ( ) != null ) {
            existing.setClosingHours ( request.getClosingHours ( ) );
        }
        if ( request.getWorkingDays ( ) != null ) {
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
            existing.setWebsite ( request.getWebsite ( ) );
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
            if ( existing.getShopLogo ( ) != null ) {
                filesToCleanup.add ( existing.getShopLogo ( ).getId ( ) );
            }
            MediaFile mediaFile = mediaFileRepository.findById ( request.getShopLogoId ( ) )
                    .orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
            existing.setShopLogo ( mediaFile );
        }


        if ( request.getTradeLicenseIds ( ) != null ) {
            // Schedule old trade licenses for cleanup
            List<Long> oldTradeLicenseIds = existing.getTradeLicenseAttachment ( ).stream ( )
                    .map ( MediaFile::getId )
                    .toList ( );
            if ( ! oldTradeLicenseIds.isEmpty ( ) ) {
                filesToCleanup.addAll ( oldTradeLicenseIds );
            }

            // Create new trade license collection
            List<MediaFile> newTradeLicenseAttachments = new ArrayList<> ( );
            request.getTradeLicenseIds ( ).forEach ( tid -> {
                MediaFile mediaFile = mediaFileRepository.findById ( tid )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                newTradeLicenseAttachments.add ( mediaFile );
            } );
            existing.getTradeLicenseAttachment().addAll ( newTradeLicenseAttachments );
        }


        if ( request.getShopImageIds ( ) != null ) {
            // Schedule old company images for cleanup
            List<Long> oldShopImageIds = existing.getShopImages ( ).stream ( )
                    .map ( MediaFile::getId )
                    .toList ( );
            if ( ! oldShopImageIds.isEmpty ( ) ) {
                filesToCleanup.addAll ( oldShopImageIds );
            }

            // Create new shop images collection
            List<MediaFile> newShopImages = new ArrayList<> ( );
            request.getShopImageIds ( ).forEach ( cid -> {
                MediaFile mediaFile = mediaFileRepository.findById ( cid )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                newShopImages.add ( mediaFile );
            } );
            existing.getShopImages().addAll ( newShopImages );
        }

        if ( request.getVatCertificateIds ( ) != null ) {
            // Schedule old VAT certificates for cleanup
            List<Long> oldVatCertIds = existing.getVatCertificate ( ).stream ( )
                    .map ( MediaFile::getId )
                    .toList ( );
            if ( ! oldVatCertIds.isEmpty ( ) ) {
                filesToCleanup.addAll ( oldVatCertIds );
            }

            // Create new VAT certificates collection
            List<MediaFile> newVatCertificates = new ArrayList<> ( );
            request.getVatCertificateIds ( ).forEach ( vid -> {
                MediaFile mediaFile = mediaFileRepository.findById ( vid )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                newVatCertificates.add ( mediaFile );
            } );
            existing.getVatCertificate().addAll ( newVatCertificates );
        }
        ResponseEntity<Supplier> supplierResponseEntity = new ResponseEntity<> ( supplierRepository.save ( existing ), HttpStatus.OK );


        if ( ! filesToCleanup.isEmpty ( ) ) {
            scheduleMediaFileCleanup ( filesToCleanup );
        }


        return supplierResponseEntity;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
        // Small delay to ensure transaction is committed
        try {
            Thread.sleep ( 5000 );
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

        long logoCount = supplierRepository.countByShopLogoId ( mediaFileId );
        long shopImageCount = supplierRepository.countByShopImageId ( mediaFileId );
        long tradeLicenseCount = supplierRepository.countByTradeLicenseId ( mediaFileId );
        long vatCertCount = supplierRepository.countByVatCertificateId ( mediaFileId );

        return (logoCount + shopImageCount + tradeLicenseCount + vatCertCount) > 0;

    }

}
