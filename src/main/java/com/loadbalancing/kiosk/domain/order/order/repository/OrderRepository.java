package com.loadbalancing.kiosk.domain.order.order.repository;


import com.loadbalancing.kiosk.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>{
    Optional<Order> findByEmailAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String email,
            LocalDateTime start,
            LocalDateTime end
    );
}
