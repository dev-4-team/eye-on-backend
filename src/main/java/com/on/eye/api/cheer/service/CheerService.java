package com.on.eye.api.cheer.service;

import java.util.List;

import com.on.eye.api.cheer.dto.CheerStat;

public interface CheerService {
    /**
     * 시위에 대한 응원 수를 증가시킴
     *
     * @param protestId 시위 ID
     * @return 증가 후의 응원 수
     */
    CheerStat cheerProtest(Long protestId);

    /**
     * 시위의 현재 응원 통계를 조회
     *
     * @param protestId 시위 ID
     * @return 응원 통계 정보
     */
    CheerStat getCheerStat(Long protestId);

    /**
     * 모든 오늘 시위의 응원 통계를 조회
     *
     * @return 모든 시위의 응원 통계 목록
     */
    List<CheerStat> getTodayCheerStats();
}
