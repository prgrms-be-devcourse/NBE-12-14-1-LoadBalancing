package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

/**
 * 존재하지 않는 상품 id로 조회/수정/삭제를 시도했을 때 던지는 예외.
 * Service 계층에서 productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id))
 * 형태로 사용한다.
 */
public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException(Long id) {
        super(404, "상품을 찾을 수 없습니다. id=" + id);
    }
}
