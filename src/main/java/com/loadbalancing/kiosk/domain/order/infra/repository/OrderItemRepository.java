package com.loadbalancing.kiosk.domain.order.infra.repository;

import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderItem;
import com.loadbalancing.kiosk.domain.product.dto.ProductResponse;
import com.loadbalancing.kiosk.domain.product.infra.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrder_Id(Long orderId);

    @Query("""
            SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0L)
            FROM OrderItem oi
            WHERE oi.order.createdAt >= :startAt
              AND oi.order.createdAt < :endAt
              AND oi.order.orderStatus <> :excludedStatus
            """)
    long sumSalesBetween(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludedStatus") OrderStatus excludedStatus
    );

    @Query("""
            SELECT p AS product,
                   SUM(oi.quantity) AS totalQuantity
            FROM OrderItem oi
            JOIN oi.product p
            WHERE oi.order.orderStatus <> :excludedStatus
            GROUP BY p
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<ProductSalesAnalysisProjection> findBestSellingProduct(
            @Param("excludedStatus") OrderStatus excludedStatus,
            Pageable pageable
    );

    @Query("""
            SELECT p AS product,
                   oi.quantity AS totalQuantity
            FROM OrderItem oi
            JOIN oi.product p
            WHERE oi.order.orderStatus <> :excludedStatus
            ORDER BY oi.quantity DESC
            """)
    List<ProductSalesAnalysisProjection> findMostPurchasedAtOnceProduct(
            @Param("excludedStatus") OrderStatus excludedStatus,
            Pageable pageable
    );

    interface ProductSalesAnalysisProjection {
        Product getProduct();

        Long getTotalQuantity();
    }
}