package com.loadbalancing.kiosk.domain.admin.dto;

import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.product.dto.ProductResponse;
import lombok.Builder;

import java.util.List;

public class AdminResponse {

    public record LoginResponse(String token) {}

    // 관리자 대시보드 전체 응답. order/product 도메인 두 곳의 통계를 한데 모아 보여줌
    @Builder
    public record DashboardInfo(
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
            List<OrderStatusCount> orderStatusCounts,

            // 판매 분석 - 주문이 하나도 없으면 null로 옴
            ProductResponse.ProductSalesAnalysisInfo bestSellingProduct,
            ProductResponse.ProductSalesAnalysisInfo mostPurchasedAtOnceProduct,
            ProductResponse.ProductSalesAnalysisInfo worstSellingProduct
    ) {
    }

    // AdminDashboardService 내부에서 일/주/월 매출 계산 과정을 담는 중간 집계 결과
    // (DashboardInfo로 최종 응답을 만들기 전 단계)
    @Builder
    public record OrderMetrics(
            PeriodMetric daily,
            PeriodMetric weekly,
            PeriodMetric monthly,
            List<OrderStatusCount> orderStatusCounts
    ) {
    }

    @Builder
    public record PeriodMetric(
            long totalSales,
            long orderCount,
            long averageOrderAmount
    ) {
    }

    @Builder
    public record OrderStatusCount(
            OrderStatus status,
            String description,
            long count
    ) {
        public static OrderStatusCount of(
                OrderStatus status,
                long count
        ) {
            return OrderStatusCount.builder()
                    .status(status)
                    .description(status.getDescription())
                    .count(count)
                    .build();
        }
    }
}
