package com.loadbalancing.kiosk.domain.admin.infra.repository;

import com.loadbalancing.kiosk.domain.admin.infra.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminId(String adminId);
}
