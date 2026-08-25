package com.loadbalancing.kiosk.domain.admin.order.controller;


import com.loadbalancing.kiosk.domain.admin.order.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Controller
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
  //  private final OrderService orderService;

  /*  @PostMapping("/order/status")
    public String updateStatusOrder(Model model, @PathVariable("id") Long id) {

        return null;
    }*/

}
