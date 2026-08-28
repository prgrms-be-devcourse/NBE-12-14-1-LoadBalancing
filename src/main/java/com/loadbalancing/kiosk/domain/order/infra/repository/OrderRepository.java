package com.loadbalancing.kiosk.domain.order.order.repository;


import com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse;
import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}