package com.loadbalancing.kiosk.domain.notification.infra.repository;

import com.loadbalancing.kiosk.domain.notification.infra.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

    boolean existsByNotification_IdAndAdminId(Long notificationId, String adminId);

    List<NotificationRead> findAllByNotification_Id(Long notificationId);

    // 여러 알림의 읽음 기록을 한 번에 조회할 때 씀 (목록 화면에서 N+1 줄이는 용도)
    List<NotificationRead> findAllByNotification_IdIn(List<Long> notificationIds);

    long countByAdminId(String adminId);
}
