package com.loadbalancing.kiosk.domain.order.dto;

import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;


//todo validation으로 검증(email, address postalCode 등)
public class OrderRequest {

    public record OrderCreate(
            String email,
            String addressLine1,
            String addressLine2,
            String postalCode,
            List<OrderItem> items
    ) {}

    public record OrderItem(
            Long productId,
            Long quantity
    ) {}

    public record AdminOrderRequest(
        @NotNull OrderStatus status
    ) {}
}
