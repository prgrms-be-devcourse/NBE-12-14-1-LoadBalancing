package com.loadbalancing.kiosk.domain.admin.product.dto;

import com.loadbalancing.kiosk.domain.product.entity.Product;

public record AdminProductResponse(
        Long id,
        String title,
        String description,
        int price,
        int stock,
        String thumbnail
) {
    public static AdminProductResponse from(Product product) {
        return new AdminProductResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getThumbnail()
        );
    }
}