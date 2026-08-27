package com.loadbalancing.kiosk.domain.admin.service;

import com.loadbalancing.kiosk.domain.admin.dto.AdminDashboardResponse;
import com.loadbalancing.kiosk.domain.admin.dto.product.ProductStockResponse;
import com.loadbalancing.kiosk.domain.admin.dto.product.ProductSummaryResponse;
import com.loadbalancing.kiosk.domain.admin.dto.product.RecentProductResponse;
import com.loadbalancing.kiosk.domain.order.order.repository.OrderRepository;
import com.loadbalancing.kiosk.domain.order.orderItem.repository.OrderItemRepository;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        ProductSummaryResponse productSummary =
                getProductSummary();

        return AdminDashboardResponse.builder()
                .productSummary(productSummary)
                .build();
    }

    // 상품/재고용 메서드
    private ProductSummaryResponse getProductSummary() {

        List<Product> products = productRepository.findAll();

        // 전체 상품 수
        long totalProductCount = products.size();

        // 품절 상품 리스트
        List<ProductStockResponse> outOfStockProducts = products.stream()
                .filter(product -> product.getStock() == 0)
                .map(ProductStockResponse::from)
                .toList();

        // 재고 부족 상품 리스트(1~10으로 범위를 잡음)
        List<ProductStockResponse> lowStockProducts = products.stream()
                .filter(product ->
                        product.getStock() >= 1
                                && product.getStock() <= 10
                )
                .map(ProductStockResponse::from)
                .toList();

        // 최근에 등록된 상품 5개 리스트(createdAt을 기준으로 최신 순 정렬함)
        List<RecentProductResponse> recentProducts =
                productRepository.findTop5ByOrderByCreatedAtDesc()
                        .stream()
                        .map(RecentProductResponse::from)
                        .toList();

        // 상품 별 재고 현황
        List<ProductStockResponse> stockStatus =
                products.stream()
                        .map(ProductStockResponse::from)
                        .toList();

        return ProductSummaryResponse.builder()
                .totalProductCount(totalProductCount)
                .outOfStockProducts(outOfStockProducts)
                .lowStockProducts(lowStockProducts)
                .recentProducts(recentProducts)
                .stockStatus(stockStatus)
                .build();
    }
}
