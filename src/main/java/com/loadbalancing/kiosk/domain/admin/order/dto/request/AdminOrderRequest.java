package com.loadbalancing.kiosk.domain.admin.order.dto.request;

import com.loadbalancing.kiosk.domain.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record AdminOrderRequest(
        @NotNull OrderStatus status
) {
}