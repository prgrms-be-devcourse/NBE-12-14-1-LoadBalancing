package com.loadbalancing.kiosk.domain.admin.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 관리자용 주문 관리 API 테스트 (상태변경/삭제/검색).
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminOrderApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EntityManager entityManager;

    // Spring Boot 4 기본 ObjectMapper 빈은 Jackson 3(tools.jackson.*) 타입이라
    // com.fasterxml.jackson(고전 Jackson 2) 타입으로 autowire가 안 됨 - 그냥 직접 생성해서 씀
    ObjectMapper objectMapper = new ObjectMapper();

    // 매 테스트마다 주문을 하나 새로 만들어서 그 orderId를 돌려줌 (email로 구분 가능하게)
    private Long createOrder(String email) throws Exception {
        Map<String, Object> request = Map.of(
                "email", email,
                "addressLine1", "서울시 강남구",
                "addressLine2", "101동",
                "postalCode", "06000",
                "items", List.of(Map.of("productId", 1, "quantity", 1))
        );

        String body = mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("orderId").asLong();
    }

    @Test
    void 주문상태를_변경하면_한글_설명으로_응답한다() throws Exception {
        Long orderId = createOrder("status-test@example.com");

        mockMvc.perform(patch("/api/v1/admin/order/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PAYMENT_COMPLETED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.status").value("결제완료"));
    }

    @Test
    void 존재하지_않는_주문_상태를_바꾸면_404가_난다() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/order/9999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "CANCELLED"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void 주문을_삭제하면_검색결과에서도_빠진다() throws Exception {
        Long orderId = createOrder("delete-test@example.com");

        // @Transactional 테스트라 전체가 한 트랜잭션/영속성 컨텍스트를 공유함.
        // 방금 만든 Order/OrderItem이 세션에 그대로 남은 채로 바로 delete()를 부르면,
        // "OrderItem이 참조하는 Order가 transient다"라는 Hibernate 예외가 남
        // (Order에 OrderItem 쪽 cascade/orphanRemoval이 없는 상태에서 @SQLDelete remove()를 걸 때 생기는
        // 문제라, 실제 운영에서는 요청마다 세션이 따로 끝나서 거의 안 겪지만 테스트에선 바로 재현됨).
        // 방금 만든 것들을 DB에 반영하고 세션에서 비워서, delete() 시점엔 깨끗한 상태로 다시 조회되게 함
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/v1/admin/order/" + orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/order/search")
                        .param("keyword", "delete-test@example.com"))
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void 이메일_키워드로_주문을_검색할_수_있다() throws Exception {
        createOrder("search-target@example.com");
        createOrder("someone-else@example.com");

        mockMvc.perform(get("/api/v1/admin/order/search")
                        .param("keyword", "search-target"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].email").value("search-target@example.com"));
    }

    @Test
    void 기간으로_주문을_검색할_수_있다() throws Exception {
        createOrder("date-range@example.com");
        LocalDate today = LocalDate.now();

        // 오늘 하루로 검색 -> 방금 만든 주문이 걸려야 함
        mockMvc.perform(get("/api/v1/admin/order/search")
                        .param("startDate", today.toString())
                        .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        // 한참 미래 날짜로 검색 -> 아무것도 안 걸려야 함
        LocalDate future = today.plusYears(1);
        mockMvc.perform(get("/api/v1/admin/order/search")
                        .param("startDate", future.toString())
                        .param("endDate", future.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void 검색조건_없이_호출하면_전체_주문이_나온다() throws Exception {
        createOrder("no-filter-1@example.com");
        createOrder("no-filter-2@example.com");

        mockMvc.perform(get("/api/v1/admin/order/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }
}
