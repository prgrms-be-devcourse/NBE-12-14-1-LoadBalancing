package com.loadbalancing.kiosk.domain.admin.order.repository;

import com.loadbalancing.kiosk.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminOrderRepository extends JpaRepository<Admin, Long> {

}