package com.loadbalancing.kiosk.domain.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadbalancing.kiosk.global.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 관리자용 상품 CRUD API 테스트.
// 상품 병합 리팩토링 때 고쳤던 버그 3개(수정 500 에러, stock 미반영, 생성 경로 중복)가
// 다시 회귀하지 않는지 확인하는 목적도 겸함.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminProductApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtProvider jwtProvider;

    // Spring Boot 4 기본 ObjectMapper 빈은 Jackson 3(tools.jackson.*) 타입이라
    // com.fasterxml.jackson(고전 Jackson 2) 타입으로 autowire가 안 됨 - 그냥 직접 생성해서 씀
    ObjectMapper objectMapper = new ObjectMapper();

    // /api/v1/admin/**는 SecurityConfig에서 인증을 요구하므로, 매번 로그인하는 대신
    // JwtProvider로 바로 유효한 토큰을 발급해서 헤더에 붙인다.
    private String adminAuthHeader() {
        return "Bearer " + jwtProvider.generateToken("admin01");
    }

    @Test
    void 상품을_생성하면_201과_함께_생성된_상품정보를_반환한다() throws Exception {
        Map<String, Object> request = Map.of(
                "title", "새 원두",
                "description", "테스트용 설명",
                "price", 20000,
                "stock", 10,
                "thumbnail", "https://picsum.photos/seed/new/400/400",
                "imgs", List.of("https://picsum.photos/seed/new-1/400/400")
        );

        mockMvc.perform(post("/api/v1/admin/product") // 경로 중복 버그(admin/product/product) 재발 확인용
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("새 원두"))
                .andExpect(jsonPath("$.data.price").value(20000));

        mockMvc.perform(get("/api/v1/auth/product/list"))
                .andExpect(jsonPath("$.data.content.length()").value(5)); // 시딩 4개 + 방금 생성 1개
    }

    @Test
    void 상품_수정하면_이름_가격_재고가_전부_반영된다() throws Exception {
        // Product.update()가 stock을 안 넣던 버그 회귀 확인용 - 재고까지 같이 바뀌는지 검증
        Map<String, Object> request = Map.of(
                "title", "수정된 예가체프",
                "description", "수정된 설명",
                "price", 25000,
                "stock", 99,
                "thumbnail", "https://picsum.photos/seed/updated/400/400",
                "imageUrls", List.of("https://picsum.photos/seed/updated-1/400/400")
        );

        mockMvc.perform(put("/api/v1/admin/product/1")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 예가체프"))
                .andExpect(jsonPath("$.data.price").value(25000))
                .andExpect(jsonPath("$.data.stock").value(99))
                .andExpect(jsonPath("$.data.imgs.length()").value(1)); // ProductResponse.ProductInfo 필드명은 imgs

        // 상세 조회로 실제 DB에도 반영됐는지 다시 확인
        mockMvc.perform(get("/api/v1/auth/product/detail/1"))
                .andExpect(jsonPath("$.data.stock").value(99));
    }

    @Test
    void 존재하지_않는_상품을_수정하면_404가_난다() throws Exception {
        Map<String, Object> request = Map.of(
                "title", "x", "description", "x", "price", 1000, "stock", 1,
                "thumbnail", "x", "imageUrls", List.of("x")
        );

        mockMvc.perform(put("/api/v1/admin/product/9999")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 재고만_수정하면_다른_필드는_그대로다() throws Exception {
        mockMvc.perform(put("/api/v1/admin/product/1/stock")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stock", 7))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/product/detail/1"))
                .andExpect(jsonPath("$.data.stock").value(7))
                .andExpect(jsonPath("$.data.title").value("에티오피아 예가체프")); // 제목은 안 바뀜
    }

    @Test
    void 상품을_삭제하면_더이상_조회되지_않는다() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/product/1")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/product/detail/1"))
                .andExpect(status().isNotFound()); // @SQLRestriction으로 소프트 삭제 반영 확인

        mockMvc.perform(get("/api/v1/auth/product/list"))
                .andExpect(jsonPath("$.data.content.length()").value(3));
    }
}
