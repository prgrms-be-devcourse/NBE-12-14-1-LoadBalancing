package com.loadbalancing.kiosk.domain.admin.order.dto;


import com.loadbalancing.kiosk.domain.order.entity.Order;
import lombok.Builder;

@Builder
public record AdminOrderResponse(
        Long orderId,
        String status
){
    public static AdminOrderResponse from(Order order){
        return AdminOrderResponse.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus().getDescription())
                .build();
    }
}
