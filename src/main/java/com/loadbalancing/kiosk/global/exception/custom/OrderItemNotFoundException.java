package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

public class OrderItemNotFoundException extends BusinessException {
    public OrderItemNotFoundException(Long id) {
        super(404, "존재하지 않는 주문상품입니다. id=" + id);
    }
}