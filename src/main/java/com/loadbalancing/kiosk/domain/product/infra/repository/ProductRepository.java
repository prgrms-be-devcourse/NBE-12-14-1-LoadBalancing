package com.loadbalancing.kiosk.domain.product.infra.repository;

import com.loadbalancing.kiosk.domain.product.infra.entity.Product;
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

    Page<Product> findByPriceBetween(Long minPrice, Long maxPrice, Pageable pageable);
}
