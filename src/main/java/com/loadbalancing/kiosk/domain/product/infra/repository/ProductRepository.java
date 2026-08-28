package com.loadbalancing.kiosk.domain.product.infra.repository;

import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.product.dto.ProductResponse;
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


    @Query("""
            SELECT p AS product,
                   COALESCE(SUM(oi.quantity), 0L) AS totalQuantity
            FROM Product p
            LEFT JOIN OrderItem oi
                ON oi.product = p
               AND oi.order.orderStatus <> :excludedStatus
            GROUP BY p
            ORDER BY COALESCE(SUM(oi.quantity), 0L) ASC
            """)
    List<ProductSalesAnalysisProjection> findWorstSellingProduct(
            @Param("excludedStatus") OrderStatus excludedStatus,
            Pageable pageable
    );

    interface ProductSalesAnalysisProjection {
        Product getProduct();

        Long getTotalQuantity();
    }
}

    Page<Product> findByPriceBetween(Integer minPrice, Integer maxPrice, Pageable pageable);

    Page<Product> findByTitleContainingIgnoreCaseAndPriceBetween(
            String title, Integer minPrice, Integer maxPrice, Pageable pageable);
}

