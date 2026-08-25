package com.loadbalancing.kiosk.domain.order.dto;

public record OrderCreateRequest(
        String email,
        String addressLine1,
        String addressLine2,
        String postalCode
){

}