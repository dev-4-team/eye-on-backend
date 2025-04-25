package com.on.eye.api.cheer.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.on.eye.api.cheer.repository.JpaCheerRepository;
import com.on.eye.api.cheer.repository.RedisCheerCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.repository.CheerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheerServiceImpl implements CheerService {

    private final CheerRepository cheerRepository;

    private final ConcurrentHashMap<Long, Integer> pendingCheers = new ConcurrentHashMap<>();
    private final AtomicInteger pendingCheerCount = new AtomicInteger(0);

    private static final int BATCH_THRESHOLD = 1000;
    private static final long FLUSH_INTERVAL = 3000;

    @Autowired
    public CheerServiceImpl(JpaCheerRepository jpaCheerRepository,
                            RedisCheerCacheRepository redisCheerCacheRepository,
                            @Value("${cheer.repository.type:redis}") String repositoryType) {

        this.cheerRepository = "jpa".equals(repositoryType) ? jpaCheerRepository : redisCheerCacheRepository;
    }

    @Override
    public CheerStat cheerProtest(Long protestId) {

        pendingCheers.compute(protestId, (id, count) -> count == null ? 1 : count + 1);

        pendingCheerCount.incrementAndGet();

        if (isFlushNeed()) {
            flushPendingCheers();
        }

        return new CheerStat(protestId, 0);
    }

    @Override
    public CheerStat getCheerStat(Long protestId) {
        Integer cheerCount = cheerRepository.getCheerCount(protestId);
        return new CheerStat(protestId, cheerCount);
    }

    @Override
    public List<CheerStat> getTodayCheerStats() {
        return cheerRepository.getTodayCheerStats();
    }

    @Scheduled(fixedRate = FLUSH_INTERVAL)
    public void scheduledFlush() {
        if (hasPendingCheers()) {
            flushPendingCheers();
        }
    }

    private synchronized void flushPendingCheers() {
        if (pendingCheers.isEmpty()) {
            return;
        }

        cheerRepository.incrementCheerCountBatch(pendingCheers);

        pendingCheers.clear();
        pendingCheerCount.set(0);
    }

    private boolean isFlushNeed() {
        return pendingCheerCount.get() >= BATCH_THRESHOLD;
    }

    private boolean hasPendingCheers() {
        return pendingCheerCount.get() >= 0;
    }
}
