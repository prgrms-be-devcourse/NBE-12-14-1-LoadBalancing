package com.loadbalancing.kiosk.domain.admin.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@RestController
public class AdminOrderController {
/*
    private final AdminOrderService adminOrderService;
    private final OrderService orderService;

    *//* 주문 상태변경*//*
    @PatchMapping("/order/status/{id}")
    @Transactional
    public String updateStatusOrder(@PathVariable Long id,
                                    @Valid @RequestBody PostModifyReqBody reqBody) {

        Order order = orderService.findById(id).get();
        orderService.modify(post, reqBody.title, reqBody.content);

        return new RsData<>(
                "200-1",
                "%d번 게시물이 수정되었습니다.".formatted(id)
        );
    }
    *//* 주문 삭제*//*
    @DeleteMapping("/order/{id}")
    public RsData<Void> delete(
            @PathVariable int id
    ) {
        postService.delete(id);

        return new RsData<>(
                "200-1",
                "%d번 게시물이 삭제되었습니다.".formatted(id)
        );
    }*/
}