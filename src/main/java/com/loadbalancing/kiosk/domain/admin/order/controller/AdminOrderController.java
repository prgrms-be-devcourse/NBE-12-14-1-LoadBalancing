package com.loadbalancing.kiosk.domain.admin.order.controller;

import com.loadbalancing.kiosk.domain.admin.order.dto.AdminOrderRequest;
import com.loadbalancing.kiosk.domain.admin.order.dto.AdminOrderResponse;
import com.loadbalancing.kiosk.domain.admin.order.service.AdminOrderService;
import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                .status(HttpStatus.OK)
                .body(ApiResponse.success(200, ""));
    }
}