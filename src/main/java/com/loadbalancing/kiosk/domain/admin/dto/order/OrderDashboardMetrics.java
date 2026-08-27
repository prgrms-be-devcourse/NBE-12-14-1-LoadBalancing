package com.loadbalancing.kiosk.domain.admin.dto.order;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderDashboardMetrics(
        PeriodOrderMetric daily,
        PeriodOrderMetric weekly,
        PeriodOrderMetric monthly,
        List<OrderStatusCountResponse> orderStatusCounts
) {
}