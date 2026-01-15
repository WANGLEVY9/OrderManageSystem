package com.example.oms.controller;

import com.example.oms.dto.ApiResponse;
import com.example.oms.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Statistics", description = "Aggregated order and product statistics")
@SecurityRequirement(name = "BearerAuth")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/counters")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Key counters", description = "Admin metrics such as totals and revenue")
    public ApiResponse<?> counters() {
        return ApiResponse.ok(statsService.allCounters());
    }

    @GetMapping("/rank")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Top products", description = "Rank products by sales with configurable limit")
    public ApiResponse<?> rank(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ApiResponse.ok(statsService.topRank(limit));
    }
}
