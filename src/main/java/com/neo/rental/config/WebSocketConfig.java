package com.neo.rental.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketMessage; // 추가됨
import org.springframework.web.socket.WebSocketSession; // 추가됨
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration; // 추가됨
import org.springframework.web.socket.handler.WebSocketHandlerDecorator; // 추가됨

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    // 🚩 [추가] 소켓 통신 로그를 찍기 위한 설정 (이게 있어야 디버깅 가능)
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                // 들어오는 모든 데이터(Payload)를 콘솔에 출력
                System.out.println("🔥 [Socket Raw Data] " + message.getPayload());
                super.handleMessage(session, message);
            }
        });
    }
}