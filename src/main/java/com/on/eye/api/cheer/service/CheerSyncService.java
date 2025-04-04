package com.on.eye.api.cheer.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.entity.ProtestCheerCount;
import com.on.eye.api.cheer.repository.ProtestCheerCountRepository;
import com.on.eye.api.global.common.util.LocalDateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 응원 데이터 DB 영속화 및 cache 복구 로직 담당 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CheerSyncService {
    private final ProtestCheerCountRepository protestCheerCountRepository;
    private final CheerCacheService cheerCacheService;

    /** 주기마다 Cache -> DB로 data Sync */
    @Scheduled(fixedDelayString = "${cheer.sync.interval:300000}") // 기본 5분
    @Transactional
    public void syncCacheToDBRegularly() {
        syncCacheToDB();
    }

    /** 시스템 시작 시 DB -> Cache로 초기 데이터를 로드. 애플리케이션 시작 시 실행되어 이전에 저장된 응원 데이터를 복원. 무중단 배포시 수정 필요 */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    @Transactional(readOnly = true)
    public void initializeCacheFromDatabase() {
        syncDBToCache(false);
    }

    /** 오늘의 시위로 정보 cache 최신화. 현재 방법의 한계: 2~3번 과정 동안 들어오는 응원은 예외로 처리됨 */
    @Transactional(readOnly = true)
    public void updateTodayCheerCache(List<Long> createdProtestIds) {
        // 1. sync cache -> db
        syncCacheToDB();
        // 2. cache clear
        cheerCacheService.clearAllOutdatedCheerCounts();

        // 3. create new cheer counts
        List<ProtestCheerCount> dailyUpdatedCheerList =
                createdProtestIds.stream().map(ProtestCheerCount::from).toList();
        protestCheerCountRepository.saveAll(dailyUpdatedCheerList);

        // 4. sync db -> cache
        syncDBToCache(true);
    }

    private void syncDBToCache(boolean isUpdate) {
        log.info("DB -> Cache 응원 데이터 초기화 시작");

        List<ProtestCheerCount> allCheers =
                protestCheerCountRepository.findAllByProtestBetweenStartDateTimeAndEndDateTime(
                        LocalDateTimeUtils.todayStartTime(), LocalDateTimeUtils.todayEndTime());
        int loadedCount = 0;

        for (ProtestCheerCount cheer : allCheers) {
            try {
                if (isUpdate) {
                    cheerCacheService.setCheerCount(cheer.getProtestId(), cheer.getCheerCount());
                    loadedCount++;
                } else {
                    // Cache의 응원 수가 DB보다 많으면 유지, 아니면 DB 값으로 설정
                    Integer cacheCheerCount =
                            cheerCacheService.getCheerStat(cheer.getProtestId()).cheerCount();
                    if (cacheCheerCount < cheer.getCheerCount()) {
                        cheerCacheService.setCheerCount(
                                cheer.getProtestId(), cheer.getCheerCount());
                        loadedCount++;
                    }
                }
            } catch (Exception e) {
                log.error("시위 ID: {} 데이터 초기화 실패: {}", cheer.getProtestId(), e.getMessage(), e);
            }
        }

        log.info("DB에서 Cache로 응원 데이터 초기화 완료 - 로드된 시위: {}/{}", loadedCount, allCheers.size());
    }

    private void syncCacheToDB() {
        List<CheerStat> dataToSync = cheerCacheService.getCheerCountsForSync();
        log.info("Cache -> DB 응원 데이터 동기화 시작 - 총 {}개 시위", dataToSync.size());

        List<Long> syncedIds = new ArrayList<>();

        for (CheerStat cheerStat : dataToSync) {
            Long protestId = cheerStat.protestId();
            Integer cheerCount = cheerStat.cheerCount();

            try {
                if (cheerCount <= 0) continue;
                ProtestCheerCount protestCheerCount = ProtestCheerCount.from(cheerStat);
                protestCheerCountRepository.save(protestCheerCount);
                log.debug("시위 ID: {} 응원 데이터 Cache -> DB로 저장됨", protestId);
                syncedIds.add(protestId);
            } catch (Exception e) {
                log.error("시위 ID: {} 응원 데이터 동기화 실패: {}", protestId, e.getMessage(), e);
            }
        }

        log.info("응원 데이터 동기화 완료 - 처리된 시위: {}/{}", syncedIds.size(), dataToSync.size());
    }
}
