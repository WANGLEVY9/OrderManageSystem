package com.example.oms.config;

import com.example.oms.model.Permission;
import com.example.oms.model.Role;
import com.example.oms.model.User;
import com.example.oms.repository.PermissionRepository;
import com.example.oms.repository.RoleRepository;
import com.example.oms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initData(PermissionRepository permissionRepository,
                               RoleRepository roleRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            Permission orderRead = ensurePermission(permissionRepository, "ORDER_READ", "Read orders");
            Permission orderCreate = ensurePermission(permissionRepository, "ORDER_CREATE", "Create orders");
            Permission orderWrite = ensurePermission(permissionRepository, "ORDER_WRITE", "Write orders");

            Role adminRole = roleRepository.findByCode("ROLE_ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setCode("ROLE_ADMIN");
                r.setName("Admin");
                r.setPermissions(new HashSet<>(List.of(orderRead, orderCreate, orderWrite)));
                return roleRepository.save(r);
            });

            roleRepository.findByCode("ROLE_USER").orElseGet(() -> {
                Role r = new Role();
                r.setCode("ROLE_USER");
                r.setName("User");
                r.setPermissions(new HashSet<>(List.of(orderRead, orderCreate)));
                return roleRepository.save(r);
            });

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(Set.of(adminRole));
                userRepository.save(admin);
            }
        };
    }

    private Permission ensurePermission(PermissionRepository repository, String code, String name) {
        return repository.findAll().stream()
                .filter(p -> code.equals(p.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode(code);
                    p.setName(name);
                    return repository.save(p);
                });
    }
}
