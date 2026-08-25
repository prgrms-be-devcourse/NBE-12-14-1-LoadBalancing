package com.loadbalancing.kiosk.domain.admin.controller;

import com.loadbalancing.kiosk.domain.admin.dto.AdminRequest;
import com.loadbalancing.kiosk.domain.admin.dto.AdminResponse;
import com.loadbalancing.kiosk.domain.admin.service.AdminAuthService;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminService;

    @PostMapping("login")
    public ResponseEntity<ApiResponse<AdminResponse.LoginResponse>> login(
        @RequestBody AdminRequest.LoginRequest request) {

        AdminResponse.LoginResponse response = adminService.login(request);

        return ResponseEntity.ok(ApiResponse.success(200, response));
    }


}
