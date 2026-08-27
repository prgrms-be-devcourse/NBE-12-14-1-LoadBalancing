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

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 관리자 대시보드 API 테스트. 시딩된 상품 4개(전부 재고 30~60, 품절/재고부족 없음) 기준으로 시작해서,
// 상품 재고를 조작하거나 주문을 넣어서 통계가 실제로 반영되는지 확인.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminDashboardApiTest {

    @Autowired
    MockMvc mockMvc;

    // Spring Boot 4 기본 ObjectMapper 빈은 Jackson 3(tools.jackson.*) 타입이라
    // com.fasterxml.jackson(고전 Jackson 2) 타입으로 autowire가 안 됨 - 그냥 직접 생성해서 씀
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 초기_상태에서는_품절_재고부족_상품이_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProductCount").value(4))
                .andExpect(jsonPath("$.data.outOfStockProducts.length()").value(0))
                .andExpect(jsonPath("$.data.lowStockProducts.length()").value(0))
                .andExpect(jsonPath("$.data.recentProducts.length()").value(4));
    }

    @Test
    void 재고를_0으로_만들면_품절_목록에_잡힌다() throws Exception {
        mockMvc.perform(put("/api/v1/admin/product/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stock", 0))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(jsonPath("$.data.outOfStockProducts.length()").value(1))
                .andExpect(jsonPath("$.data.outOfStockProducts[0].id").value(1));
    }

    @Test
    void 재고를_1에서_10사이로_만들면_재고부족_목록에_잡힌다() throws Exception {
        mockMvc.perform(put("/api/v1/admin/product/2/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stock", 5))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(jsonPath("$.data.lowStockProducts.length()").value(1))
                .andExpect(jsonPath("$.data.lowStockProducts[0].id").value(2))
                .andExpect(jsonPath("$.data.outOfStockProducts.length()").value(0)); // 품절이랑 안 겹침
    }

    @Test
    void 주문을_넣으면_오늘_매출과_주문건수_주문상태별_카운트에_반영된다() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "dashboard-test@example.com",
                "addressLine1", "서울시 강남구",
                "addressLine2", "101동",
                "postalCode", "06000",
                "items", List.of(Map.of("productId", 1, "quantity", 2)) // 예가체프 18000원 x 2 = 36000원
        );

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(jsonPath("$.data.dailyOrderCount").value(1))
                .andExpect(jsonPath("$.data.dailyTotalSales").value(36000))
                .andExpect(jsonPath("$.data.orderStatusCounts[0].status").value("ORDER_RECEIVED"))
                .andExpect(jsonPath("$.data.orderStatusCounts[0].count").value(1));
    }
}
