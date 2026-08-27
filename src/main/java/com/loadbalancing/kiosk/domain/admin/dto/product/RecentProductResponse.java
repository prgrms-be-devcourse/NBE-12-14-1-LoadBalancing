package com.loadbalancing.kiosk.domain.admin.dto.product;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import lombok.Builder;

import java.time.LocalDateTime;

//최근 등록 상품 리스트용 dto
@Builder
public record RecentProductResponse(
        Long productId,
        String title,
        int price,
        int stock,
        String thumbnail,
        LocalDateTime createdAt
) {

    public static RecentProductResponse from(Product product) {
        return RecentProductResponse.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .stock(product.getStock())
                .thumbnail(product.getThumbnail())
                .createdAt(product.getCreatedAt())
                .build();
    }
}