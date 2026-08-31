package com.loadbalancing.kiosk.domain.notification.listener;

import com.loadbalancing.kiosk.global.discord.DiscordWebhookClient;
import com.loadbalancing.kiosk.global.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 주문 완료 이벤트를 디스코드 알림 비동기로 처리
@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordOrderNotificationListener {

    private final DiscordWebhookClient discordWebhookClient;

    // 주문 이후 디스코드 전송 작업을 별도 스레드에서 실행
    // 디스코드 응답 지연이 주문 API 응답 시간에 영향을 미치지 않도록 함
    @Async("notificationExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderCompleted(
            OrderCompletedEvent event
    ) {
        try {
            discordWebhookClient.sendOrderCompleted(event);
        } catch (Exception exception) {
            log.error(
                    "Discord 주문 알림 전송에 실패했습니다. orderId={}",
                    event.orderInfo().orderId(),
                    exception
            );
        }
    }
}