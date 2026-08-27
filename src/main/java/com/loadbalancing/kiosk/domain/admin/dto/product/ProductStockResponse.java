package com.loadbalancing.kiosk.domain.admin.dto.product;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import lombok.Builder;

//상품 별 재고를 전달하기 위한 dto
@Builder
public record ProductStockResponse(
        Long productId,
        String title,
        int stock
) {

    public static ProductStockResponse from(Product product) {
        return ProductStockResponse.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .stock(product.getStock())
                .build();
    }
}