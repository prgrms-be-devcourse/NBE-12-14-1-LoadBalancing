package com.loadbalancing.kiosk.domain.order.controller;

import com.loadbalancing.kiosk.domain.order.dto.OrderRequest;
import com.loadbalancing.kiosk.domain.order.dto.OrderResponse;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.order.service.OrderService;
import com.loadbalancing.kiosk.global.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequestMapping("/api/v1/admin/order")
@RequiredArgsConstructor
@RestController
public class AdminOrderController {

    private final OrderService orderService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse.OrderStatus>> updateStatusOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest.AdminOrderRequest adminOrderRequest
    ) {

        OrderResponse.OrderStatus response = orderService.updateStatus(id, adminOrderRequest.status());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(200, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteOrder(@PathVariable Long id) {

        orderService.deleteOrder(id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContentSuccess());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<OrderResponse.OrderInfo>>> searchOrder(
            @Parameter(description = "검색어 (이메일 기준)", example = "user02@naver.com")
            @RequestParam(required = false) String keyword,
            @Parameter(
                    description = "주문상태 (ORDER_RECEIVED=주문접수, PAYMENT_COMPLETED=결제완료, IN_DELIVERY=배송중, DELIVERED=배송완료, CANCELLED=주문취소)"
            )
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "검색 시작일 (yyyy-MM-dd 형식)", example = "2026-08-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "검색 종료일 (yyyy-MM-dd 형식)", example = "2026-08-27")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        Page<OrderResponse.OrderInfo> result = orderService.search(keyword, status, start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, result));
    }
}