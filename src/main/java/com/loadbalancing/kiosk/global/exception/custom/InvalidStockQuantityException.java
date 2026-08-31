package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

public class InvalidStockQuantityException extends BusinessException {
    public InvalidStockQuantityException(int quantity) {
        super(400, "주문 수량은 1개 이상이어야 합니다. quantity=" + quantity);
    }
}
