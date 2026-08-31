package com.loadbalancing.kiosk.domain.product.infra.repository;

import com.loadbalancing.kiosk.domain.product.infra.entity.Product;
import com.loadbalancing.kiosk.domain.product.infra.entity.ProductImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImgRepository extends JpaRepository<ProductImg, Long> {
    List<ProductImg> findAllByProduct(Product products);
    List<ProductImg> findAllByProductId(Long productId);

    void deleteAllByProductId(Long product_id);
}
