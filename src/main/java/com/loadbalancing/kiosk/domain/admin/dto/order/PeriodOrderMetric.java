package com.loadbalancing.kiosk.domain.admin.dto.order;

import lombok.Builder;

@Builder
public record PeriodOrderMetric(
        long totalSales,
        long orderCount,
        long averageOrderAmount
) {
}