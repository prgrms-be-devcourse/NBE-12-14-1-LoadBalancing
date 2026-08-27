package com.loadbalancing.kiosk.domain.admin.controller;

import com.loadbalancing.kiosk.domain.admin.dto.AdminDashboardResponse;
import com.loadbalancing.kiosk.domain.admin.service.AdminDashboardService;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//todo 나중에 하나의 AdminController로 병합 예정
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> dashboard(){
        AdminDashboardResponse response =
                adminDashboardService.getDashboard();

        return ResponseEntity.ok(
                ApiResponse.success(200,response)
        );
    }
}
