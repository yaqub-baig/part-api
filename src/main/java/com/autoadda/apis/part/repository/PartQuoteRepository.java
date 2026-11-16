package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.PartQuote;
import com.autoadda.apis.part.util.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PartQuoteRepository extends JpaRepository<PartQuote, Integer> {

    List<PartQuote> findByEnquiryIdOrderByUnitPriceAsc(Integer enquiryId);

    List<PartQuote> findByEnquiryIdIn(Collection<Integer> enquiryIds);

    Optional<PartQuote> findByEnquiryIdAndSupplierId(Integer enquiryId, Integer supplierId);

    boolean existsByEnquiryIdAndStatusAndValidUntilAfter(Integer enquiryId, QuoteStatus status, LocalDateTime time);
}
