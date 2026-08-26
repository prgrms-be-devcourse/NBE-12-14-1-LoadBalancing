package com.loadbalancing.kiosk.domain.order.order.dto.request;

import com.loadbalancing.kiosk.domain.order.orderItem.dto.OrderItemRequest;

import java.util.List;
//todo validation으로 검증(email, address postalCode 등)
public record OrderCreateRequest(
        String email,
        String addressLine1,
        String addressLine2,
        String postalCode,
        List<OrderItemRequest> items
){

}