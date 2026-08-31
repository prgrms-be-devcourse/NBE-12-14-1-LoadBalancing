package com.loadbalancing.kiosk.domain.admin.dto;

/**
 * 관리자 로그인 요청 DTO
 * Security 내장 로그인을 안쓰고 따로 만든 이유는
 * Security의 formLogin은 form데이터를 기대하고있어 json으로 데이터를 통일한 지금과 어울리지 않음
 * formLogin/httpBasic 둘 다 JWT를 발급해주는 기능이 없어서,
 * 어차피 커스텀 코드를 짜야 한다면 별도 컨트롤러로 만드는 게 더 단순함
 */
public class AdminRequest {
    public record LoginRequest(
        String adminId,
        String password
    ){}
}
