package com.loadbalancing.kiosk.domain.order.service;

import com.loadbalancing.kiosk.domain.order.dto.OrderCreateRequest;
import com.loadbalancing.kiosk.domain.order.dto.OrderCreateResponse;
import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.domain.order.entity.Status;
import com.loadbalancing.kiosk.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderCreateResponse create(OrderCreateRequest request){
        Order order = Order.builder()
                .email(request.email())
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .postalCode(request.postalCode())
                .status(Status.ORDER_RECEIVED)
                .build();

        Order savedOrder = orderRepository.save(order);

        return OrderCreateResponse.from(savedOrder);

    }



}
