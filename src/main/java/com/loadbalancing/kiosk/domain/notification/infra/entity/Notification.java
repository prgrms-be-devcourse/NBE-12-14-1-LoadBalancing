package com.loadbalancing.kiosk.domain.notification.infra.entity;

import com.loadbalancing.kiosk.domain.order.infra.entity.Order;
import com.loadbalancing.kiosk.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

// 주문 발생 알림 "기록". 주문이 생성될 때마다 하나씩 쌓여서, 관리자가 접속했을 때
// 그동안 놓친 알림까지 전부 조회할 수 있게 해주는 저장소. (웹소켓은 이 저장 이후의 실시간 push만 담당)
@Getter
@Entity
@Builder
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 주문에 대한 알림인지. 상태(status)는 여기 저장하지 않고 조회 시점에 이 연관관계로
    // Order를 다시 읽어서 "현재" 상태를 보여준다 - 알림이 온 이후 상태가 바뀔 수 있어서
    // (읽었는데 처리 안 하고 방치된 걸 드러내는 게 목적이라 스냅샷이 아니라 최신값이어야 함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
