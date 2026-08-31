package com.loadbalancing.kiosk.domain.order.controller;

import com.loadbalancing.kiosk.domain.order.dto.OrderRequest;
import com.loadbalancing.kiosk.domain.order.dto.OrderResponse;
import com.loadbalancing.kiosk.domain.order.service.OrderService;
import com.loadbalancing.kiosk.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse.OrderInfo>> createOrder(@Valid @RequestBody OrderRequest.OrderCreate request) {

        OrderResponse.OrderInfo response = orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, response));
    }

    //해당 email로 db를 조회하여, 최신 주문 순으로 반환.
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<OrderResponse.OrderInfo>>> getEmailOrders(
            @RequestParam String email,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OrderResponse.OrderInfo> orders = orderService.getEmailOrderList(email, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(200, orders)
        );
    }

    @GetMapping("/detail/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse.OrderInfo>> getOrderDetail(@PathVariable Long orderId) {

        OrderResponse.OrderInfo order = orderService.getOrderDetail(orderId);

        return ResponseEntity.ok(
                ApiResponse.success(200, order)
        );
    }
    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<ApiResponse<?>> deleteOrderItem(
        @PathVariable Long orderId, @PathVariable Long itemId
    ) {
        orderService.deleteOrderItem(orderId, itemId);

        return ResponseEntity.ok(
            ApiResponse.noContentSuccess()
        );
    }
}