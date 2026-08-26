package com.loadbalancing.kiosk.domain.order.order.dto.response;

import com.loadbalancing.kiosk.domain.order.entity.Order;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderListResponse(
        Long orderId,
        String status,
        LocalDateTime createdAt
){
    public static OrderListResponse from(Order order){
        return OrderListResponse.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus().getDescription())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
