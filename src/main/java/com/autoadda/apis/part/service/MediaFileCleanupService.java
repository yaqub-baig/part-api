package com.autoadda.apis.part.service;

import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileCleanupService {

    private final MediaFileRepository mediaFileRepository;
    private final SupplierRepository supplierRepository;

    /**
     * Schedule media file cleanup asynchronously in a new transaction
     * This method runs asynchronously after a 5-second delay to ensure
     * the main transaction is committed before cleanup begins
     *
     * @param mediaFileIds list of media file IDs to cleanup
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleCleanup(List<Long> mediaFileIds) {
        try {
            Thread.sleep ( 5000 );
            cleanupMediaFiles ( mediaFileIds );
        }
        catch ( InterruptedException e ) {
            Thread.currentThread ( ).interrupt ( );
            log.warn ( "Media file cleanup interrupted" );
        }
    }

    /**
     * Cleanup media files that are no longer referenced
     * Each file is processed independently - failures don't stop other deletions
     *
     * @param mediaFileIds list of media file IDs to cleanup
     */
    private void cleanupMediaFiles(List<Long> mediaFileIds) {
        for (Long fileId : mediaFileIds) {
            try {
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

    /**
     * Check if a media file is still referenced by any supplier
     *
     * @param mediaFileId the media file ID to check
     * @return true if the file is still referenced, false otherwise
     */
    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        long logoCount = supplierRepository.countByShopLogoId ( mediaFileId );
        long shopImageCount = supplierRepository.countByShopImageId ( mediaFileId );
        long tradeLicenseCount = supplierRepository.countByTradeLicenseId ( mediaFileId );
        long vatCertCount = supplierRepository.countByVatCertificateId ( mediaFileId );

        return (logoCount + shopImageCount + tradeLicenseCount + vatCertCount) > 0;
    }
}
