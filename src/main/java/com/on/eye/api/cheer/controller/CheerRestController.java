package com.on.eye.api.cheer.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.service.CheerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 응원 관련 REST API 컨트롤러 HTTP 요청을 통한 응원 기능을 제공. Polling vs WebSocket 비교 or WebSocket 불가능한 상황 대처 위해 생성
 */
@RestController
@RequestMapping("cheer/protest")
@RequiredArgsConstructor
@Slf4j
public class CheerRestController {
    private final CheerService cheerService;

    /**
     * 특정 시위의 응원 수를 조회
     *
     * @param protestId 시위 ID
     * @return 시위 응원 통계
     */
    @GetMapping("/{protestId}")
    public ResponseEntity<CheerStat> getCheerStat(@PathVariable Long protestId) {
        CheerStat stats = cheerService.getCheerStat(protestId);
        log.debug("응원 통계 조회 - 시위 ID: {}, 응원 수: {}", protestId, stats.cheerCount());
        return ResponseEntity.ok(stats);
    }

    /**
     * 모든 시위의 응원 통계를 조회
     *
     * @return 모든 시위의 응원 통계 목록
     */
    @GetMapping
    public ResponseEntity<List<CheerStat>> getAllCheerStat() {
        List<CheerStat> statsList = cheerService.getTodayCheerStats();
        log.debug("모든 시위 응원 통계 조회 - 시위 개수: {}", statsList.size());
        return ResponseEntity.ok(statsList);
    }

    /**
     * 시위에 응원. (HTTP 요청용) WebSocket 연결이 불가능한 환경을 위한 대체 API
     *
     * @param protestId 시위 ID
     * @return 응원 후 통계
     */
    @PostMapping("/{protestId}")
    public ResponseEntity<CheerStat> cheerProtest(@PathVariable Long protestId) {
        CheerStat cheerStat = cheerService.cheerProtest(protestId);
        log.debug("REST API 응원 요청 - 시위 ID: {}, 응원 후 카운트: {}", protestId, cheerStat.cheerCount());
        return ResponseEntity.ok(cheerStat);
    }
}
