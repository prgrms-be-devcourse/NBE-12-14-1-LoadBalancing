package com.loadbalancing.kiosk.global.security;

import com.loadbalancing.kiosk.global.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// SecurityConfig의 인증 규칙(anyRequest().permitAll() 제거) 자체를 검증하는 테스트.
// "관리자 API는 토큰 없으면 401" / "공개(auth) API는 토큰 없어도 통과" / "잘못된 토큰도 401"을 확인.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityConfigApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtProvider jwtProvider;

    @Test
    void 토큰_없이_관리자_API를_호출하면_401과_공용_에러_형식을_받는다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void 잘못된_토큰으로_관리자_API를_호출해도_401이_난다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer this-is-not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 유효한_관리자_토큰이면_관리자_API가_정상_동작한다() throws Exception {
        String token = "Bearer " + jwtProvider.generateToken("admin01");

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void 공개_API는_토큰_없이도_호출된다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/product/list"))
                .andExpect(status().isOk());
    }
}
