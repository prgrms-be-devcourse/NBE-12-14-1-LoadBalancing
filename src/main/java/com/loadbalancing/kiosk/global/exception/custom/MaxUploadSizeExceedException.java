package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

public class MaxUploadSizeExceedException extends BusinessException {
    public MaxUploadSizeExceedException() {
        super(400, "사진 용량이 기준치를 초과했습니다.");
    }
}
