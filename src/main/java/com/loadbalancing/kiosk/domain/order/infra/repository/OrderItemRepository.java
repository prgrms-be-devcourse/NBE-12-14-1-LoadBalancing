package com.loadbalancing.kiosk.domain.order.infra.repository;

import com.loadbalancing.kiosk.domain.order.infra.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrder_Id(Long orderId);

    Boolean existsByOrder_Id(Long orderId);

    @Query("""
            SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0)
            FROM OrderItem oi
            WHERE oi.order.createdAt >= :startAt
              AND oi.order.createdAt < :endAt
              AND oi.order.orderStatus <> OrderStatus.CANCELLED
            """)
    long sumSalesBetween(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}