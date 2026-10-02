package com.xxxx.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final ChatAuthenticationInterceptor authentication;
    @Value("${app.websocket.allowed-origins:http://localhost:8080,http://127.0.0.1:8080,http://localhost:8081,http://127.0.0.1:8081}")
    private String[] allowedOrigins;
    public WebSocketConfig(ChatAuthenticationInterceptor authentication) { this.authentication = authentication; }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/ep").setAllowedOrigins(allowedOrigins).withSockJS();
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) { registration.interceptors(authentication); }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue").setTaskScheduler(chatHeartbeatScheduler()).setHeartbeatValue(new long[]{10000,10000});
    }
    @Bean
    public ThreadPoolTaskScheduler chatHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); scheduler.setThreadNamePrefix("chat-heartbeat-"); scheduler.setDaemon(true);
        return scheduler;
    }
}