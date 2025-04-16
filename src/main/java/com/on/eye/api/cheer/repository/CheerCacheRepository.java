package com.on.eye.api.cheer.repository;

public interface CheerCacheRepository extends CheerRepository {
    /** 캐시에서 오래된 응원 데이터 정리 */
    void clearAllOutdatedCheerCounts();

    void setCheerCount(Long protestId, Integer count);
}
