package com.loadbalancing.kiosk.domain.admin.order.controller;

import com.loadbalancing.kiosk.domain.admin.order.dto.request.AdminOrderRequest;
import com.loadbalancing.kiosk.domain.admin.order.dto.response.AdminOrderListResponse;
import com.loadbalancing.kiosk.domain.admin.order.dto.response.AdminOrderResponse;
import com.loadbalancing.kiosk.domain.admin.order.service.AdminOrderService;
import com.loadbalancing.kiosk.domain.product.dto.response.ProductResponse;
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

@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@RestController
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @PatchMapping("/order/status/{id}")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> updateStatusOrder(
            @PathVariable Long id,
            @Valid @RequestBody AdminOrderRequest adminOrderRequest
    ) {

        AdminOrderResponse response = adminOrderService.updateStatus(id, adminOrderRequest.status());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(200, response));
    }

    @DeleteMapping("/order/{id}")
    public ResponseEntity<ApiResponse<?>> deleteOrder(@PathVariable Long id) {

        adminOrderService.delete(id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContentSuccess());
    }

    @GetMapping("/order/search")
    public ResponseEntity<ApiResponse<Page<AdminOrderListResponse>>> searchOrder(
            @Parameter(description = "검색어 (주문에 담긴 상품명 기준)", example = "케냐")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 시작일 (yyyy-MM-dd 형식)", example = "2026-08-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "검색 종료일 (yyyy-MM-dd 형식)", example = "2026-08-27")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        Page<AdminOrderListResponse> result = adminOrderService.search(keyword, start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, result));
    }
}