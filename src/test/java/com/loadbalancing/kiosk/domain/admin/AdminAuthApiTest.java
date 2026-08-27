package com.loadbalancing.kiosk.domain.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 관리자 로그인 API 테스트. DataInitializer가 시딩한 admin01/admin1234! 계정 기준.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminAuthApiTest {

    @Autowired
    MockMvc mockMvc;

    // Spring Boot 4 기본 ObjectMapper 빈은 Jackson 3(tools.jackson.*) 타입이라
    // com.fasterxml.jackson(고전 Jackson 2) 타입으로 autowire가 안 됨 - 그냥 직접 생성해서 씀
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 올바른_아이디_비밀번호로_로그인하면_토큰을_받는다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("adminId", "admin01", "password", "admin1234!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void 비밀번호가_틀리면_401과_공용_에러메시지를_받는다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("adminId", "admin01", "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    // 주의: AdminAuthService는 "아이디/비밀번호 중 뭐가 틀렸는지 구분 안 한다"고 주석에 써놨는데,
    // 실제로는 존재하지 않는 아이디일 때 InvalidLoginException이 아니라 순수 IllegalArgumentException을 던져서
    // GlobalExceptionHandler의 공용 500 핸들러로 빠짐 (비밀번호 틀림=401, 아이디 없음=500 으로 실제로는 구분됨).
    // 의도한 동작은 아닌 것 같아서, 지금 실제 동작(500) 기준으로 테스트를 작성해둠 - 고치실 거면 이 테스트도 401/InvalidLoginException 쪽으로 같이 바꾸면 됨.
    @Test
    void 존재하지_않는_아이디면_현재는_500이_난다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("adminId", "no-such-admin", "password", "whatever"))))
                .andExpect(status().isInternalServerError());
    }
}
