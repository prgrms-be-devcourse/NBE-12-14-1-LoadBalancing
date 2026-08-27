package com.loadbalancing.kiosk.domain.admin.service;

import com.loadbalancing.kiosk.domain.admin.dto.AdminDashboardResponse;
import com.loadbalancing.kiosk.domain.order.order.repository.OrderRepository;
import com.loadbalancing.kiosk.domain.order.orderItem.repository.OrderItemRepository;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

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

        long outOfStockProductCount = products.stream()
                .filter(product -> product.getStock() == 0)
                .count();
        long lowStockProductCount = products.stream()
                .filter(product ->
                        product.getStock() >= 1
                                && product.getStock() <= 10
                )
                .count();


        return AdminDashboardResponse.builder()
                .totalProductCount(totalProductCount)
                .outOfStockProductCount(outOfStockProductCount)
                .lowStockProductCount(lowStockProductCount)
                .build();
    }
}
