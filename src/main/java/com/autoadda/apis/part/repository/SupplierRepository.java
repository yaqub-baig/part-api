package com.autoadda.apis.part.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.part.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

	Supplier findByName(String name);

	List<Supplier> findByNameContaining(String name);

	List<Supplier> findBySparePartShopOwner(Integer i);

	@Query("SELECT COUNT(s) FROM Supplier s WHERE s.shopLogo.id = :mediaFileId")
	long countByShopLogoId(@Param("mediaFileId") Long mediaFileId);

	// Check if media file is referenced in shop images
	@Query("SELECT COUNT(s) FROM Supplier s JOIN s.shopImages si WHERE si.id = :mediaFileId")
	long countByShopImageId(@Param("mediaFileId") Long mediaFileId);

	// Check if media file is referenced in trade license attachments
	@Query("SELECT COUNT(s) FROM Supplier s JOIN s.tradeLicenseAttachment tla WHERE tla.id = :mediaFileId")
	long countByTradeLicenseId(@Param("mediaFileId") Long mediaFileId);

	// Check if media file is referenced in VAT certificates
	@Query("SELECT COUNT(s) FROM Supplier s JOIN s.vatCertificate vc WHERE vc.id = :mediaFileId")
	long countByVatCertificateId(@Param("mediaFileId") Long mediaFileId);

	Optional<Supplier> findByTradeLicenceNumber(String tradeLicenceNumber);
}
