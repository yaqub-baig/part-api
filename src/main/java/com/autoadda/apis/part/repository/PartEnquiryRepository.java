package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.PartEnquiry;
import com.autoadda.apis.part.util.EnquiryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PartEnquiryRepository extends JpaRepository<PartEnquiry, Integer> {

    List<PartEnquiry> findByVehicleOwnerIdOrderByCreatedAtDesc(Integer vehicleOwnerId);

    List<PartEnquiry> findByVehicleOwnerIdAndStatusOrderByCreatedAtDesc(Integer vehicleOwnerId, EnquiryStatus status);

    long countByVehicleOwnerIdAndStatusIn(Integer vehicleOwnerId, Collection<EnquiryStatus> statuses);

    List<PartEnquiry> findByStatusInAndExpiresAtBefore(Collection<EnquiryStatus> statuses, LocalDateTime time);

    /** Locks the enquiry row so two quotes can't be accepted at the same time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM PartEnquiry e WHERE e.id = :id")
    Optional<PartEnquiry> findByIdForUpdate(@Param("id") Integer id);
}
