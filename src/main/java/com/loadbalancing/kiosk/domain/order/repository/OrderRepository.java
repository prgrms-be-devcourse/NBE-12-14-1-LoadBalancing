package com.loadbalancing.kiosk.domain.order.repository;


import com.loadbalancing.kiosk.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>{
}
