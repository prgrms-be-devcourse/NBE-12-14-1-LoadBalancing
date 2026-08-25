package com.loadbalancing.kiosk.domain.admin.product.dto;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.entity.ProductImg;
import lombok.Builder;

import java.util.List;

@Builder
public record AdminProductResponse(
        Long id,
        String title,
        String description,
        int price,
        int stock,
        String thumbnail,
        List<ProductImageInfo> images
) {

    public static AdminProductResponse from(
            Product product,
            List<ProductImg> productImages
    ) {
        List<ProductImageInfo> images = productImages.stream()
                .map(ProductImageInfo::from)
                .toList();

        return AdminProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .thumbnail(product.getThumbnail())
                .images(images)
                .build();
    }

    @Builder
    public record ProductImageInfo(
            Long id,
            String url
    ) {
        public static ProductImageInfo from(ProductImg productImg) {
            return ProductImageInfo.builder()
                    .id(productImg.getId())
                    .url(productImg.getUrl())
                    .build();
        }
    }
}