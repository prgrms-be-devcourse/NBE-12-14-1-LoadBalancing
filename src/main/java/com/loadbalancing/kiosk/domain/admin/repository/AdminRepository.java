package com.loadbalancing.kiosk.domain.admin.repository;

import com.loadbalancing.kiosk.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminId(String adminId);
}
