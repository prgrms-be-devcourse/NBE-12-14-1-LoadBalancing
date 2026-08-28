package com.loadbalancing.kiosk.domain.notification.listener;

import com.loadbalancing.kiosk.domain.notification.dto.OrderNotificationResponse;
import com.loadbalancing.kiosk.domain.notification.event.OrderCompletedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderNotificationEventListenerTest {

    @Test
    void 주문완료_이벤트를_관리자_토픽으로_전송한다() {
        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);

        OrderNotificationEventListener listener =
                new OrderNotificationEventListener(
                        messagingTemplate
                );

        OrderCompletedEvent event =
                new OrderCompletedEvent(
                        1L,
                        "customer@example.com",
                        LocalDateTime.of(
                                2026, 8, 27, 15, 30
                        )
                );

        listener.handleOrderCompleted(event);

        ArgumentCaptor<OrderNotificationResponse> captor =
                ArgumentCaptor.forClass(
                        OrderNotificationResponse.class
                );

        verify(messagingTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(
                        "/topic/admin/orders"
                ),
                captor.capture()
        );

        OrderNotificationResponse response =
                captor.getValue();

        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.email())
                .isEqualTo("customer@example.com");
        assertThat(response.message())
                .isEqualTo("새로운 주문이 접수되었습니다.");
    }
}