package com.loadbalancing.kiosk.domain.admin.dto.product;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import lombok.Builder;

import java.time.LocalDateTime;

//상품에 있는 전체 필드를 반환하니까 DTO하나 만들어서 상품 리스트 반환하는 애들은 전부 얘를 쓰도록함
@Builder
public record DashboardProductResponse(
        Long productId,
        String title,
        String description,
        int price,
        int stock,
        String thumbnail,
        LocalDateTime createdAt
) {
    public static DashboardProductResponse from(Product product) {
        return DashboardProductResponse.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .thumbnail(product.getThumbnail())
                .createdAt(product.getCreatedAt())
                .build();
    }
}