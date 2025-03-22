package com.on.eye.api.global.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import lombok.extern.slf4j.Slf4j;

/** WebSocket 이벤트 리스너 설정 연결, 연결 해제, 구독, 구독 취소 등의 이벤트를 처리. */
@Configuration
@Slf4j
public class WebSocketEventListener {
    /** 클라이언트 연결 완료 이벤트 핸들러 */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Authentication user = (Authentication) headerAccessor.getUser();

        if (user != null) {
            log.info("사용자 연결됨: {}", user.getName());
        } else {
            log.info("익명 사용자 연결됨: {}", headerAccessor.getSessionId());
        }
    }

    /** 클라이언트 연결 해제 이벤트 핸들러 */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Authentication user = (Authentication) headerAccessor.getUser();

        log.debug(
                "연결 해제 이벤트 발생 - 세션: {}, 상태: {}, 이벤트 메세지: {}",
                headerAccessor.getSessionId(),
                headerAccessor.getCommand(),
                event.getMessage());

        if (user != null) {
            log.info("사용자 연결 해제됨: {}", user.getName());
        } else {
            log.info("익명 사용자 연결 해제됨: {}", headerAccessor.getSessionId());
        }
    }

    /** 토픽 구독 이벤트 핸들러 */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        Authentication user = (Authentication) headerAccessor.getUser();

        if (user != null) {
            log.debug("사용자 {} 토픽 구독: {}", user.getName(), destination);
        } else {
            log.debug("익명 사용자 {} 토픽 구독: {}", headerAccessor.getSessionId(), destination);
        }
    }

    /** 토픽 구독 취소 이벤트 핸들러 */
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        Authentication user = (Authentication) headerAccessor.getUser();

        if (user != null) {
            log.debug("사용자 {} 구독 취소: {}", user.getName(), destination);
        } else {
            log.debug("익명 사용자 {} 구독 취소: {}", headerAccessor.getSessionId(), destination);
        }
    }
}
