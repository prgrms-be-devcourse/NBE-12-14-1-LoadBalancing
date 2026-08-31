package com.loadbalancing.kiosk.domain.notification.dto;

import com.loadbalancing.kiosk.domain.notification.infra.entity.Notification;
import com.loadbalancing.kiosk.domain.notification.infra.entity.NotificationRead;
import com.loadbalancing.kiosk.domain.order.dto.OrderResponse;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationResponse {

    @Builder
    public record NotificationInfo(
            Long id,
            Long orderId,
            String email,
            String orderStatus, // 알림 시점이 아니라 "지금" 주문 상태 (읽고 방치했는지 판단용)
            LocalDateTime createdAt,
            boolean readByMe,
            List<ReadRecord> readRecords // 누가 언제 읽었는지 전체 기록
    ) {
        public static NotificationInfo from(
                Notification notification,
                List<NotificationRead> reads,
                String currentAdminId
        ) {
            List<ReadRecord> records = reads.stream()
                    .map(ReadRecord::from)
                    .toList();

            boolean readByMe = reads.stream()
                    .anyMatch(r -> r.getAdminId().equals(currentAdminId));

            return NotificationInfo.builder()
                    .id(notification.getId())
                    .orderId(notification.getOrder().getId())
                    .email(notification.getOrder().getEmail())
                    .orderStatus(notification.getOrder().getOrderStatus().getDescription())
                    .createdAt(notification.getCreatedAt())
                    .readByMe(readByMe)
                    .readRecords(records)
                    .build();
        }
    }

    @Builder
    public record ReadRecord(
            String adminId,
            LocalDateTime readAt
    ) {
        public static ReadRecord from(NotificationRead read) {
            return ReadRecord.builder()
                    .adminId(read.getAdminId())
                    .readAt(read.getReadAt())
                    .build();
        }
    }

    // 웹소켓으로 실시간 push할 때 쓰는 페이로드. REST 히스토리 조회 결과와 같은 id 체계를 쓰기 위해
    // 저장 직후 발급된 notificationId를 같이 실어 보낸다.
    @Builder
    public record NotificationPush(
            Long notificationId,
            OrderResponse.OrderInfo order
    ) {
    }
}
