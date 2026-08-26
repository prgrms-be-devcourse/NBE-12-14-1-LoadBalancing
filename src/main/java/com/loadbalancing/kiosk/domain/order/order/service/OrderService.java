package com.loadbalancing.kiosk.domain.order.order.service;

import com.loadbalancing.kiosk.domain.order.entity.OrderItem;
import com.loadbalancing.kiosk.domain.order.order.dto.request.OrderCreateRequest;
import com.loadbalancing.kiosk.domain.order.order.dto.response.OrderCreateResponse;
import com.loadbalancing.kiosk.domain.order.entity.Order;
import com.loadbalancing.kiosk.domain.order.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.order.order.dto.response.OrderDetailResponse;
import com.loadbalancing.kiosk.domain.order.order.dto.response.OrderListResponse;
import com.loadbalancing.kiosk.domain.order.order.repository.OrderRepository;
import com.loadbalancing.kiosk.domain.order.orderItem.dto.OrderItemRequest;
import com.loadbalancing.kiosk.domain.order.orderItem.repository.OrderItemRepository;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.repository.ProductRepository;
import com.loadbalancing.kiosk.global.exception.custom.OrderNotFoundException;
import com.loadbalancing.kiosk.global.exception.custom.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderCreateResponse create(OrderCreateRequest request) {

        // 1.현재 시간
        LocalDateTime now = LocalDateTime.now();

        // 2.현재 시간이 속한 처리주기 계산
        LocalDateTime cycleStart = calculateCycleStart(now);
        LocalDateTime cycleEnd = cycleStart.plusDays(1);

        // 3.같은 이메일 + 같은 처리주기의 Order 조회
        // 위에서 계산한 주기에 기존 주문이 있으면 그걸 쓰고, 없으면 order생성.
        Order order = orderRepository
                .findByEmailAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        request.email(),
                        cycleStart,
                        cycleEnd
                )
                .orElseGet(() -> createNewOrder(request));

        // 4.요청받은 상품마다 OrderItem 생성
        for (OrderItemRequest itemRequest : request.items()) {

            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() ->
                            new ProductNotFoundException(itemRequest.productId())
                    );
            product.decreaseStock(itemRequest.quantity().intValue());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .build();

            orderItemRepository.save(orderItem);
        }

        return OrderCreateResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderListResponse> list(
            String email,
            Pageable pageable
    ) {

        Page<Order> orders =
                orderRepository.findAllByEmail(email, pageable);

        return orders.map(order -> {

            List<OrderItem> orderItems =
                    orderItemRepository.findAllByOrder_Id(
                            order.getId()
                    );

            return OrderListResponse.from(
                    order,
                    orderItems
            );
        });
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse detail(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new OrderNotFoundException(orderId)
                );

        List<OrderItem> orderItems =
                orderItemRepository.findAllByOrder_Id(order.getId());

        return OrderDetailResponse.from(
                order,
                orderItems
        );
    }

    private Order createNewOrder(OrderCreateRequest request) {

        Order order = Order.builder()
                .email(request.email())
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .postalCode(request.postalCode())
                .orderStatus(OrderStatus.ORDER_RECEIVED)
                .build();

        return orderRepository.save(order);
    }

    private LocalDateTime calculateCycleStart(LocalDateTime now) {

        LocalDateTime today2pm =
                now.toLocalDate().atTime(14, 0);

        if (now.isBefore(today2pm)) {
            return today2pm.minusDays(1);
        }

        return today2pm;
    }
}