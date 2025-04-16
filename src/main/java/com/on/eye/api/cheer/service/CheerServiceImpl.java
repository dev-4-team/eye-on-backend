package com.on.eye.api.cheer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.repository.CheerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheerServiceImpl implements CheerService {

    private final CheerRepository redisCheerCacheRepository;

    @Override
    public CheerStat cheerProtest(Long protestId) {
        Integer newCount = redisCheerCacheRepository.incrementCheerCount(protestId);
        return new CheerStat(protestId, newCount);
    }

    @Override
    public CheerStat getCheerStat(Long protestId) {
        Integer cheerCount = redisCheerCacheRepository.getCheerCount(protestId);
        return new CheerStat(protestId, cheerCount);
    }

    @Override
    public List<CheerStat> getTodayCheerStats() {
        return redisCheerCacheRepository.getTodayCheerStats();
    }
}
