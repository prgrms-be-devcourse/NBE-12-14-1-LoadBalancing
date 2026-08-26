package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(Long id) {
        super(404, "존재하지 않는 주문입니다. id=" + id);
    }
}