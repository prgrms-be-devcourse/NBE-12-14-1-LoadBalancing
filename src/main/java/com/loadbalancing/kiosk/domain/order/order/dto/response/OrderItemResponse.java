package com.loadbalancing.kiosk.domain.order.order.dto.response;

import com.loadbalancing.kiosk.domain.order.entity.OrderItem;
import lombok.Builder;

@Builder
public record OrderItemResponse(
        Long productId,
        String title,
        int price,
        Long quantity
) {

    public static OrderItemResponse from(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .productId(orderItem.getProduct().getId())
                .title(orderItem.getProduct().getTitle())
                .price(orderItem.getProduct().getPrice())
                .quantity(orderItem.getQuantity())
                .build();
    }
}