package com.loadbalancing.kiosk.domain.notification.listener;

import com.loadbalancing.kiosk.domain.notification.dto.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 주문 완료 이벤트를 관리자 WebSocket Topic으로 전달
@Component
@RequiredArgsConstructor
public class OrderNotificationEventListener {

    private static final String ORDER_NOTIFICATION_DESTINATION =
            "/topic/admin/orders";

    private final SimpMessagingTemplate messagingTemplate;

    // 주문 트랜잭션이 정상적으로 커밋되면 주문 정보를 전송
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderCompleted(
            OrderCompletedEvent event
    ) {
        messagingTemplate.convertAndSend(
                ORDER_NOTIFICATION_DESTINATION,
                event.orderInfo()
        );
    }
}