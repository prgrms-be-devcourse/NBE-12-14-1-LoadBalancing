package com.loadbalancing.kiosk.domain.order.order.dto;

import com.loadbalancing.kiosk.domain.order.orderItem.dto.OrderItemRequest;

import java.util.List;

public record OrderCreateRequest(
        String email,
        String addressLine1,
        String addressLine2,
        String postalCode,
        List<OrderItemRequest> items
){

}