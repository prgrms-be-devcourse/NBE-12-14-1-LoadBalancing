package com.loadbalancing.kiosk.domain.admin.service;

import com.loadbalancing.kiosk.domain.admin.dto.AdminRequest;
import com.loadbalancing.kiosk.domain.admin.dto.AdminResponse;
import com.loadbalancing.kiosk.domain.admin.entity.Admin;
import com.loadbalancing.kiosk.domain.admin.repository.AdminRepository;
import com.loadbalancing.kiosk.global.exception.custom.InvalidLoginException;
import com.loadbalancing.kiosk.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AdminResponse.LoginResponse login(AdminRequest.LoginRequest request) {
        Admin admin = adminRepository.findByAdminId(request.adminId())
            .orElseThrow(() -> new IllegalArgumentException("관리자 아이디가 존재하지 않습니다."));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new InvalidLoginException();
        }

        String token = jwtProvider.generateToken(admin.getAdminId());
        return new AdminResponse.LoginResponse(token);
    }
}
