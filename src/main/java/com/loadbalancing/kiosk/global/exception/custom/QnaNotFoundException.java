package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

public class QnaNotFoundException extends BusinessException {
    public QnaNotFoundException(Long id) {
        super(404, "존재하지 않는 문의입니다. id=" + id);
    }
}