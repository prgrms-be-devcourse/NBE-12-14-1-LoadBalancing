package com.loadbalancing.kiosk.domain.notification.listener;

import com.loadbalancing.kiosk.domain.notification.dto.NotificationResponse;
import com.loadbalancing.kiosk.domain.notification.dto.OrderCompletedEvent;
import com.loadbalancing.kiosk.domain.notification.infra.entity.Notification;
import com.loadbalancing.kiosk.domain.notification.service.NotificationService;
import com.loadbalancing.kiosk.domain.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderNotificationEventListenerTest {

    @Test
    void 주문완료_이벤트를_저장하고_그_id와_함께_관리자_토픽으로_전송한다() {
        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);
        NotificationService notificationService =
                mock(NotificationService.class);

        // 저장 결과로 id=100인 Notification이 돌아온다고 가정
        Notification savedNotification = mock(Notification.class);
        when(savedNotification.getId()).thenReturn(100L);
        when(notificationService.create(1L)).thenReturn(savedNotification);

        OrderNotificationEventListener listener =
                new OrderNotificationEventListener(
                        messagingTemplate,
                        notificationService
                );

        OrderResponse.OrderInfo orderInfo =
                OrderResponse.OrderInfo.builder()
                        .orderId(1L)
                        .email("customer@example.com")
                        .addressLine1("서울특별시 강남구")
                        .addressLine2("테스트 101호")
                        .postalCode("06234")
                        .status("주문접수")
                        .createdAt(
                                LocalDateTime.of(
                                        2026, 8, 28, 12, 0
                                )
                        )
                        .items(List.of())
                        .build();

        OrderCompletedEvent event =
                new OrderCompletedEvent(
                        orderInfo,
                        LocalDateTime.of(
                                2026, 8, 28, 12, 0
                        )
                );

        listener.handleOrderCompleted(event);

        ArgumentCaptor<NotificationResponse.NotificationPush> captor =
                ArgumentCaptor.forClass(
                        NotificationResponse.NotificationPush.class
                );

        verify(messagingTemplate).convertAndSend(
                eq("/topic/admin/orders"),
                captor.capture()
        );

        NotificationResponse.NotificationPush push = captor.getValue();

        assertThat(push.notificationId()).isEqualTo(100L);
        assertThat(push.order().orderId()).isEqualTo(1L);
        assertThat(push.order().email())
                .isEqualTo("customer@example.com");
        assertThat(push.order().status()).isEqualTo("주문접수");
    }

    @Test
    void 알림_저장이_실패해도_웹소켓_전송은_그대로_나간다() {
        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);
        NotificationService notificationService =
                mock(NotificationService.class);

        // DB 저장 단계에서 예외가 나는 상황을 재현 (제약조건 위반, 커넥션 문제 등 어떤 이유든)
        when(notificationService.create(1L))
                .thenThrow(new RuntimeException("DB 저장 실패"));

        OrderNotificationEventListener listener =
                new OrderNotificationEventListener(
                        messagingTemplate,
                        notificationService
                );

        OrderResponse.OrderInfo orderInfo =
                OrderResponse.OrderInfo.builder()
                        .orderId(1L)
                        .email("customer@example.com")
                        .addressLine1("서울특별시 강남구")
                        .addressLine2("테스트 101호")
                        .postalCode("06234")
                        .status("주문접수")
                        .createdAt(LocalDateTime.of(2026, 8, 28, 12, 0))
                        .items(List.of())
                        .build();

        OrderCompletedEvent event =
                new OrderCompletedEvent(orderInfo, LocalDateTime.of(2026, 8, 28, 12, 0));

        // 예외가 밖으로 안 새고, 웹소켓 전송까지 정상적으로 이어져야 함
        listener.handleOrderCompleted(event);

        ArgumentCaptor<NotificationResponse.NotificationPush> captor =
                ArgumentCaptor.forClass(NotificationResponse.NotificationPush.class);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/admin/orders"),
                captor.capture()
        );

        // 저장은 실패했으니 notificationId는 null이지만, order 정보 자체는 그대로 실려있어야 함
        assertThat(captor.getValue().notificationId()).isNull();
        assertThat(captor.getValue().order().orderId()).isEqualTo(1L);
    }
}
