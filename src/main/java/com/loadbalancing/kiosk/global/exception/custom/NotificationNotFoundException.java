package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

public class NotificationNotFoundException extends BusinessException {
    public NotificationNotFoundException(Long id) {
        super(404, "존재하지 않는 알림입니다. id=" + id);
    }
}
