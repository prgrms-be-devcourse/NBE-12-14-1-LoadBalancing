package com.loadbalancing.kiosk.domain.order.controller;

import com.loadbalancing.kiosk.domain.order.dto.OrderCreateRequest;
import com.loadbalancing.kiosk.domain.order.dto.OrderCreateResponse;
import com.loadbalancing.kiosk.domain.order.service.OrderService;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @RequestBody OrderCreateRequest request
            ){
        OrderCreateResponse response = orderService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,response));
    }

}
