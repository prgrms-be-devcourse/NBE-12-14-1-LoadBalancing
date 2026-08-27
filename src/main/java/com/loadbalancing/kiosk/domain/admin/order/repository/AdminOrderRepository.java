package com.loadbalancing.kiosk.domain.admin.order.repository;

import com.loadbalancing.kiosk.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminOrderRepository extends JpaRepository<Order, Long> {

    @Query("""

            SELECT o FROM Order o
    WHERE (:keyword IS NULL OR o.email LIKE %:keyword%)
      AND (:startDate IS NULL OR o.createdAt >= :startDate)
      AND (:endDate IS NULL OR o.createdAt < :endDate)
    """)
    Page<Order> search(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    }