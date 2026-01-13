package com.example.oms.service;

import com.example.oms.dto.RegisterRequest;
import com.example.oms.model.Permission;
import com.example.oms.model.Role;
import com.example.oms.model.User;
import com.example.oms.repository.PermissionRepository;
import com.example.oms.repository.RoleRepository;
import com.example.oms.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = roleRepository.findByCode("ROLE_USER")
                .orElseGet(() -> createDefaultRole());
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }

    private Role createDefaultRole() {
        Role role = new Role();
        role.setCode("ROLE_USER");
        role.setName("User");
        Permission viewPermission = new Permission();
        viewPermission.setCode("ORDER_READ");
        viewPermission.setName("Read orders");
        permissionRepository.save(viewPermission);
        role.setPermissions(new HashSet<>(List.of(viewPermission)));
        return roleRepository.save(role);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> new SimpleGrantedAuthority(p.getCode()))
                .collect(Collectors.toList());
        // also include role codes
        user.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority(r.getCode())));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                true,
                true,
                true,
                authorities
        );
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
