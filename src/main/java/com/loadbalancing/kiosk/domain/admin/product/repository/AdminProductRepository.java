package com.loadbalancing.kiosk.domain.admin.product.repository;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminProductRepository
        extends JpaRepository<Product, Long> {
}