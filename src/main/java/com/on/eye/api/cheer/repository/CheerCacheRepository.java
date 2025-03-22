package com.on.eye.api.cheer.repository;

import java.util.List;

import com.on.eye.api.cheer.dto.CheerStat;

public interface CheerCacheRepository {
    /**
     * 시위의 응원 수를 1 증가.
     *
     * @param protestId 시위 ID
     * @return 증가 후의 응원 수
     */
    Integer incrementCheerCount(Long protestId);

    /**
     * 시위의 현재 응원 수를 조회.
     *
     * @param protestId 시위 ID
     * @return 현재 응원 수
     */
    Integer getCheerCount(Long protestId);

    /**
     * 모든 시위의 응원 수 조회
     *
     * @return 시위 ID별 응원 수 맵
     */
    List<CheerStat> getAllCheerStats();

    /**
     * 지정된 시위의 응원 수 설정 기존 데이터를 덮어씁니다.
     *
     * @param protestId 시위 ID
     * @param count 응원 수
     */
    void setCheerCount(Long protestId, Integer count);

    /** Redis에 저장된 응원 수 데이터를 영구 저장소와 동기화하기 위해 필요한 모든 시위의 응원 수를 한 번에 가져오기. */
    List<CheerStat> getCheerStatsForSync();

    /** 모든 캐시 clear */
    void clearAllOutdatedCheerCounts();
}
