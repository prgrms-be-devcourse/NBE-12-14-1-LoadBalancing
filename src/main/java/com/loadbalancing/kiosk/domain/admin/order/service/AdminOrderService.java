package com.loadbalancing.kiosk.domain.admin.order.service;

import com.loadbalancing.kiosk.domain.admin.order.dto.response.AdminOrderResponse;
import com.loadbalancing.kiosk.domain.admin.order.repository.AdminOrderRepository;
import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.domain.order.entity.OrderItem;
import com.loadbalancing.kiosk.domain.order.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.order.order.dto.response.OrderListResponse;
import com.loadbalancing.kiosk.domain.order.orderItem.repository.OrderItemRepository;
import com.loadbalancing.kiosk.global.exception.custom.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminOrderService {

    private final AdminOrderRepository adminOrderRepository;
    private final OrderItemRepository orderItemRepository;


    @Transactional
    public AdminOrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = adminOrderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.updateStatus(status);
        return AdminOrderResponse.from(order);
    }

    @Transactional
    public void delete(Long id) {
        Order order = adminOrderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        adminOrderRepository.delete(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderListResponse> search(
            String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable
    ) {
        Page<Order> orders = adminOrderRepository.search(keyword, startDate, endDate, pageable);
        return orders.map(order -> {
            List<OrderItem> items = orderItemRepository.findAllByOrder_Id(order.getId());
            return OrderListResponse.from(order, items);
        });
    }
}