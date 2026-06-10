package com.campus.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotSearchService {

    private final StringRedisTemplate redisTemplate;
    private static final String HOT_SEARCH_KEY = "hot:search";
    private static final int MAX_HOT_SEARCH = 10;

    public void record(String keyword) {
        if (keyword == null || keyword.isBlank()) return;
        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) return;
        redisTemplate.opsForZSet().incrementScore(HOT_SEARCH_KEY, trimmed, 1);
    }

    public List<String> getHotSearches() {
        var entries = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_SEARCH_KEY, 0, MAX_HOT_SEARCH - 1);
        if (entries == null) return List.of();
        return entries.stream()
                .map(e -> e.getValue() != null ? e.getValue() : "")
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
