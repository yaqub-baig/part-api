package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.EnquiryRecipient;
import com.autoadda.apis.part.util.EnquiryRecipientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnquiryRecipientRepository extends JpaRepository<EnquiryRecipient, Integer> {

    List<EnquiryRecipient> findBySupplierIdOrderByCreatedAtDesc(Integer supplierId);

    List<EnquiryRecipient> findBySupplierIdAndStatusOrderByCreatedAtDesc(Integer supplierId, EnquiryRecipientStatus status);

    Optional<EnquiryRecipient> findByEnquiryIdAndSupplierId(Integer enquiryId, Integer supplierId);
}
