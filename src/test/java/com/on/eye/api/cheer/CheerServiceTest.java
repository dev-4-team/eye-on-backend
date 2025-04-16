package com.on.eye.api.cheer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.error.exception.ProtestCacheNotFoundException;
import com.on.eye.api.cheer.repository.CheerRepository;
import com.on.eye.api.cheer.service.CheerService;
import com.on.eye.api.cheer.service.CheerServiceImpl;

class CheerServiceTest {

    // 테스트 대상
    private CheerService cheerService;

    // 인메모리 저장소 (fake)
    private InMemoryCheerRepository cheerRepository;

    @BeforeEach
    void setUp() {
        // 테스트마다 새로운 인메모리 저장소 생성
        cheerRepository = new InMemoryCheerRepository();

        // 실제 서비스 구현체 사용 (mock 없음)
        cheerService = new CheerServiceImpl(cheerRepository);

        // 테스트 데이터 초기화
        cheerRepository.setCheerCount(1L, 5); // 시위 ID 1, 응원 수 5
        cheerRepository.setCheerCount(2L, 10); // 시위 ID 2, 응원 수 10
    }

    @Test
    @DisplayName("시위에 응원을 하면 응원 수가 1 증가한다")
    void cheerProtest_ShouldIncrementCheerCount() {
        // Given
        Long protestId = 1L;
        int initialCount = cheerRepository.getCheerCount(protestId);

        // When
        CheerStat result = cheerService.cheerProtest(protestId);

        // Then - 상태 검증
        assertEquals(protestId, result.protestId());
        assertEquals(initialCount + 1, result.cheerCount());

        // 저장소의 최종 상태 확인
        assertEquals(initialCount + 1, cheerRepository.getCheerCount(protestId));
    }

    @Test
    @DisplayName("특정 시위의 응원 통계를 조회할 수 있다")
    void getCheerStat_ShouldReturnCorrectStat() {
        // Given
        Long protestId = 2L;
        int expectedCount = 10;

        // When
        CheerStat result = cheerService.getCheerStat(protestId);

        // Then - 상태 검증
        assertEquals(protestId, result.protestId());
        assertEquals(expectedCount, result.cheerCount());
    }

    @Test
    @DisplayName("모든 오늘 시위의 응원 통계를 조회할 수 있다")
    void getTodayCheerStats_ShouldReturnAllStats() {
        // Given
        // 초기 데이터는 이미 setUp에서 준비됨

        // When
        List<CheerStat> results = cheerService.getTodayCheerStats();

        // Then - 상태 검증
        assertEquals(2, results.size());

        // ID 기준 정렬하여 확인
        List<CheerStat> sortedResults =
                results.stream()
                        .sorted((a, b) -> Long.compare(a.protestId(), b.protestId()))
                        .toList();

        assertEquals(1L, sortedResults.get(0).protestId());
        assertEquals(5, sortedResults.get(0).cheerCount());
        assertEquals(2L, sortedResults.get(1).protestId());
        assertEquals(10, sortedResults.get(1).cheerCount());
    }

    @Test
    @DisplayName("존재하지 않는 시위에 응원을 시도하면 예외가 발생한다")
    void cheerProtest_WithNonExistentProtest_ShouldThrowException() {
        // Given
        Long nonExistentProtestId = 999L;

        // When & Then
        assertThrows(
                ProtestCacheNotFoundException.class,
                () -> {
                    cheerService.cheerProtest(nonExistentProtestId);
                });
    }

    @Test
    @DisplayName("존재하지 않는 시위의 통계 조회 시 0을 반환한다")
    void getCheerStat_WithNonExistentProtest_ShouldReturnZero() {
        // Given
        Long nonExistentProtestId = 999L;

        // When
        CheerStat result = cheerService.getCheerStat(nonExistentProtestId);

        // Then
        assertEquals(nonExistentProtestId, result.protestId());
        assertEquals(0, result.cheerCount());
    }

    @Test
    @DisplayName("시스템 장애 시뮬레이션 - 저장소 접근 실패 시 예외 처리")
    void cheerProtest_WithSystemFailure_ShouldHandleException() {
        // Given
        Long protestId = 1L;
        cheerRepository.simulateFailure(true);

        // When & Then
        Exception exception =
                assertThrows(
                        RuntimeException.class,
                        () -> {
                            cheerService.cheerProtest(protestId);
                        });

        assertTrue(exception.getMessage().contains("시스템 장애 시뮬레이션"));
    }

    /** 테스트용 인메모리 CheerRepository 구현체 Redis 의존성을 제거하고 순수한 자바 코드로 동일한 기능 제공 */
    static class InMemoryCheerRepository implements CheerRepository {

        private final Map<Long, Integer> cheerCounts = new HashMap<>();
        private boolean failureMode = false;

        public void simulateFailure(boolean failureMode) {
            this.failureMode = failureMode;
        }

        @Override
        public Integer incrementCheerCount(Long protestId) {
            if (failureMode) {
                throw new RuntimeException("시스템 장애 시뮬레이션");
            }

            if (!cheerCounts.containsKey(protestId)) {
                throw ProtestCacheNotFoundException.EXCEPTION;
            }

            int newCount = cheerCounts.getOrDefault(protestId, 0) + 1;
            cheerCounts.put(protestId, newCount);
            return newCount;
        }

        @Override
        public Integer getCheerCount(Long protestId) {
            if (failureMode) {
                throw new RuntimeException("시스템 장애 시뮬레이션");
            }

            return cheerCounts.getOrDefault(protestId, 0);
        }

        public void setCheerCount(Long protestId, Integer count) {
            if (failureMode) {
                throw new RuntimeException("시스템 장애 시뮬레이션");
            }

            cheerCounts.put(protestId, count);
        }

        @Override
        public List<CheerStat> getTodayCheerStats() {
            if (failureMode) {
                throw new RuntimeException("시스템 장애 시뮬레이션");
            }

            return cheerCounts.entrySet().stream()
                    .map(entry -> new CheerStat(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        public void clearAllOutdatedCheerCounts() {
            if (failureMode) {
                throw new RuntimeException("시스템 장애 시뮬레이션");
            }

            cheerCounts.clear();
        }
    }
}
