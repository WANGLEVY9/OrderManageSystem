package com.example.oms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final boolean enabled;
    private final long limit;
    private final Duration window;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitFilter(StringRedisTemplate redisTemplate,
            @Value("${ratelimit.enabled:true}") boolean enabled,
            @Value("${ratelimit.limit:100}") long limit,
            @Value("${ratelimit.window-seconds:60}") long windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
        this.limit = limit;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !enabled
                || path.startsWith("/api/auth")
                || path.startsWith("/swagger")
                || path.startsWith("/v3")
                || path.startsWith("/doc.html")
                || path.startsWith("/webjars")
                || path.startsWith("/swagger-resources")
                || "/health".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String subject = request.getRemoteAddr();
        if (request.getUserPrincipal() != null) {
            subject = request.getUserPrincipal().getName();
        }
        String key = "ratelimit:" + subject + ":" + request.getRequestURI();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window);
        }
        if (count != null && count > limit) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "success", false,
                    "message", "Rate limit exceeded"
            )));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
