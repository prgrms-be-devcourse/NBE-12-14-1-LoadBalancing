package com.loadbalancing.kiosk.global.exception.custom;

import com.loadbalancing.kiosk.global.exception.BusinessException;

/**
 * 로그인 실패했을때 던지는 예외
 * 보안을 위해 아이디가 틀렸는지 비밀번호가 틀렸는지 일부러 구분안함
 */
public class InvalidLoginException extends BusinessException {
    public InvalidLoginException() {
        super(401, "아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}
