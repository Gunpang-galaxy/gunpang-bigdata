package com.gunpang.heartservice.config;

import com.gunpang.heartservice.interceptor.HandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // topic으로 시작되는 요청을 구독한 모든 사용자들에게 메시지를 broadcast
        registry.enableSimpleBroker("/topic");
        // watch로 시작되는 메시지는 message-handling methods로 라우팅된다
        registry.setApplicationDestinationPrefixes("/watch");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //클라이언트가 웹 소켓 연결을 시작할 수 있는 엔드포인트를 생성
        registry.addEndpoint("/watch-data-to-server")
                .addInterceptors(new HandshakeInterceptor())
            .withSockJS();
    }

}