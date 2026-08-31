package com.loadbalancing.kiosk.domain.notification;

import com.loadbalancing.kiosk.domain.notification.infra.repository.NotificationReadRepository;
import com.loadbalancing.kiosk.domain.notification.infra.repository.NotificationRepository;
import com.loadbalancing.kiosk.domain.notification.service.NotificationService;
import com.loadbalancing.kiosk.domain.order.infra.entity.Order;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.order.infra.repository.OrderRepository;
import com.loadbalancing.kiosk.global.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// /api/v1/admin/notifications API 테스트.
// 알림 생성은 NotificationService를 직접 호출해서 준비함(이유는 NotificationServiceTest 상단 주석 참고).
// NotificationService.create()가 REQUIRES_NEW라 항상 자기 트랜잭션에서 커밋되기 때문에,
// create()가 볼 Order를 먼저 진짜로 커밋해둬야 하고(TestTransaction 사용), create()로 생긴
// Notification도 테스트 롤백으로 안 지워지니 직접 지워줘야 함 (자세한 이유는 NotificationServiceTest 참고)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    NotificationService notificationService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    NotificationReadRepository notificationReadRepository;

    private final List<Long> createdOrderIds = new ArrayList<>();

    private String adminAuthHeader() {
        return "Bearer " + jwtProvider.generateToken("admin01");
    }

    private Long createOrder(String email) {
        Order order = Order.builder()
                .email(email)
                .addressLine1("서울시 강남구")
                .addressLine2("101동")
                .postalCode("06000")
                .orderStatus(OrderStatus.ORDER_RECEIVED)
                .build();
        Long orderId = orderRepository.save(order).getId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        createdOrderIds.add(orderId);
        return orderId;
    }

    @AfterTransaction
    void cleanUpCommittedRows() {
        notificationReadRepository.deleteAll();
        notificationRepository.deleteAll();
        orderRepository.deleteAllById(createdOrderIds);
    }

    @Test
    void 토큰_없이_알림_목록을_조회하면_401이_난다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 알림_목록을_조회하면_생성한_알림이_보인다() throws Exception {
        Long orderId = createOrder("api-list-test@example.com");
        notificationService.create(orderId);

        mockMvc.perform(get("/api/v1/admin/notifications")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].orderId").value(orderId))
                .andExpect(jsonPath("$.data.content[0].readByMe").value(false));
    }

    @Test
    void 안읽은_개수를_조회할_수_있다() throws Exception {
        Long orderId = createOrder("api-unread-test@example.com");
        notificationService.create(orderId);
        notificationService.create(orderId);

        mockMvc.perform(get("/api/v1/admin/notifications/unread-count")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(2));
    }

    @Test
    void 알림을_읽음_처리하면_목록에서_readByMe가_true로_바뀐다() throws Exception {
        Long orderId = createOrder("api-read-test@example.com");
        Long notificationId = notificationService.create(orderId).getId();

        mockMvc.perform(patch("/api/v1/admin/notifications/" + notificationId + "/read")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/notifications")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader()))
                .andExpect(jsonPath("$.data.content[0].readByMe").value(true));
    }

    @Test
    void 존재하지_않는_알림을_읽음_처리하면_404가_난다() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/notifications/9999/read")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader()))
                .andExpect(status().isNotFound());
    }

    @Test
    void 전체읽음_API를_호출하면_안읽은_개수가_0이_된다() throws Exception {
        Long orderId = createOrder("api-read-all-test@example.com");
        notificationService.create(orderId);
        notificationService.create(orderId);

        mockMvc.perform(patch("/api/v1/admin/notifications/read-all")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/notifications/unread-count")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader()))
                .andExpect(jsonPath("$.data.count").value(0));
    }
}
