package com.loadbalancing.kiosk.domain.product.repository;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAll(Pageable pageable);

    // SELECT * FROM product WHERE title LIKE %:keyword%
    Page<Product> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
