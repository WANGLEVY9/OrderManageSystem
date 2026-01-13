package com.example.oms.controller;

import com.example.oms.dto.ApiResponse;
import com.example.oms.dto.OrderCreateRequest;
import com.example.oms.model.OrderEntity;
import com.example.oms.model.User;
import com.example.oms.service.OrderService;
import com.example.oms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ORDER_CREATE') or hasRole('ADMIN')")
    public ApiResponse<OrderEntity> create(@AuthenticationPrincipal UserDetails principal,
                                           @Valid @RequestBody OrderCreateRequest request) {
        User user = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ApiResponse.ok(orderService.createOrder(user, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderEntity> updateStatus(@PathVariable Long id,
                                                 @RequestParam(name = "status") String status,
                                                 @AuthenticationPrincipal UserDetails principal) {
        return ApiResponse.ok(orderService.updateStatus(id, status, principal.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OrderEntity>> listAll(@RequestParam(name = "page", defaultValue = "0") int page,
                                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        return ApiResponse.ok(orderService.listAll(page, size));
    }

    @GetMapping("/mine")
    public ApiResponse<Page<OrderEntity>> myOrders(@AuthenticationPrincipal UserDetails principal,
                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        User user = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ApiResponse.ok(orderService.listOrders(user, page, size));
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ORDER_READ')")
    public ApiResponse<?> logs(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getLogs(id));
    }
}
