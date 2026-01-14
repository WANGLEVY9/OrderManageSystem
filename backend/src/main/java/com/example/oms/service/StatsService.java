package com.example.oms.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final String API_COUNTER = "api:counter";
    private static final String API_RANK = "api:rank";
    private final StringRedisTemplate redisTemplate;

    public StatsService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void hit(String key) {
        redisTemplate.opsForValue().increment(API_COUNTER + ":" + key);
        redisTemplate.opsForZSet().incrementScore(API_RANK, key, 1.0);
    }

    public Map<String, Long> allCounters() {
        Set<String> keys = redisTemplate.keys(API_COUNTER + ":*");
        if (keys == null) {
            return Map.of();
        }
        return keys.stream().collect(Collectors.toMap(
                k -> k.substring((API_COUNTER + ":").length()),
                k -> {
                    String value = redisTemplate.opsForValue().get(k);
                    if (value == null) {
                        return 0L;
                    }
                    try {
                        return Long.parseLong(value);
                    } catch (NumberFormatException ex) {
                        return 0L;
                    }
                }
        ));
    }

    public Map<String, Double> topRank(int limit) {
        var tuples = redisTemplate.opsForZSet().reverseRangeWithScores(API_RANK, 0, limit - 1);
        if (tuples == null) {
            return Map.of();
        }
        return tuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.getValue().toString(),
                        tuple -> tuple.getScore()
                ));
    }
}
