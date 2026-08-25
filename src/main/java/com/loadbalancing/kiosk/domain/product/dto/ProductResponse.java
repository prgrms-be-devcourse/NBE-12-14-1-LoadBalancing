package com.loadbalancing.kiosk.domain.product.dto;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import lombok.Builder;

import java.time.LocalDateTime;


public class ProductResponse {

    @Builder
    public record ProductInfo(
            Long id,
            String title,
            String description,
            int price,
            int stock,
            String thumbnail,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public static ProductInfo from(Product product){
            return ProductInfo.builder()
                    .id(product.getId())
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .stock(product.getStock())
                    .thumbnail(product.getThumbnail())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
        }
    }
}

