package com.loadbalancing.kiosk.domain.notification.listener;

import com.loadbalancing.kiosk.domain.notification.dto.NotificationResponse;
import com.loadbalancing.kiosk.domain.notification.infra.entity.Notification;
import com.loadbalancing.kiosk.domain.notification.service.NotificationService;
import com.loadbalancing.kiosk.global.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 주문 완료 이벤트를 1) DB에 알림 기록으로 저장하고 2) 관리자 WebSocket Topic으로 실시간 전달.
// 저장을 먼저 해야 관리자가 나중에(접속 안 하고 있던 동안 놓친 것까지) REST로 히스토리를 조회할 수 있다.
// 웹소켓은 그중 "지금 이 순간 연결돼 있는 사람"에게 새로고침 없이 바로 보여주는 역할만 함.
//
// 저장(DB)과 실시간 전송(웹소켓)을 일부러 분리해서 처리한다 - 저장이 실패해도 실시간 알림 자체는
// 계속 나가야 하기 때문(하나가 죽었다고 나머지까지 같이 죽으면 안 됨. DiscordOrderNotificationListener랑
// 같은 이유로 try-catch를 씀).
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationEventListener {

    private static final String ORDER_NOTIFICATION_DESTINATION =
            "/topic/admin/orders";

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    // 주문 트랜잭션이 정상적으로 커밋되면 알림을 저장하고, 그 id를 실어서 전송
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderCompleted(
            OrderCompletedEvent event
    ) {
        // 이 로그가 아예 안 찍히면 리스너 자체가 호출이 안 되고 있다는 뜻(이벤트 발행/구독 단계 문제).
        // 이 로그는 찍히는데 아래 저장 실패 로그만 없으면 저장은 잘 된 것 - 두 로그로 어디서 끊기는지 구분함
        log.info("주문완료 이벤트 수신. orderId={}", event.orderInfo().orderId());

        Long notificationId = null;
        try {
            Notification saved = notificationService.create(event.orderInfo().orderId());
            notificationId = saved.getId();
            log.info("알림 저장 성공. notificationId={}, orderId={}", notificationId, event.orderInfo().orderId());
        } catch (Exception exception) {
            log.error(
                    "알림 저장에 실패했습니다. 실시간 웹소켓 전송은 계속 진행합니다. orderId={}",
                    event.orderInfo().orderId(),
                    exception
            );
        }

        NotificationResponse.NotificationPush push = NotificationResponse.NotificationPush.builder()
                .notificationId(notificationId) // 저장 실패 시 null - 프론트는 null이면 읽음처리 대상에서 제외
                .order(event.orderInfo())
                .build();

        messagingTemplate.convertAndSend(
                ORDER_NOTIFICATION_DESTINATION,
                push
        );
    }
}