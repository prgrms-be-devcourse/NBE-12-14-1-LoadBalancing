package com.loadbalancing.kiosk.domain.notification.service;

import com.loadbalancing.kiosk.domain.notification.dto.NotificationResponse;
import com.loadbalancing.kiosk.domain.notification.infra.entity.Notification;
import com.loadbalancing.kiosk.domain.notification.infra.entity.NotificationRead;
import com.loadbalancing.kiosk.domain.notification.infra.repository.NotificationReadRepository;
import com.loadbalancing.kiosk.domain.notification.infra.repository.NotificationRepository;
import com.loadbalancing.kiosk.domain.order.infra.entity.Order;
import com.loadbalancing.kiosk.domain.order.infra.repository.OrderRepository;
import com.loadbalancing.kiosk.global.exception.custom.NotificationNotFoundException;
import com.loadbalancing.kiosk.global.exception.custom.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final OrderRepository orderRepository;

    // 주문 완료 이벤트가 발생할 때마다 알림 기록을 하나 저장한다.
    // (기존엔 웹소켓으로 쏘고 끝이었는데, 이제 여기서 먼저 저장하고 그 id를 웹소켓 페이로드에 실어보냄)
    //
    // 이 메서드는 OrderNotificationEventListener의 @TransactionalEventListener(phase = AFTER_COMMIT)
    // 콜백 안에서 호출된다. 그 시점엔 원래 주문 트랜잭션이 이미 커밋 처리 중이라, 기본 REQUIRED
    // 전파로는 "TransactionRequiredException: No active transaction"이 나는 걸 실제로 겪었음
    // (AFTER_COMMIT 콜백 실행 시점의 트랜잭션 동기화 상태가 애매하게 걸쳐있어서 생기는, 잘 알려진 함정).
    // REQUIRES_NEW로 무조건 새 트랜잭션을 강제로 열게 하면 해결됨.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification create(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Notification notification = Notification.builder()
                .order(order)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse.NotificationInfo> getList(String adminId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<Long> notificationIds = page.getContent().stream()
                .map(Notification::getId)
                .toList();

        // 페이지에 담긴 알림들의 읽음 기록을 한 번에 조회해서 notificationId별로 묶어둠 (N+1 방지)
        Map<Long, List<NotificationRead>> readsByNotificationId =
                notificationReadRepository.findAllByNotification_IdIn(notificationIds).stream()
                        .collect(Collectors.groupingBy(r -> r.getNotification().getId()));

        return page.map(n -> NotificationResponse.NotificationInfo.from(
                n,
                readsByNotificationId.getOrDefault(n.getId(), List.of()),
                adminId
        ));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String adminId) {
        // 각 admin은 알림 하나당 읽음 기록을 최대 1개만 남기므로(markRead에서 중복 방지),
        // "전체 개수 - 이 admin이 읽은 개수"가 곧 안 읽은 개수와 정확히 일치한다.
        long total = notificationRepository.count();
        long readByMe = notificationReadRepository.countByAdminId(adminId);
        return total - readByMe;
    }

    @Transactional
    public void markRead(Long notificationId, String adminId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new NotificationNotFoundException(notificationId);
        }
        saveReadIfAbsent(notificationId, adminId);
    }

    @Transactional
    public void markAllRead(String adminId) {
        // 지금 규모(카페 한 곳 분량)에서는 전체를 훑어도 부담 없어서 간단하게 구현.
        // 데이터가 많이 쌓이면 "최근 N개만" 또는 배치로 바꿔야 함 - 지금은 범위 밖으로 남겨둠.
        List<Long> allIds = notificationRepository.findAll().stream()
                .map(Notification::getId)
                .toList();

        for (Long id : allIds) {
            saveReadIfAbsent(id, adminId);
        }
    }

    private void saveReadIfAbsent(Long notificationId, String adminId) {
        if (notificationReadRepository.existsByNotification_IdAndAdminId(notificationId, adminId)) {
            return; // 이미 읽음 처리돼 있으면 중복 저장 안 함 (읽은 시각이 최초 시점 그대로 유지됨)
        }

        Notification notification = notificationRepository.getReferenceById(notificationId);
        NotificationRead read = NotificationRead.builder()
                .notification(notification)
                .adminId(adminId)
                .readAt(LocalDateTime.now())
                .build();

        notificationReadRepository.save(read);
    }
}
