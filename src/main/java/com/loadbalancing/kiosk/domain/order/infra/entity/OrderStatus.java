package com.loadbalancing.kiosk.domain.order.infra.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDER_RECEIVED("주문접수"),
    PAYMENT_COMPLETED("결제완료"),
    IN_DELIVERY("배송중"),
    DELIVERED("배송완료"),
    CANCELLED("주문취소");

    private final String description;
}