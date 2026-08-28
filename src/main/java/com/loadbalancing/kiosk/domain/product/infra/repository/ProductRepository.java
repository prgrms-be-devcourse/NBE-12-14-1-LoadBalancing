package com.loadbalancing.kiosk.domain.product.infra.repository;

import com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.product.infra.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAll(Pageable pageable);

    Page<Product> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    List<Product> findTop5ByOrderByCreatedAtDesc();

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM product
                    WHERE deleted_at IS NOT NULL
                    """,
            nativeQuery = true
    )
    long countDeletedProducts();

    // 가장 안 팔린 상품
    @Query("""
            SELECT new com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse(
                p.id,
                p.title,
                p.price,
                p.stock,
                p.thumbnail,
                COALESCE(SUM(oi.quantity), 0L)
            )
            FROM Product p
            LEFT JOIN OrderItem oi
                ON oi.product = p
               AND oi.order.orderStatus <> :excludedStatus
            GROUP BY p.id, p.title, p.price, p.stock, p.thumbnail
            ORDER BY COALESCE(SUM(oi.quantity), 0) ASC
            """)
    List<ProductSalesAnalysisResponse> findWorstSellingProduct(
            @Param("excludedStatus") OrderStatus excludedStatus,
            Pageable pageable
    );
}
