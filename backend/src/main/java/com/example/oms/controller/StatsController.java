package com.example.oms.controller;

import com.example.oms.dto.ApiResponse;
import com.example.oms.service.StatsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/counters")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> counters() {
        return ApiResponse.ok(statsService.allCounters());
    }

    @GetMapping("/rank")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> rank(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ApiResponse.ok(statsService.topRank(limit));
    }
}
