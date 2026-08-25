package com.loadbalancing.kiosk.domain.admin.order.service;

import com.loadbalancing.kiosk.domain.admin.order.repository.AdminOrderRepository;
import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.domain.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class AdminOrderService {
    private final AdminOrderRepository orderRepository;


    @Transactional
    public void updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 주문입니다. id=" + id));
        order.updateStatus(status);
    }

    @Transactional
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }
}
