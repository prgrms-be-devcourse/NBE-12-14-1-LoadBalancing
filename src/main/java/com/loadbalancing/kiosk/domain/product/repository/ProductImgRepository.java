package com.loadbalancing.kiosk.domain.product.repository;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.entity.ProductImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImgRepository extends JpaRepository<ProductImg, Long> {
    List<ProductImg> findAllByProduct(Product products);
}
