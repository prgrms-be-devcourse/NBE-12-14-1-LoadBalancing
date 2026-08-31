package com.loadbalancing.kiosk.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

/**
 * OncePerRequestFilter 상속시 요청 하나당 딱 한번 실행되는 필터가 됨
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    // doFilterInternal: OncePerRequestFilter가 강제하는 메소드. "요청 올 때마다 이 안의 코드를 실행해라"는 뜻
    protected void doFilterInternal(
        HttpServletRequest request,  //들어온 HTTP 요청 정보
        HttpServletResponse response,//나갈 응답(이 필터에서 직접 사용하지 않고 다음 단계로 넘긴다.
        FilterChain filterChain      // 다음 필터로 넘겨라를 실행시키는 도구
    ) throws ServletException, IOException {
        // 아래에서 만든 메서드, 헤더에서 토큰 문자열만 뽑아냄
        String token = resolveToken(request);

        if(token != null && jwtProvider.validateToken(token)) {
            String adminId = jwtProvider.getAdminId(token);
            //UsernamePasswordAuthenticationToken: Spring Security가 "이 사람은 인증됐다"를 표현할 때 쓰는 객체
            // 원래는 로그인폼 아이디/비번 담는 용도로 만들어진 클래스인데, 여기선 "인증 완료된 사람 정보" 담는 그릇으로 재활용
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    // 대충 각 역할에 대해서 설명함
                    adminId, // 누구임
                    null, // 비밀번호 자라인데 이미 토큰 인증 끝나서 필요x 그래서 null
                    // 이 사람이 가진 권한 목록. ROLE_ADMIN 권한 하나 부여
                    // 현재는 Admin 계정만 있어서 필요없지만 추후 권한 관리 할때 편함
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")) 
                );
            /**
             *** 중요 **
             * SecurityContextHolder: 스프링 시큐리티가 "지금 이 요청을 누가 보냈는지"를 저장해두는 창고
             * 여기에 등록해두면, 이후 컨트롤러/서비스 어디서든 "지금 요청자가 누구냐" 꺼내볼 수 있음
             */
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 토큰이 없거나 잘못됐어도 여기는 무조건 실행됨 (필터에서 막지 않음)
        // "인증 필요한 API인지"는 SecurityConfig의 authorizeHttpRequests가 나중에 따로 판단하기 때문
        filterChain.doFilter(request, response);

    }

    //간단한 문자놀이
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

}
