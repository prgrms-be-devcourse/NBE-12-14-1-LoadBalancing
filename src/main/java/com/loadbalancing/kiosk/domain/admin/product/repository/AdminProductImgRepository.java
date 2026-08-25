package com.loadbalancing.kiosk.domain.admin.product.repository;

import com.loadbalancing.kiosk.domain.product.entity.ProductImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminProductImgRepository extends JpaRepository<ProductImg, Long> {

    List<ProductImg> findAllByProductId(Long productId);

    void deleteAllByProductId(Long productId);
}