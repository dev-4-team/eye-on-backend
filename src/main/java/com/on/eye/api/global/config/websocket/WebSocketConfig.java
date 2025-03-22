package com.on.eye.api.global.config.websocket;

import static com.on.eye.api.global.constants.Constants.CORS_ALLOW_LIST;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/** WebSocket 및 STOMP 메시지 브로커 설정을 담당하는 설정 클래스 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /** STOMP 메시지 브로커 설정. /topic: 구독 기반 메시지 전달을 위한 prefix /app: 클라이언트에서 서버로 메시지 전송 시 사용할 prefix */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // server -> client 메세지 브로드캐스트를 위한 prefix
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
        // client -> server 메세지 전송을 위한 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

    /** WebSocket 연결 엔드포인트 등록. SockJS를 통해 WebSocket을 지원하지 않는 브라우저에서도 동작 보장 */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(CORS_ALLOW_LIST).withSockJS();
    }
}
