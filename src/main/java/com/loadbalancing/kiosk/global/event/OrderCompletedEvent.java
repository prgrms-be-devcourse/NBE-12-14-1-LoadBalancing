package com.loadbalancing.kiosk.global.event;

import com.loadbalancing.kiosk.domain.order.dto.OrderResponse;

import java.time.LocalDateTime;

/*
    주문 완료를 알림 영역에 전달하는 이벤트.
    order 도메인이 발행하고 notification 도메인이 구독하는, 두 도메인을 가로지르는 이벤트라
    둘 중 한쪽 안에 두면 반대쪽이 그 도메인에 의존하게 돼서 global로 뺌.
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
