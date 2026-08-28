package com.loadbalancing.kiosk.domain.admin.dto;
import com.loadbalancing.kiosk.domain.admin.dto.order.OrderStatusCountResponse;
import com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse;
import com.loadbalancing.kiosk.domain.product.dto.response.ProductResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record AdminDashboardResponse(
        // 상품/재고
        long totalProductCount,
        List<ProductResponse.ProductInfo> outOfStockProducts,
        List<ProductResponse.ProductInfo> lowStockProducts,
        List<ProductResponse.ProductInfo> recentProducts,

        // 매출
        long dailyTotalSales,
        long weeklyTotalSales,
        long monthlyTotalSales,

        // 주문건수
        long dailyOrderCount,
        long weeklyOrderCount,
        long monthlyOrderCount,

        // 평균 주문금액
        long dailyAverageOrderAmount,
        long weeklyAverageOrderAmount,
        long monthlyAverageOrderAmount,

        // 주문 상태별 개수
        List<OrderStatusCountResponse> orderStatusCounts
) {
}