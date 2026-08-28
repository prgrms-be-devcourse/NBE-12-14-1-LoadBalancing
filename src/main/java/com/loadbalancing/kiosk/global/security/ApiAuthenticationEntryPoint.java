package com.loadbalancing.kiosk.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
    로그인(토큰) 없이 인증이 필요한 API를 호출했을 때 실행됨.
    커스텀 EntryPoint가 없으면 Spring Security 기본 동작(빈 403)이 나가서
    나머지 API들이랑 응답 형태(ApiResponse)가 안 맞으므로 직접 JSON을 만들어 내려준다.
    GlobalExceptionHandler는 컨트롤러 진입 이후 예외만 잡기 때문에, 필터 단계에서 막히는
    이 케이스는 GlobalExceptionHandler를 안 거치고 여기서 바로 응답해야 한다.
*/
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"code\":401,\"message\":\"로그인이 필요합니다.\",\"data\":null}"
        );
    }
}
