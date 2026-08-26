package com.loadbalancing.kiosk.domain.order.order.dto.response;

import com.loadbalancing.kiosk.domain.order.entity.OrderItem;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderItemDetailResponse(
        Long orderItemId,
        Long productId,
        String title,
        String description,
        int price,
        String thumbnail,
        Long quantity,
        LocalDateTime createdAt
) {

    public static OrderItemDetailResponse from(OrderItem orderItem) {

        Product product = orderItem.getProduct();

        return OrderItemDetailResponse.builder()
                .orderItemId(orderItem.getId())
                .productId(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .quantity(orderItem.getQuantity())
                .createdAt(orderItem.getCreatedAt())
                .build();
    }
}