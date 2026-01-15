package com.example.oms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Service liveness probe")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Lightweight endpoint for probes")
    public String health() {
        return "ok";
    }
}
