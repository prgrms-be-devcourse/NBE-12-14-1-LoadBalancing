package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

public class ProductImgNotFoundException
        extends BusinessException {

    public ProductImgNotFoundException(Long imageId) {
        super(
                404,
                "상품 이미지를 찾을 수 없습니다. imageId="
                        + imageId
        );
    }
}