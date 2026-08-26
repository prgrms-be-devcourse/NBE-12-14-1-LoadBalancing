package com.loadbalancing.kiosk.domain.order.order.dto.response;

import com.loadbalancing.kiosk.domain.order.entity.Order;
import lombok.Builder;

@Builder
public record OrderCreateResponse(
        Long orderId,
        String status
){
    public static OrderCreateResponse from(Order order){
        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus().getDescription())
                .build();
    }
}
