package com.loadbalancing.kiosk.domain.admin.service;

import com.loadbalancing.kiosk.domain.admin.dto.AdminDashboardResponse;
import com.loadbalancing.kiosk.domain.admin.dto.order.OrderDashboardMetrics;
import com.loadbalancing.kiosk.domain.admin.dto.order.OrderStatusCountResponse;
import com.loadbalancing.kiosk.domain.admin.dto.order.PeriodOrderMetric;
import com.loadbalancing.kiosk.domain.admin.dto.sales.ProductSalesAnalysisResponse;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.order.infra.repository.OrderItemRepository;
import com.loadbalancing.kiosk.domain.order.infra.repository.OrderRepository;
import com.loadbalancing.kiosk.domain.product.dto.ProductResponse;
import com.loadbalancing.kiosk.domain.product.infra.entity.Product;
import com.loadbalancing.kiosk.domain.product.infra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

//todo 나중에 하나의 AdminService로 병합예정
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {

        List<Product> products = productRepository.findAll();

        long totalProductCount = products.size();

        List<ProductResponse.ProductInfo> outOfStockProducts = products.stream()
                .filter(product -> product.getStock() == 0)
                .map(ProductResponse.ProductInfo::from)
                .toList();

        List<ProductResponse.ProductInfo> lowStockProducts = products.stream()
                .filter(product ->
                        product.getStock() >= 1
                                && product.getStock() <= 10
                )
                .map(ProductResponse.ProductInfo::from)
                .toList();

        List<ProductResponse.ProductInfo> recentProducts =
                productRepository.findTop5ByOrderByCreatedAtDesc()
                        .stream()
                        .map(ProductResponse.ProductInfo::from)
                        .toList();

        OrderDashboardMetrics orderMetrics =
                getOrderDashboardMetrics();

        ProductSalesAnalysisResponse bestSellingProduct =
                getBestSellingProduct();

        ProductSalesAnalysisResponse mostPurchasedAtOnceProduct =
                getMostPurchasedAtOnceProduct();

        ProductSalesAnalysisResponse worstSellingProduct =
                getWorstSellingProduct();

        return AdminDashboardResponse.builder()
                // 상품/재고
                .totalProductCount(totalProductCount)
                .outOfStockProducts(outOfStockProducts)
                .lowStockProducts(lowStockProducts)
                .recentProducts(recentProducts)

                // 매출
                .dailyTotalSales(orderMetrics.daily().totalSales())
                .weeklyTotalSales(orderMetrics.weekly().totalSales())
                .monthlyTotalSales(orderMetrics.monthly().totalSales())

                // 주문건수
                .dailyOrderCount(orderMetrics.daily().orderCount())
                .weeklyOrderCount(orderMetrics.weekly().orderCount())
                .monthlyOrderCount(orderMetrics.monthly().orderCount())

                // 평균 주문금액
                .dailyAverageOrderAmount(orderMetrics.daily().averageOrderAmount())
                .weeklyAverageOrderAmount(orderMetrics.weekly().averageOrderAmount())
                .monthlyAverageOrderAmount(orderMetrics.monthly().averageOrderAmount())

                // 주문 상태별 개수
                .orderStatusCounts(orderMetrics.orderStatusCounts())

                // 판매 분석
                .bestSellingProduct(bestSellingProduct)
                .mostPurchasedAtOnceProduct(mostPurchasedAtOnceProduct)
                .worstSellingProduct(worstSellingProduct)
                .build();
    }

    private ProductSalesAnalysisResponse getBestSellingProduct() {

        Pageable limitOne = PageRequest.of(0, 1);

        return orderItemRepository.findBestSellingProduct(
                        OrderStatus.CANCELLED,
                        limitOne
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private ProductSalesAnalysisResponse getMostPurchasedAtOnceProduct() {

        Pageable limitOne = PageRequest.of(0, 1);

        return orderItemRepository.findMostPurchasedAtOnceProduct(
                        OrderStatus.CANCELLED,
                        limitOne
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private ProductSalesAnalysisResponse getWorstSellingProduct() {

        Pageable limitOne = PageRequest.of(0, 1);

        return productRepository.findWorstSellingProduct(
                        OrderStatus.CANCELLED,
                        limitOne
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private OrderDashboardMetrics getOrderDashboardMetrics() {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime dailyStart = calculateDailyStart(now);
        LocalDateTime dailyEnd = dailyStart.plusDays(1);

        LocalDateTime weeklyStart = calculateWeeklyStart(now);
        LocalDateTime weeklyEnd = weeklyStart.plusWeeks(1);

        LocalDateTime monthlyStart = calculateMonthlyStart(now);
        LocalDateTime monthlyEnd = monthlyStart.plusMonths(1);

        PeriodOrderMetric daily =
                getPeriodOrderMetric(dailyStart, dailyEnd);

        PeriodOrderMetric weekly =
                getPeriodOrderMetric(weeklyStart, weeklyEnd);

        PeriodOrderMetric monthly =
                getPeriodOrderMetric(monthlyStart, monthlyEnd);

        List<OrderStatusCountResponse> orderStatusCounts =
                orderRepository.countGroupByOrderStatus()
                        .stream()
                        .map(result -> OrderStatusCountResponse.of(
                                result.getStatus(),
                                result.getCount()
                        ))
                        .toList();

        return OrderDashboardMetrics.builder()
                .daily(daily)
                .weekly(weekly)
                .monthly(monthly)
                .orderStatusCounts(orderStatusCounts)
                .build();
    }

    private PeriodOrderMetric getPeriodOrderMetric(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        long totalSales =
                orderItemRepository.sumSalesBetween(
                        startAt,
                        endAt,
                        OrderStatus.CANCELLED
                );

        long orderCount =
                orderRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndOrderStatusNot(
                        startAt,
                        endAt,
                        OrderStatus.CANCELLED
                );

        long averageOrderAmount = orderCount == 0
                ? 0
                : totalSales / orderCount;

        return PeriodOrderMetric.builder()
                .totalSales(totalSales)
                .orderCount(orderCount)
                .averageOrderAmount(averageOrderAmount)
                .build();
    }

    private LocalDateTime calculateDailyStart(LocalDateTime now) {

        LocalDateTime today2pm =
                now.toLocalDate().atTime(14, 0);

        if (now.isBefore(today2pm)) {
            return today2pm.minusDays(1);
        }

        return today2pm;
    }

    private LocalDateTime calculateWeeklyStart(LocalDateTime now) {

        LocalDateTime dailyStart =
                calculateDailyStart(now);

        return dailyStart
                .toLocalDate()
                .with(DayOfWeek.MONDAY)
                .atTime(14, 0);
    }

    private LocalDateTime calculateMonthlyStart(LocalDateTime now) {

        LocalDateTime dailyStart =
                calculateDailyStart(now);

        return dailyStart
                .toLocalDate()
                .withDayOfMonth(1)
                .atTime(14, 0);
    }
}
