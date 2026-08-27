package com.loadbalancing.kiosk.domain.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 고객용(비로그인) 주문 API 테스트.
// 핵심은 "같은 이메일 + 같은 처리주기(오후 2시 컷오프)면 주문이 하나로 합쳐진다"는 비즈니스 규칙 검증.
// 테스트가 순식간에 끝나서 두 요청이 같은 컷오프 주기 안에 들어가는 걸 전제로 함
// (자정 근처 오후 2시 정각에 걸치면 아주 드물게 flaky할 수 있음 - 그 정도 리스크는 감수)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderApiTest {

    @Autowired
    MockMvc mockMvc;

    // Spring Boot 4 기본 ObjectMapper 빈은 Jackson 3(tools.jackson.*) 타입이라
    // com.fasterxml.jackson(고전 Jackson 2) 타입으로 autowire가 안 됨 - 그냥 직접 생성해서 씀
    ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Object> orderRequest(String email, List<Map<String, Object>> items) {
        return Map.of(
                "email", email,
                "addressLine1", "서울시 강남구",
                "addressLine2", "101동 202호",
                "postalCode", "06000",
                "items", items
        );
    }

    private Map<String, Object> item(long productId, long quantity) {
        return Map.of("productId", productId, "quantity", quantity);
    }

    @Test
    void 주문을_생성하면_상품_재고가_수량만큼_줄어든다() throws Exception {
        // 예가체프(id=1) 초기 재고 50
        var request = orderRequest("stock-test@example.com", List.of(item(1, 3)));

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("주문접수"));

        mockMvc.perform(get("/api/v1/auth/product/detail/1"))
                .andExpect(jsonPath("$.data.stock").value(47)); // 50 - 3
    }

    @Test
    void 같은_이메일로_연달아_주문하면_하나의_주문으로_합쳐진다() throws Exception {
        String email = "merge-test@example.com";

        String firstBody = mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest(email, List.of(item(1, 1), item(2, 1))))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondBody = mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest(email, List.of(item(3, 1))))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long firstOrderId = objectMapper.readTree(firstBody).path("data").path("orderId").asLong();
        Long secondOrderId = objectMapper.readTree(secondBody).path("data").path("orderId").asLong();

        assertEquals(firstOrderId, secondOrderId);

        // 주문은 1건인데, 그 안의 아이템은 3종류(1+1+1) 다 들어있어야 함
        mockMvc.perform(get("/api/v1/order/list").param("email", email))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].items.length()").value(3));
    }

    @Test
    void 다른_이메일로_주문하면_각각_별도_주문으로_생성된다() throws Exception {
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("a@example.com", List.of(item(1, 1))))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("b@example.com", List.of(item(1, 1))))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/order/list").param("email", "a@example.com"))
                .andExpect(jsonPath("$.data.content.length()").value(1));
        mockMvc.perform(get("/api/v1/order/list").param("email", "b@example.com"))
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void 재고보다_많은_수량을_주문하면_409가_난다() throws Exception {
        // 케냐 AA(id=3) 초기 재고 30
        var request = orderRequest("insufficient@example.com", List.of(item(3, 999)));

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void 존재하지_않는_상품으로_주문하면_404가_난다() throws Exception {
        var request = orderRequest("noproduct@example.com", List.of(item(9999, 1)));

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 주문한적_없는_이메일로_조회하면_빈_목록이_나온다() throws Exception {
        mockMvc.perform(get("/api/v1/order/list").param("email", "nobody@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void 관리자용_전체_주문목록은_이메일_필터없이_다_나온다() throws Exception {
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("admin-list-1@example.com", List.of(item(1, 1))))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("admin-list-2@example.com", List.of(item(2, 1))))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/order/admin/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void 주문_상세조회하면_주문항목이_상세정보와_함께_나온다() throws Exception {
        String body = mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("detail-test@example.com", List.of(item(1, 2))))))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).path("data").path("orderId").asLong();

        mockMvc.perform(get("/api/v1/order/detail/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("detail-test@example.com"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("에티오피아 예가체프"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }
}
