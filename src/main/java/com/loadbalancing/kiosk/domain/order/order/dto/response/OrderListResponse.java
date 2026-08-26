package com.loadbalancing.kiosk.domain.order.order.dto.response;

import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.domain.order.entity.OrderItem;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderListResponse(
        Long orderId,
        String status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {

    public static OrderListResponse from(
            Order order,
            List<OrderItem> orderItems
    ) {

        List<OrderItemResponse> items = orderItems.stream()
                .map(OrderItemResponse::from)
                .toList();

        return OrderListResponse.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus().getDescription())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}