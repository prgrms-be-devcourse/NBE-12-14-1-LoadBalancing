package com.loadbalancing.kiosk.domain.product.repository;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAll(Pageable pageable);

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

}
