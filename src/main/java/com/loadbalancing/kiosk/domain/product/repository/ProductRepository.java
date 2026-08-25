package com.loadbalancing.kiosk.domain.product.repository;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
