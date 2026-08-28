package com.loadbalancing.kiosk.domain.notification.dto;

import com.loadbalancing.kiosk.domain.order.dto.OrderResponse;

import java.time.LocalDateTime;
/*
    주문 완료를 알림 영역에 전달하는 이벤트
*/
public record OrderCompletedEvent(
        OrderResponse.OrderInfo orderInfo,
        LocalDateTime orderedAt
) {
/*
    orderInfo를 통해 주문 완료 이벤트 생성
    이벤트가 발생한 시간 기록
 */
    public static OrderCompletedEvent of(
            OrderResponse.OrderInfo orderInfo
    ) {
        return new OrderCompletedEvent(
                orderInfo,
                LocalDateTime.now()
        );
    }
}