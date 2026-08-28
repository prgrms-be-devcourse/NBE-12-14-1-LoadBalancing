package com.loadbalancing.kiosk.domain.notification.dto;

import com.loadbalancing.kiosk.domain.notification.event.OrderCompletedEvent;

import java.time.LocalDateTime;

public record OrderNotificationResponse(
        Long orderId,
        String email,
        String message,
        LocalDateTime orderedAt
) {
    // 주문 완료 이벤트를 관리자 화면 알림 응답으로 변환
    public static OrderNotificationResponse from(
            OrderCompletedEvent event
    ) {
        return new OrderNotificationResponse(
                event.orderId(),
                event.email(),
                "새로운 주문이 접수되었습니다.",
                event.orderedAt()
        );
    }
}