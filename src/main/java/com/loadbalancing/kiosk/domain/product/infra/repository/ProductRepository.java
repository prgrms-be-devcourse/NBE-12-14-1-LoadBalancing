package com.loadbalancing.kiosk.domain.product.repository;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAll(Pageable pageable);

    // SELECT * FROM product WHERE title LIKE %:keyword%
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

    //가장 안팔리는 상품 -> 상품별 전체 판매 수량 SUM(quantity)이 가장 작은 상품
    @Query("""
        SELECT new com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse(
            p.id,
            p.title,
            p.price,
            p.stock,
            p.thumbnail,
            COALESCE(SUM(oi.quantity), 0)
        )
        FROM Product p
        LEFT JOIN OrderItem oi
            ON oi.product = p
           AND oi.order.orderStatus <> com.loadbalancing.kiosk.domain.order.entity.OrderStatus.CANCELLED
        GROUP BY p.id, p.title, p.price, p.stock, p.thumbnail
        ORDER BY COALESCE(SUM(oi.quantity), 0) ASC
        """)
    List<ProductSalesAnalysisResponse> findWorstSellingProducts();
}
