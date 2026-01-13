package com.example.oms.aspect;

import com.example.oms.service.StatsService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ApiMetricsAspect {
    private final StatsService statsService;

    public ApiMetricsAspect(StatsService statsService) {
        this.statsService = statsService;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllers() {
    }

    @AfterReturning("restControllers()")
    public void record(JoinPoint joinPoint) {
        String key = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        statsService.hit(key);
    }
}
