package com.loadbalancing.kiosk.domain.order.orderItem.dto;

public record OrderItemRequest(
        Long productId,
        Long quantity
){

}