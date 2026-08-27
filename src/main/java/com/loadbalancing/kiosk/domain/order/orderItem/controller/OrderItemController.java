package com.loadbalancing.kiosk.domain.order.orderItem.controller;

import com.loadbalancing.kiosk.domain.order.orderItem.service.OrderItemService;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orderItem")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteOrderItem(
            @PathVariable Long id
    ) {
        orderItemService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.noContentSuccess()
        );
    }
}
