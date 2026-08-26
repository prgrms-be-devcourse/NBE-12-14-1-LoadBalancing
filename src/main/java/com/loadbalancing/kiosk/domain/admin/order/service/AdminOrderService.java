package com.loadbalancing.kiosk.domain.admin.order.service;

import com.loadbalancing.kiosk.domain.admin.order.dto.AdminOrderResponse;
import com.loadbalancing.kiosk.domain.admin.order.repository.AdminOrderRepository;
import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.domain.order.entity.OrderStatus;
import com.loadbalancing.kiosk.global.exception.custom.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminOrderService {

    private final AdminOrderRepository adminOrderRepository;

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
}