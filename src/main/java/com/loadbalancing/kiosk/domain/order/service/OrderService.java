package com.loadbalancing.kiosk.domain.order.service;

import com.loadbalancing.kiosk.domain.notification.dto.OrderCompletedEvent;
import com.loadbalancing.kiosk.domain.order.dto.OrderRequest;
import com.loadbalancing.kiosk.domain.order.dto.OrderResponse;
import com.loadbalancing.kiosk.domain.order.infra.entity.Order;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderItem;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import com.loadbalancing.kiosk.domain.order.infra.repository.OrderItemRepository;
import com.loadbalancing.kiosk.domain.order.infra.repository.OrderRepository;
import com.loadbalancing.kiosk.domain.product.infra.entity.Product;
import com.loadbalancing.kiosk.domain.product.infra.repository.ProductRepository;
import com.loadbalancing.kiosk.global.exception.custom.OrderItemNotFoundException;
import com.loadbalancing.kiosk.global.exception.custom.OrderNotFoundException;
import com.loadbalancing.kiosk.global.exception.custom.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse.OrderInfo createOrder(OrderRequest.OrderCreate request) {

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
        for (OrderRequest.OrderItem itemRequest : request.items()) {

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

        List<OrderItem> allItems = orderItemRepository.findAllByOrder_Id(order.getId());

        OrderResponse.OrderInfo orderInfo = OrderResponse.OrderInfo.from(order, allItems);
        // 주문 응답 정보를 알림 이벤트로 전달
        eventPublisher.publishEvent(OrderCompletedEvent.of(orderInfo));

        return orderInfo;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse.OrderInfo> getEmailOrderList(String email, Pageable pageable) {

        Page<Order> orders = orderRepository.findAllByEmail(email, pageable);

        return orders.map(order -> {
            List<OrderItem> orderItems = orderItemRepository.findAllByOrder_Id(order.getId());
            return OrderResponse.OrderInfo.from(order, orderItems);
        });
    }

    @Transactional(readOnly = true)
    public OrderResponse.OrderInfo getOrderDetail(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        List<OrderItem> orderItems = orderItemRepository.findAllByOrder_Id(order.getId());

        return OrderResponse.OrderInfo.from(order, orderItems);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse.OrderInfo> search(
        String keyword, OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable
    ) {
        Page<Order> orders = orderRepository.search(keyword, status, startDate, endDate, pageable);
        return orders.map(order -> {
            List<OrderItem> items = orderItemRepository.findAllByOrder_Id(order.getId());
            return OrderResponse.OrderInfo.from(order, items);
        });
    }

    @Transactional
    public OrderResponse.OrderStatus updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

        order.updateStatus(status);
        return OrderResponse.OrderStatus.from(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
        orderRepository.delete(order);
    }

    @Transactional
    public void deleteOrderItem(Long orderId, Long itemId) {
        OrderItem orderItem = orderItemRepository.findById(itemId)
                .orElseThrow(
                        () -> new OrderItemNotFoundException(itemId)
                );
        orderItemRepository.delete(orderItem);

        //order에 item들이 하나도 없는지 확인
        boolean existItems = orderItemRepository.existsByOrder_Id(orderId);

        //주문의 item들이 하나도 없다면
        if(!existItems) {
            //지우기 위한 주문도 호출
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(
                            () -> new OrderNotFoundException(orderId)
                    );
            orderRepository.delete(order); //주문도 지우기
        }
    }

    private Order createNewOrder(OrderRequest.OrderCreate request) {

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