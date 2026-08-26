package com.loadbalancing.kiosk.domain.order.order.controller;

import com.loadbalancing.kiosk.domain.order.order.dto.request.OrderCreateRequest;
import com.loadbalancing.kiosk.domain.order.order.dto.response.OrderCreateResponse;
import com.loadbalancing.kiosk.domain.order.order.service.OrderService;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
}