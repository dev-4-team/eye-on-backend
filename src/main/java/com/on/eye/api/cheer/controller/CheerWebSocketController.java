package com.on.eye.api.cheer.controller;

import static com.on.eye.api.cheer.constant.CheerConstants.CHEER_TOPIC;

import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.cheer.service.CheerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 응원 관련 WebSocket 메시지 핸들러 클라이언트와의 WebSocket 통신을 처리 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CheerWebSocketController {

    private final CheerService cheerCacheService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 클라이언트로부터 응원 요청을 처리. 예: /app/cheer/protest/123 (123은 시위 ID)
     *
     * @param protestId 시위 ID
     */
    @MessageMapping("/cheer/protest/{protestId}")
    public void processCheerRequest(@DestinationVariable Long protestId) {
        CheerStat cheerStat = cheerCacheService.cheerProtest(protestId);
        simpMessagingTemplate.convertAndSend(CHEER_TOPIC, cheerStat);
    }

    /**
     * 클라이언트가 토픽을 구독할 때 초기 데이터를 제공. 예: /topic/cheer
     *
     * @return 해당 시위의 현재 응원 정보
     */
    @SubscribeMapping(CHEER_TOPIC)
    public List<CheerStat> getInitCheerCounts() {
        return cheerCacheService.getTodayCheerStats();
    }
}
