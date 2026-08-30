package com.loadbalancing.kiosk.domain.notification.infra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// "누가 언제 이 알림을 읽었는지"의 기록. Notification 하나당 admin 한 명당 최대 1행만 존재함
// (같은 admin이 같은 알림을 두 번 읽어도 중복 저장 안 함 - NotificationService에서 존재 체크 후 저장).
// 이 테이블이 있어야 "봤는데 처리를 안 했다"는 책임소재를 관리자별로 구분할 수 있다.
@Getter
@Entity
@Builder
@Table(
        name = "notification_read",
        uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id", "admin_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    // JWT subject(adminId) 그대로 저장. 관리자 엔티티를 따로 연관관계로 안 잡은 이유는
    // JwtAuthenticationFilter가 인증 정보로 adminId 문자열만 들고 있어서, 굳이 조인 없이
    // 바로 비교할 수 있게 하려는 것.
    @Column(name = "admin_id", nullable = false)
    private String adminId;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}
