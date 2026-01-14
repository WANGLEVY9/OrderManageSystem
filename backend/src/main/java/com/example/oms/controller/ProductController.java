package com.example.oms.controller;

import com.example.oms.dto.ApiResponse;
import com.example.oms.dto.ProductRequest;
import com.example.oms.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(productService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> create(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.create(request));
    }
}
