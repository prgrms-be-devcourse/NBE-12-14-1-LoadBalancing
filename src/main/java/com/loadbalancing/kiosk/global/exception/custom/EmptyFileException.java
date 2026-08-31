package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

public class EmptyFileException extends BusinessException {
    public EmptyFileException() {
        super(400, "파일이 존재하지 않습니다.");
    }
}
