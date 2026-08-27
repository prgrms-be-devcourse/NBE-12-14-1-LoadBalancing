package com.loadbalancing.kiosk.domain.order.orderItem.service;

import com.loadbalancing.kiosk.domain.order.entity.OrderItem;
import com.loadbalancing.kiosk.domain.order.order.repository.OrderRepository;
import com.loadbalancing.kiosk.domain.order.orderItem.repository.OrderItemRepository;
import com.loadbalancing.kiosk.domain.product.repository.ProductRepository;
import com.loadbalancing.kiosk.global.exception.custom.OrderItemNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void delete(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(
                        () -> new OrderItemNotFoundException(id)
                );
        orderItemRepository.delete(orderItem);
    }
}
