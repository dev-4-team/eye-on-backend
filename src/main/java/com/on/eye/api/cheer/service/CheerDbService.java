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

        Integer newCount = 1;
        if(optionalCheerCount.isPresent()) {
            newCount = optionalCheerCount.get().getCheerCount() + 1;
        }

        ProtestCheerCount cheerCount = ProtestCheerCount.builder()
                .protestId(protestId)
                .cheerCount(newCount)
                .build();

        protestCheerCountRepository.save(cheerCount);
        log.debug("시위 ID: {} 응원 성공 - 현재 응원 수: {}", protestId, newCount);
        return newCount;
    }

    @Override
    @Transactional(readOnly = true)
    public CheerStat getCheerStat(Long protestId) {
        Optional<ProtestCheerCount> optionalCheerCount = protestCheerCountRepository.findByProtestId(protestId);

        Integer cheerCount = 0;
        if(optionalCheerCount.isPresent()) {
            ProtestCheerCount protestCheerCount = optionalCheerCount.get();
            cheerCount = protestCheerCount.getCheerCount();
        }

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
                .collect(Collectors.toList());
    }
}
