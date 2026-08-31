package com.loadbalancing.kiosk.domain.notification.controller;

import com.loadbalancing.kiosk.domain.notification.dto.NotificationResponse;
import com.loadbalancing.kiosk.domain.notification.service.NotificationService;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.Map;

// 관리자 전용(=/api/v1/admin/**라 SecurityConfig에서 로그인 필요). 로그인한 adminId는
// JwtAuthenticationFilter가 SecurityContext에 넣어둔 Authentication에서 꺼내 쓴다.
@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse.NotificationInfo>>> list(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NotificationResponse.NotificationInfo> result =
                notificationService.getList(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(200, result));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(
            Authentication authentication
    ) {
        long count = notificationService.getUnreadCount(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(200, Map.of("count", count)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<?>> read(
            @PathVariable Long id,
            Authentication authentication
    ) {
        notificationService.markRead(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.noContentSuccess());
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<?>> readAll(
            Authentication authentication
    ) {
        notificationService.markAllRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.noContentSuccess());
    }
}
