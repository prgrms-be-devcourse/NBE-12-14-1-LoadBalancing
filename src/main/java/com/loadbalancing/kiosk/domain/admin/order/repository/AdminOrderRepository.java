package com.loadbalancing.kiosk.domain.admin.order.repository;

import com.loadbalancing.kiosk.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminOrderRepository extends JpaRepository<Order, Long> {
}