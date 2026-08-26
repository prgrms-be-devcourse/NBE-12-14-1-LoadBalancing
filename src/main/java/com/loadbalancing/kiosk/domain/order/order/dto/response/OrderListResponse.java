package com.loadbalancing.kiosk.domain.order.order.dto.response;

import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.domain.order.entity.OrderItem;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderListResponse(
        Long orderId,
        Long productId,
        String title,
        int price,
        Long quantity,
        String status,
        LocalDateTime createdAt
) {

    public static OrderListResponse from(OrderItem orderItem) {
        return OrderListResponse.builder()
                .orderId(orderItem.getOrder().getId())
                .productId(orderItem.getProduct().getId())
                .title(orderItem.getProduct().getTitle())
                .price(orderItem.getProduct().getPrice())
                .quantity(orderItem.getQuantity())
                .status(orderItem.getOrder()
                        .getOrderStatus()
                        .getDescription())
                .createdAt(orderItem.getCreatedAt())
                .build();
    }
}