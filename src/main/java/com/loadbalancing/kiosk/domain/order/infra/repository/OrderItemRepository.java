package com.loadbalancing.kiosk.domain.order.infra.repository;

import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrder_Id(Long orderId);

    @Query("""
            SELECT COALESCE(SUM(oi.quantity * oi.product.price), 0)
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

    // 가장 많이 팔린 상품
    @Query("""
            SELECT new com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse(
                p.id,
                p.title,
                p.price,
                p.stock,
                p.thumbnail,
                SUM(oi.quantity)
            )
            FROM OrderItem oi
            JOIN oi.product p
            WHERE oi.order.orderStatus <> :excludedStatus
            GROUP BY p.id, p.title, p.price, p.stock, p.thumbnail
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<ProductSalesAnalysisResponse> findBestSellingProduct(
            @Param("excludedStatus") OrderStatus excludedStatus,
            Pageable pageable
    );

    // 한번에 가장 많이 구매된 상품
    @Query("""
            SELECT new com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse(
                p.id,
                p.title,
                p.price,
                p.stock,
                p.thumbnail,
                oi.quantity
            )
            FROM OrderItem oi
            JOIN oi.product p
            WHERE oi.order.orderStatus <> :excludedStatus
            ORDER BY oi.quantity DESC
            """)
    List<ProductSalesAnalysisResponse> findMostPurchasedAtOnceProduct(
            @Param("excludedStatus") OrderStatus excludedStatus,
            Pageable pageable
    );
}