package com.loadbalancing.kiosk.domain.admin.order.service;

import com.loadbalancing.kiosk.domain.admin.order.repository.AdminOrderRepository;
import com.loadbalancing.kiosk.domain.order.entity.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminOrderService {
    private final AdminOrderRepository answerOrderRepository;

    @Transactional
    public void updateStatus(Long orderId, Status newStatus) {

    }
}
