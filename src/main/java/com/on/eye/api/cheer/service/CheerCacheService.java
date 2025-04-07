package com.on.eye.api.cheer.service;

import static com.on.eye.api.cheer.constant.CheerConstants.CHEER_TOPIC;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.dto.CheerUpdateDto;
import com.on.eye.api.cheer.repository.CheerCacheRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 응원 관련 cache 사용과 socket message 발송 담당. */
@Service
@RequiredArgsConstructor
@Slf4j
public class CheerCacheService implements CheerService {

    private final CheerCacheRepository cheerCacheRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 시위에 대한 응원 수를 증가시키고 이를 구독자들에게 브로드캐스트.
     *
     * @param protestId 시위 ID
     * @return 증가 후의 응원 수
     */
    @Override
    public Integer cheerProtest(Long protestId) {
        // 시위 존재 확인

        Integer newCount = cheerCacheRepository.incrementCheerCount(protestId);

        CheerUpdateDto updateDto = CheerUpdateDto.of(protestId, newCount);
        // websocket을 통해 모든 구독자에게 업데이트 전송
        publishCheerUpdate(updateDto);
        return newCount;
    }

    /**
     * 시위의 현재 응원 통계를 조회.
     *
     * @param protestId 시위 ID
     * @return 응원 통계 정보
     */
    @Override
    public CheerStat getCheerStat(Long protestId) {
        Integer cheerCount = cheerCacheRepository.getCheerCount(protestId);
        return new CheerStat(protestId, cheerCount);
    }

    /**
     * 모든 시위의 응원 통계를 조회.
     *
     * @return 모든 시위의 응원 통계 목록
     */
    @Override
    public List<CheerStat> getAllCheerStats() {
        return cheerCacheRepository.getAllCheerStats();
    }

    /**
     * 시위의 응원 수를 직접 설정.
     *
     * @param protestId 시위 ID
     * @param count 설정할 응원 수
     */
    public void setCheerCount(Long protestId, Integer count) {

        cheerCacheRepository.setCheerCount(protestId, count);

        // 변경된 값을 실시간으로 발행
        publishCheerUpdate(CheerUpdateDto.of(protestId, count));
    }

    public void publishCheerUpdate(CheerUpdateDto updateDto) {
        String destination = CHEER_TOPIC;
        simpMessagingTemplate.convertAndSend(destination, updateDto);
    }

    public void clearAllOutdatedCheerCounts() {
        cheerCacheRepository.clearAllOutdatedCheerCounts();
    }

    public List<CheerStat> getCheerCountsForSync() {
        return cheerCacheRepository.getCheerStatsForSync();
    }
}
