package com.loadbalancing.kiosk.domain.admin.dto;

import lombok.Builder;

public class AdminResponse {

    public record LoginResponse(String token) {}
}
