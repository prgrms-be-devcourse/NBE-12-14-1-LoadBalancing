package com.loadbalancing.kiosk.domain.notification.event;

import java.time.LocalDateTime;
/*
    주문 완료를 알림 영역에 전달하는 이벤트
*/
public record OrderCompletedEvent(
        Long orderId,
        String email,
        LocalDateTime orderedAt
) {
/*
    주문 id와 주문자 이메일을 통해 주문 완료 이벤트 생성
    이벤트가 발생한 시간 기록
 */
    public static OrderCompletedEvent of(
            Long orderId,
            String email
    ) {
        return new OrderCompletedEvent(
                orderId,
                email,
                LocalDateTime.now()
        );
    }
}