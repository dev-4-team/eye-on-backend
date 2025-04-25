package com.on.eye.api.cheer.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.error.exception.ProtestCacheNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisCheerCacheRepository implements CheerCacheRepository {

    private final StringRedisTemplate redisTemplate;
    private static final String CHEER_COUNT_KEY_PREFIX = "protest:cheer:";

    @Override
    public Integer incrementCheerCount(Long protestId) {
        String key = generateCheerCountKeyWithNullCheck(protestId);
        Integer newCount = redisTemplate.opsForValue().increment(key).intValue();
        log.debug("시위 ID: {} 응원 수 증가 - 현재 카운트: {}", protestId, newCount);
        return newCount;
    }

    @Override
    public Integer getCheerCount(Long protestId) {
        String key = generateCheerCountKey(protestId);
        String count = redisTemplate.opsForValue().get(key);
        Integer result = count == null ? 0 : Integer.parseInt(count);
        log.debug("시위 ID: {} 응원 수 조회 - 카운트: {}", protestId, result);
        return result;
    }

    @Override
    public void setCheerCount(Long protestId, Integer count) {
        String key = generateCheerCountKey(protestId); // 여기선 걍 키 만들어야지!
        redisTemplate.opsForValue().set(key, String.valueOf(count));
    }

    @Override
    public List<CheerStat> getTodayCheerStats() {
        List<CheerStat> stats = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(CHEER_COUNT_KEY_PREFIX + "*");
        if (keys.isEmpty()) return stats;

        for (String key : keys) {
            Long protestId = extractProtestId(key);
            if (protestId != null) {
                stats.add(new CheerStat(protestId, getCheerCount(protestId)));
            }
        }
        log.debug("모든 시위 응원 수 조회 - 총 {}개 시위", stats.size());
        return stats;
    }

    @Override
    public void clearAllOutdatedCheerCounts() {
        Set<String> keys = redisTemplate.keys(CHEER_COUNT_KEY_PREFIX + "*");
        if (keys.isEmpty()) return;

        redisTemplate.delete(keys);
    }

    @Override
    public void incrementCheerCountBatch(Map<Long, Integer> pendingCheers) {
        for (Map.Entry<Long, Integer> entry : pendingCheers.entrySet()) {
            String key = "protest:cheer:" + entry.getKey();
            Integer increment = entry.getValue();

            redisTemplate.opsForValue().increment(key, increment);
        }
    }

    private String generateCheerCountKeyWithNullCheck(Long protestId) {
        String key = generateCheerCountKey(protestId);
        String count = redisTemplate.opsForValue().get(key);
        if (count == null) throw ProtestCacheNotFoundException.EXCEPTION;
        return key;
    }

    private String generateCheerCountKey(Long protestId) {
        return CHEER_COUNT_KEY_PREFIX + protestId;
    }

    private Long extractProtestId(String key) {
        try {
            return Long.parseLong(key.substring(CHEER_COUNT_KEY_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
