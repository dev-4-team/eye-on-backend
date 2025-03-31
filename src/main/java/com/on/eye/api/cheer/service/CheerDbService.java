package com.on.eye.api.cheer.service;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.entity.ProtestCheerCount;
import com.on.eye.api.cheer.repository.ProtestCheerCountRepository;
import com.on.eye.api.protest.entity.Protest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheerDbService implements CheerService{

    private final ProtestCheerCountRepository protestCheerCountRepository;

    @Override
    @Transactional
    public Integer cheerProtest(Long protestId) {
        Optional<ProtestCheerCount> optionalCheerCount = protestCheerCountRepository.findByProtestId(protestId);

        if (optionalCheerCount.isPresent()) {
            Integer newCount = protestCheerCountRepository.incrementCheerCount(protestId);

            log.debug("시위 ID: {} 응원 성공 - 현재 응원 수: {}", protestId, newCount);
            return newCount;
        }

        ProtestCheerCount newCheerCount = ProtestCheerCount.builder()
                .protestId(protestId)
                .cheerCount(1)
                .build();
        protestCheerCountRepository.save(newCheerCount);
        log.debug("시위 ID: {} 첫 응원 등록 - 현재 응원 수: 1", protestId);
        return 1;
    }

    @Override
    @Transactional(readOnly = true)
    public CheerStat getCheerStat(Long protestId) {
        Integer cheerCount = protestCheerCountRepository.findByProtestId(protestId)
                .map(ProtestCheerCount::getCheerCount)
                .orElse(0);

        log.debug("시위 ID: {} 응원 수 조회 - 카운트: {}", protestId, cheerCount);
        return new CheerStat(protestId, cheerCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheerStat> getAllCheerStats() {
        List<ProtestCheerCount> allCheerCounts = protestCheerCountRepository.findAll();

        log.debug("모든 시위 응원 수 조회 - 총 {}개 시위", allCheerCounts.size());
        return allCheerCounts.stream()
                .map(cheerCount -> new CheerStat(cheerCount.getProtestId(), cheerCount.getCheerCount()))
                .toList();
    }
}
