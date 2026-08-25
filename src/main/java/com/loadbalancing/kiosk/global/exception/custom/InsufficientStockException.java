package com.loadbalancing.kiosk.global.exception.custom;


import com.loadbalancing.kiosk.global.exception.BusinessException;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(Long productId, int stock, int quantity) {
        super(
                409,
                "재고가 부족합니다. productId=" + productId
                        + ", stock=" + stock
                        + ", quantity=" + quantity
        );
    }
}