package com.loadbalancing.kiosk.domain.notification.listener;

import com.loadbalancing.kiosk.domain.notification.dto.OrderCompletedEvent;
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

class OrderNotificationEventListenerTest {

    @Test
    void 주문완료_이벤트를_관리자_토픽으로_전송한다() {
        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);

        OrderNotificationEventListener listener =
                new OrderNotificationEventListener(
                        messagingTemplate
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

        ArgumentCaptor<OrderResponse.OrderInfo> captor =
                ArgumentCaptor.forClass(
                        OrderResponse.OrderInfo.class
                );

        verify(messagingTemplate).convertAndSend(
                eq("/topic/admin/orders"),
                captor.capture()
        );

        OrderResponse.OrderInfo response =
                captor.getValue();

        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.email())
                .isEqualTo("customer@example.com");
        assertThat(response.status()).isEqualTo("주문접수");
    }
}