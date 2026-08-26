package com.loadbalancing.kiosk.domain.order.order.controller;

import com.loadbalancing.kiosk.domain.order.order.dto.request.OrderCreateRequest;
import com.loadbalancing.kiosk.domain.order.order.dto.response.OrderCreateResponse;
import com.loadbalancing.kiosk.domain.order.order.dto.response.OrderDetailResponse;
import com.loadbalancing.kiosk.domain.order.order.dto.response.OrderListResponse;
import com.loadbalancing.kiosk.domain.order.order.service.OrderService;
import com.loadbalancing.kiosk.domain.product.dto.response.ProductResponse;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> create(
            @RequestBody OrderCreateRequest request
    ) {

        OrderCreateResponse response =
                orderService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, response));
    }
    //해당 email로 db를 조회하여, 최신 주문 순으로 반환.
    //todo OrderStatus별로 주문 나눠서 보여주기.
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getOrderList(
            @RequestParam String email,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Page<OrderListResponse> orders =
                orderService.list(email, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(200, orders)
        );
    }

    @GetMapping("/detail/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> detail(
            @PathVariable Long orderId
    ) {

        OrderDetailResponse order =
                orderService.detail(orderId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        order
                )
        );
    }
}