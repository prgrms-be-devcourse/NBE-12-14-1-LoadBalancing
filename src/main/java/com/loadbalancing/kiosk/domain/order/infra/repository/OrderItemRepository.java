package com.loadbalancing.kiosk.domain.order.orderItem.repository;

import com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse;
import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.domain.order.entity.OrderItem;
import org.springframework.data.domain.Page;
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
              AND oi.order.orderStatus <> com.loadbalancing.kiosk.domain.order.entity.OrderStatus.CANCELLED
            """)
    long sumSalesBetween(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    //가장 많이 팔리는 상품 조회 -> 상품별 전체 판매 수량 SUM(quantity)이 가장 큰 상품
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
        WHERE oi.order.orderStatus <> com.loadbalancing.kiosk.domain.order.entity.OrderStatus.CANCELLED
        GROUP BY p.id, p.title, p.price, p.stock, p.thumbnail
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<ProductSalesAnalysisResponse> findBestSellingProducts();

    //한번에 가장 많이 구매된 상품 조회 -> 단일 주문상품 OrderItem.quantity가 가장 큰 상품
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
        WHERE oi.order.orderStatus <> com.loadbalancing.kiosk.domain.order.entity.OrderStatus.CANCELLED
        ORDER BY oi.quantity DESC
        """)
    List<ProductSalesAnalysisResponse> findMostPurchasedAtOnceProducts();

}