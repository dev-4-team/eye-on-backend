package com.on.eye.api.cheer.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.entity.ProtestCheerCount;
import com.on.eye.api.cheer.error.exception.ProtestCacheNotFoundException;
import com.on.eye.api.global.common.util.LocalDateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class JpaCheerRepository implements CheerRepository {

    private final ProtestCheerCountRepository protestCheerCountRepository;

    @Override
    @Transactional
    public Integer incrementCheerCount(Long protestId) {
        Optional<ProtestCheerCount> optionalCheerCount =
                protestCheerCountRepository.findByProtestId(protestId);

        if (optionalCheerCount.isPresent()) {
            protestCheerCountRepository.incrementCheerCount(protestId);
            Integer newCount = optionalCheerCount.get().getCheerCount() + 1;

            log.debug("시위 ID: {} 응원 성공 - 현재 응원 수: {}", protestId, newCount);
            return newCount;
        }

        ProtestCheerCount newCheerCount =
                ProtestCheerCount.builder().protestId(protestId).cheerCount(1).build();
        protestCheerCountRepository.save(newCheerCount);
        log.debug("시위 ID: {} 첫 응원 등록 - 현재 응원 수: 1", protestId);
        return 1;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCheerCount(Long protestId) {
        return protestCheerCountRepository
                .findByProtestId(protestId)
                .map(ProtestCheerCount::getCheerCount)
                .orElseThrow(() -> ProtestCacheNotFoundException.EXCEPTION);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheerStat> getTodayCheerStats() {
        LocalDateTime todayStartTime = LocalDateTimeUtils.todayStartTime();
        LocalDateTime todayEndTime = LocalDateTimeUtils.todayEndTime();
        return protestCheerCountRepository
                .findAllByProtestBetweenStartDateTimeAndEndDateTime(todayStartTime, todayEndTime)
                .stream()
                .map(
                        cheerCount ->
                                new CheerStat(
                                        cheerCount.getProtestId(), cheerCount.getCheerCount()))
                .toList();
    }
}
