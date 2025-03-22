package com.on.eye.api.global.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import lombok.extern.slf4j.Slf4j;

/** WebSocket 에러 처리 및 고급 설정 클래스 */
@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketAdvancedConfig implements WebSocketMessageBrokerConfigurer {
    /** STOMP 에러 핸들러 웹소켓 통신 중 발생하는 예외를 처리. */
    @Bean
    public StompSubProtocolErrorHandler stompErrorHandler() {
        return new StompSubProtocolErrorHandler() {
            @Override
            public Message<byte[]> handleClientMessageProcessingError(
                    Message<byte[]> clientMessage, Throwable ex) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(clientMessage, StompHeaderAccessor.class);

                if (accessor != null) {
                    log.error(
                            "WebSocket 메시지 처리 오류 - 명령: {}, 목적지: {}, 오류: {}",
                            accessor.getCommand(),
                            accessor.getDestination(),
                            ex.getMessage());
                } else {
                    log.error("WebSocket 메시지 처리 오류: {}", ex.getMessage());
                }

                return super.handleClientMessageProcessingError(clientMessage, ex);
            }
        };
    }

    /** WebSocket 하트비트 설정을 위한 Task Scheduler 연결 유지를 위한 하트비트 메시지 전송에 사용. */
    @Bean
    public TaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("ws-heartbeat-thread-");
        scheduler.setDaemon(true);
        return scheduler;
    }
}
