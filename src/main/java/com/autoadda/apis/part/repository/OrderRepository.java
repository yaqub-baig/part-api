package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.util.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByVehicleOwnerId(Integer vehicleOwnerId);

    List<Order> findByVehicleOwnerIdOrderByOrderDateDesc(Integer vehicleOwnerId);

    /** Row-locked read so webhook, expiry job and user cancel can't change the same order at once. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.gatewayOrderReference = :ref")
    Optional<Order> findByGatewayOrderReferenceForUpdate(@Param("ref") String gatewayOrderReference);

    Optional<Order> findByGatewayOrderReference(String gatewayOrderReference);

    @Query("SELECT o.id FROM Order o WHERE o.status = :status AND o.paymentExpiresAt < :now")
    List<Integer> findIdsByStatusAndPaymentExpiresAtBefore(@Param("status") Status status, @Param("now") LocalDateTime now);
}
