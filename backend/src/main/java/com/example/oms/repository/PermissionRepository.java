package com.example.oms.repository;

import com.example.oms.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByCode(String code);
}
