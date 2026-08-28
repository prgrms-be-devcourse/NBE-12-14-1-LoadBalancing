package com.loadbalancing.kiosk.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 로그인은 했지만(토큰은 유효) 권한이 없는 요청일 때 실행됨. 지금은 ROLE_ADMIN 하나뿐이라
// 당장 걸릴 일은 없지만, 나중에 역할이 늘어나도 일관된 JSON 응답이 나가도록 미리 둔다.
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"code\":403,\"message\":\"접근 권한이 없습니다.\",\"data\":null}"
        );
    }
}
