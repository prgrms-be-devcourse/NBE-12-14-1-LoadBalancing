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

        mockMvc.perform(post("/api/v1/auth/order")
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

        String firstBody = mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest(email, List.of(item(1, 1), item(2, 1))))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondBody = mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest(email, List.of(item(3, 1))))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long firstOrderId = objectMapper.readTree(firstBody).path("data").path("orderId").asLong();
        Long secondOrderId = objectMapper.readTree(secondBody).path("data").path("orderId").asLong();

        assertEquals(firstOrderId, secondOrderId);

        // 주문은 1건인데, 그 안의 아이템은 3종류(1+1+1) 다 들어있어야 함
        mockMvc.perform(get("/api/v1/auth/order/list").param("email", email))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].items.length()").value(3));
    }

    @Test
    void 다른_이메일로_주문하면_각각_별도_주문으로_생성된다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("a@example.com", List.of(item(1, 1))))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("b@example.com", List.of(item(1, 1))))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/auth/order/list").param("email", "a@example.com"))
                .andExpect(jsonPath("$.data.content.length()").value(1));
        mockMvc.perform(get("/api/v1/auth/order/list").param("email", "b@example.com"))
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void 재고보다_많은_수량을_주문하면_409가_난다() throws Exception {
        // 케냐 AA(id=3) 초기 재고 30
        var request = orderRequest("insufficient@example.com", List.of(item(3, 999)));

        mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void 존재하지_않는_상품으로_주문하면_404가_난다() throws Exception {
        var request = orderRequest("noproduct@example.com", List.of(item(9999, 1)));

        mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 이메일이_비어있으면_400이_난다() throws Exception {
        var request = orderRequest("", List.of(item(1, 1)));

        mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 이메일_형식이_아니면_400이_난다() throws Exception {
        var request = orderRequest("not-an-email", List.of(item(1, 1)));

        mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 우편번호가_숫자_5자리가_아니면_400이_난다() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "bad-postal@example.com",
                "addressLine1", "서울시 강남구",
                "addressLine2", "101동 202호",
                "postalCode", "abc12", // 숫자 5자리가 아님
                "items", List.of(item(1, 1))
        );

        mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 상품을_하나도_안_담으면_400이_난다() throws Exception {
        var request = orderRequest("empty-items@example.com", List.of());

        mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 수량이_0이하이면_400이_난다() throws Exception {
        var request = orderRequest("zero-quantity@example.com", List.of(item(1, 0)));

        mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 주문한적_없는_이메일로_조회하면_빈_목록이_나온다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/order/list").param("email", "nobody@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    // 예전엔 여기서 관리자용 전체 목록(/order/admin/list)도 테스트했는데, 그 엔드포인트 자체가
    // AdminOrderController.searchOrder()(파라미터 없이 호출하면 전체 목록과 동일)로 흡수되면서 삭제됨.
    // 동일한 케이스는 AdminOrderApiTest.검색조건_없이_호출하면_전체_주문이_나온다()가 커버함.

    @Test
    void 주문_상세조회하면_주문항목이_상세정보와_함께_나온다() throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("detail-test@example.com", List.of(item(1, 2))))))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).path("data").path("orderId").asLong();

        mockMvc.perform(get("/api/v1/auth/order/detail/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("detail-test@example.com"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].itemId").isNotEmpty()) // 항목 삭제 API를 부르려면 필요한 값
                .andExpect(jsonPath("$.data.items[0].title").value("에티오피아 예가체프"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    void 주문항목을_하나_삭제하면_주문은_남고_그_항목만_빠진다() throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("item-delete@example.com",
                                        List.of(item(1, 1), item(2, 1))))))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).path("data").path("orderId").asLong();
        Long firstItemId = objectMapper.readTree(body)
                .path("data").path("items").get(0).path("itemId").asLong();

        mockMvc.perform(delete("/api/v1/auth/order/" + orderId + "/items/" + firstItemId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auth/order/detail/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1)); // 2개 중 1개만 남음
    }

    @Test
    void 주문의_마지막_항목을_삭제하면_주문_자체도_같이_삭제된다() throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                orderRequest("last-item-delete@example.com", List.of(item(1, 1))))))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).path("data").path("orderId").asLong();
        Long itemId = objectMapper.readTree(body)
                .path("data").path("items").get(0).path("itemId").asLong();

        mockMvc.perform(delete("/api/v1/auth/order/" + orderId + "/items/" + itemId))
                .andExpect(status().isOk());

        // 주문에 항목이 하나도 안 남으면 OrderService.deleteOrderItem이 주문 자체도 지움
        mockMvc.perform(get("/api/v1/auth/order/detail/" + orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void 존재하지_않는_주문항목을_삭제하면_404가_난다() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/order/1/items/999999"))
                .andExpect(status().isNotFound());
    }
}
