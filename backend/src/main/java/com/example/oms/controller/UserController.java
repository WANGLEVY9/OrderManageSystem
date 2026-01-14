package com.example.oms.controller;

import com.example.oms.dto.ApiResponse;
import com.example.oms.dto.UserUpdateRequest;
import com.example.oms.model.Role;
import com.example.oms.model.User;
import com.example.oms.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<User>> list() {
        return ApiResponse.ok(userService.findAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> update(@PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(userService.updateUser(id, request));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Role>> roles() {
        return ApiResponse.ok(userService.findRoles());
    }
}
