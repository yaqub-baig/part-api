package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);

    /** A shop's order lines (its listings), newest order first. */
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order o JOIN FETCH oi.part p WHERE p.supplierId = :supplierId "
            + "ORDER BY o.orderDate DESC, oi.id")
    List<OrderItem> findForSupplier(@Param("supplierId") String supplierId);

    /** Lines whose job card update still has to reach garage-api. */
    List<OrderItem> findByPendingGarageSyncIsNotNull();
}