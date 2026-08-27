package com.loadbalancing.kiosk.domain.admin.dto;

import lombok.Builder;

@Builder
public record AdminDashboardResponse(
        //상품 종류 개수
        Long totalProductCount
){

}