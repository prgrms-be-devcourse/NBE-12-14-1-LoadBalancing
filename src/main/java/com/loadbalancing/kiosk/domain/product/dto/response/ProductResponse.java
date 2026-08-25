package com.loadbalancing.kiosk.domain.product.dto.response;

import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.entity.ProductImg;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;


public class ProductResponse {

    @Builder
    public record ProductInfo(
            Long id,
            String title,
            String description,
            int price,
            int stock,
            String thumbnail,
            List<ProductImgInfo> imgs,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        // 1. 이미지 응답 전용 record 정의
        @Builder
        public record ProductImgInfo(
                Long id,
                String url
        ) {
            public static ProductImgInfo from(ProductImg img) {
                return ProductImgInfo.builder()
                        .id(img.getId())
                        .url(img.getUrl())
                        .build();
            }
        }
        //이건 목록용 응답(목록에서는 굳이 세부 사진까지 필요 없기 때문)
        public static ProductInfo from(Product product) {
            return from(product, List.of());
        }

        public static ProductInfo from(Product product, List<ProductImg> imgs){

            List<ProductImgInfo> imgInfos = imgs.stream()
                 .map(ProductImgInfo::from)
                 .toList();

            return ProductInfo.builder()
                    .id(product.getId())
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .stock(product.getStock())
                    .thumbnail(product.getThumbnail())
                    .imgs(imgInfos)
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
        }
    }
}