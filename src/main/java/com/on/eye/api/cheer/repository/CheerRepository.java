package com.on.eye.api.cheer.repository;

import java.util.List;

import com.on.eye.api.cheer.dto.CheerStat;

public interface CheerRepository {
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
     * 특정 기간 내의 시위에 대한 응원 수 조회
     *
     * @return 시위 ID별 응원 수 통계
     */
    List<CheerStat> getTodayCheerStats();
}
