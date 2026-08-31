package com.loadbalancing.kiosk.domain.order.dto;

import com.loadbalancing.kiosk.domain.order.infra.entity.Order;
import com.loadbalancing.kiosk.domain.order.infra.entity.OrderItem;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    @Builder
    public record OrderInfo(
        Long orderId,
        String email,
        String addressLine1,
        String addressLine2,
        String postalCode,
        String status,
        LocalDateTime createdAt,
        List<OrderItemInfo> items
    ) {

        public static OrderInfo from(Order order, List<OrderItem> orderItems) {

            List<OrderItemInfo> items = orderItems.stream()
                .map(OrderItemInfo::from)
                .toList();

            return OrderInfo.builder()
                .orderId(order.getId())
                .email(order.getEmail())
                .addressLine1(order.getAddressLine1())
                .addressLine2(order.getAddressLine2())
                .postalCode(order.getPostalCode())
                .status(order.getOrderStatus().getDescription())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
        }
    }
    @Builder
    public record OrderItemInfo(
        // OrderItem 자신의 PK. OrderController.deleteOrderItem(orderId, itemId)를 호출하려면
        // productId가 아니라 이 id(주문 항목 자체의 id)가 필요해서 추가함.
        Long itemId,
        Long productId,
        String title,
        int price,
        Long quantity
    ) {

        public static OrderItemInfo from(OrderItem orderItem) {
            return OrderItemInfo.builder()
                .itemId(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .title(orderItem.getProduct().getTitle())
                .price(orderItem.getProduct().getPrice())
                .quantity(orderItem.getQuantity())
                .build();
        }
    }

    @Builder
    public record OrderStatus(
        Long orderId,
        String status
    ){
        public static OrderStatus from(Order order){
            return OrderStatus.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus().getDescription())
                .build();
        }
    }

}

