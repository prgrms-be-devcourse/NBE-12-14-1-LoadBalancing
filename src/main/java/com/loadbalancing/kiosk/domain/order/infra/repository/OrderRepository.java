package com.loadbalancing.kiosk.domain.order.infra.repository;


import com.loadbalancing.kiosk.domain.order.infra.entity.Order;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByEmailAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String email,
            LocalDateTime start,
            LocalDateTime end
    );

    Page<Order> findAllByEmail(
            String email,
            Pageable pageable
    );

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndOrderStatusNot(
            LocalDateTime startAt,
            LocalDateTime endAt,
            OrderStatus orderStatus
    );

    @Query("""
            SELECT o.orderStatus AS status,
                   COUNT(o) AS count
            FROM Order o
            GROUP BY o.orderStatus
            """)
    List<OrderStatusCountProjection> countGroupByOrderStatus();

    interface OrderStatusCountProjection {
        OrderStatus getStatus();

        long getCount();
    }

    @Query("""
                SELECT o FROM Order o
                WHERE (:keyword IS NULL OR o.email LIKE %:keyword%)
                  AND (:status IS NULL OR o.orderStatus = :status)
                  AND (:startDate IS NULL OR o.createdAt >= :startDate)
                  AND (:endDate IS NULL OR o.createdAt < :endDate)
        """)
    Page<Order> search(
        @Param("keyword") String keyword,
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}