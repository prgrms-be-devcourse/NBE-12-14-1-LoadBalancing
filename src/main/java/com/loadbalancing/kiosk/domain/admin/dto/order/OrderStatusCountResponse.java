package com.loadbalancing.kiosk.domain.admin.dto.order;

import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import lombok.Builder;

@Builder
public record OrderStatusCountResponse(
        OrderStatus status,
        String description,
        long count
) {
    public static OrderStatusCountResponse of(
            OrderStatus status,
            long count
    ) {
        return OrderStatusCountResponse.builder()
                .status(status)
                .description(status.getDescription())
                .count(count)
                .build();
    }
}