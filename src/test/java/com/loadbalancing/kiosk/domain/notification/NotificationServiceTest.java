package com.loadbalancing.kiosk.domain.notification;

import com.loadbalancing.kiosk.domain.notification.dto.NotificationResponse;
import com.loadbalancing.kiosk.domain.notification.infra.entity.Notification;
import com.loadbalancing.kiosk.domain.notification.infra.repository.NotificationReadRepository;
import com.loadbalancing.kiosk.domain.notification.infra.repository.NotificationRepository;
import com.loadbalancing.kiosk.domain.notification.service.NotificationService;
import com.loadbalancing.kiosk.domain.order.infra.entity.Order;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.order.infra.repository.OrderRepository;
import com.loadbalancing.kiosk.global.exception.custom.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// NotificationService 자체를 직접 호출해서 검증하는 테스트.
// (POST /api/v1/auth/order로 주문을 만드는 방식은 안 씀 - @Transactional 테스트는 끝에 롤백만 하고
// 실제 커밋을 안 해서, AFTER_COMMIT 시점에 도는 OrderNotificationEventListener가 테스트 중엔 절대
// 안 불려서 그 경로로는 알림이 안 생김. 그래서 여기서는 서비스 메서드를 직접 호출해서 검증함)
//
// NotificationService.create()는 REQUIRES_NEW라 항상 자기만의 트랜잭션에서 커밋됨(테스트가 끝에
// 롤백하는 것과 무관하게). 그래서 create()가 조회할 Order를 먼저 진짜로 커밋해둬야 하고(안 그러면
// REQUIRES_NEW 쪽 트랜잭션에서는 아직 커밋 안 된 Order가 안 보여서 OrderNotFoundException이 남),
// create()로 생긴 Notification 행도 테스트가 끝나도 롤백 안 되니 직접 지워줘야 함.
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceTest {

    @Autowired
    NotificationService notificationService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    NotificationReadRepository notificationReadRepository;

    private final List<Long> createdOrderIds = new ArrayList<>();

    private Long createOrder(String email) {
        Order order = Order.builder()
                .email(email)
                .addressLine1("서울시 강남구")
                .addressLine2("101동")
                .postalCode("06000")
                .orderStatus(OrderStatus.ORDER_RECEIVED)
                .build();
        Long orderId = orderRepository.save(order).getId();

        // 여기서 실제로 커밋해서, REQUIRES_NEW로 도는 create()의 별도 트랜잭션에서도 이 주문이 보이게 함.
        // TestTransaction.start()로 다시 새 트랜잭션을 열어두면, 그 안에서 하는 나머지 작업(읽음 처리 등)은
        // 여전히 테스트 끝나고 정상적으로 롤백됨 - 딱 이 Order 커밋만 예외적으로 남는 것
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        createdOrderIds.add(orderId);
        return orderId;
    }

    // 일반 @AfterEach는 테스트용 트랜잭션이 아직 안 끝난 채로 실행돼서, 그 안에서 지워봐야
    // 어차피 곧 롤백돼버림(즉 아무것도 안 지워짐). @AfterTransaction은 그 트랜잭션이 끝난
    // "뒤"에 실행되는 스프링 테스트 전용 콜백이라 여기서 지워야 진짜로 지워짐
    @AfterTransaction
    void cleanUpCommittedRows() {
        // create()가 REQUIRES_NEW로 실제 커밋해버린 Notification/NotificationRead는 테스트 롤백으로
        // 안 지워지니 직접 삭제. 이 두 테이블은 이 알림 기능 전용이라 deleteAll 해도 다른 테스트에
        // 영향 없음. Order는 다른 테스트도 쓰는 공용 테이블이라 이 테스트가 만든 것만 콕 집어 지움
        notificationReadRepository.deleteAll();
        notificationRepository.deleteAll();
        orderRepository.deleteAllById(createdOrderIds);
    }

    @Test
    void 존재하는_주문으로_알림을_생성하면_저장된다() {
        Long orderId = createOrder("notify-test@example.com");

        Notification saved = notificationService.create(orderId);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrder().getId()).isEqualTo(orderId);
    }

    @Test
    void 존재하지_않는_주문으로_알림을_생성하면_예외가_난다() {
        assertThatThrownBy(() -> notificationService.create(9999L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void 읽지_않은_상태에서_unreadCount는_생성한_개수만큼이다() {
        Long orderId = createOrder("unread-test@example.com");
        notificationService.create(orderId);
        notificationService.create(orderId);

        long unread = notificationService.getUnreadCount("admin01");

        assertThat(unread).isEqualTo(2);
    }

    @Test
    void 읽음_처리하면_unreadCount가_줄어들고_다시_읽어도_중복되지_않는다() {
        Long orderId = createOrder("read-test@example.com");
        Notification n1 = notificationService.create(orderId);
        notificationService.create(orderId);

        notificationService.markRead(n1.getId(), "admin01");
        assertThat(notificationService.getUnreadCount("admin01")).isEqualTo(1);

        // 같은 admin이 같은 알림을 또 읽음 처리해도 개수는 그대로여야 함(중복 저장 방지)
        notificationService.markRead(n1.getId(), "admin01");
        assertThat(notificationService.getUnreadCount("admin01")).isEqualTo(1);
    }

    @Test
    void 다른_관리자가_읽었어도_나는_안읽음으로_남는다() {
        Long orderId = createOrder("multi-admin-test@example.com");
        Notification n1 = notificationService.create(orderId);

        notificationService.markRead(n1.getId(), "admin01");

        // admin02 입장에서는 아직 안 읽은 상태
        assertThat(notificationService.getUnreadCount("admin02")).isEqualTo(1);

        Page<NotificationResponse.NotificationInfo> page =
                notificationService.getList("admin02", PageRequest.of(0, 10));
        NotificationResponse.NotificationInfo info = page.getContent().stream()
                .filter(i -> i.id().equals(n1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(info.readByMe()).isFalse();
        // 하지만 "누가 읽었는지" 기록에는 admin01이 남아있어야 함 - 방치 여부 추적의 핵심
        assertThat(info.readRecords())
                .extracting(NotificationResponse.ReadRecord::adminId)
                .contains("admin01");
    }

    @Test
    void 전체읽음_처리하면_모든_알림이_읽음으로_바뀐다() {
        Long orderId = createOrder("read-all-test@example.com");
        notificationService.create(orderId);
        notificationService.create(orderId);
        notificationService.create(orderId);

        notificationService.markAllRead("admin01");

        assertThat(notificationService.getUnreadCount("admin01")).isEqualTo(0);
    }

    @Test
    void 알림_목록에는_주문의_현재_상태가_반영된다() {
        Long orderId = createOrder("status-reflect-test@example.com");
        notificationService.create(orderId);

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.updateStatus(OrderStatus.PAYMENT_COMPLETED); // 알림 생성 이후에 주문 상태가 바뀜

        Page<NotificationResponse.NotificationInfo> page =
                notificationService.getList("admin01", PageRequest.of(0, 10));
        NotificationResponse.NotificationInfo info = page.getContent().stream()
                .filter(i -> i.orderId().equals(orderId))
                .findFirst()
                .orElseThrow();

        // 알림 생성 시점 상태(주문접수)가 아니라, 조회 시점의 최신 상태(결제완료)가 나와야 함
        assertThat(info.orderStatus()).isEqualTo("결제완료");
    }
}
