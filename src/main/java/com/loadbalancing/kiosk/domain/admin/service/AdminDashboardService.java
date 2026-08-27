package com.loadbalancing.kiosk.domain.admin.service;

import com.loadbalancing.kiosk.domain.admin.dto.AdminDashboardResponse;
import com.loadbalancing.kiosk.domain.admin.dto.product.DashboardProductResponse;
import com.loadbalancing.kiosk.domain.admin.dto.product.ProductSummaryResponse;
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

        long totalProductCount = products.size();

        List<DashboardProductResponse> outOfStockProducts = products.stream()
                .filter(product -> product.getStock() == 0)
                .map(DashboardProductResponse::from)
                .toList();

        List<DashboardProductResponse> lowStockProducts = products.stream()
                .filter(product ->
                        product.getStock() >= 1
                                && product.getStock() <= 10
                )
                .map(DashboardProductResponse::from)
                .toList();

        List<DashboardProductResponse> recentProducts =
                productRepository.findTop5ByOrderByCreatedAtDesc()
                        .stream()
                        .map(DashboardProductResponse::from)
                        .toList();

        return ProductSummaryResponse.builder()
                .totalProductCount(totalProductCount)
                .outOfStockProducts(outOfStockProducts)
                .lowStockProducts(lowStockProducts)
                .recentProducts(recentProducts)
                .build();
    }
}
