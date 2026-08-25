package com.loadbalancing.kiosk.domain.order.controller;

import com.loadbalancing.kiosk.domain.order.service.OrderService;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {

}
