package com.testing.websocket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConfig.class);

	@Value("${spring.rabbitmq.username}") private String userName;
	@Value("${spring.rabbitmq.password}") private String password;
	@Value("${spring.rabbitmq.host}")     private String host;
	@Value("${spring.rabbitmq.port}")     private int    port;
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableStompBrokerRelay("/queue/", "/topic/")
			.setRelayHost(host)
			.setRelayPort(port)
			.setClientLogin("guest")
			.setClientPasscode("guest")
			.setSystemLogin(userName)
		.setSystemPasscode(password);
		config.setUserDestinationPrefix("/user");
		config.setApplicationDestinationPrefixes("/app");
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/channel").setAllowedOrigins("*");
	}
	
	@EventListener
	public void onSocketConnected(SessionConnectedEvent event) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
		LOGGER.info("WebSocket Session Connected: {}", event.getMessage());
		LOGGER.info("Connect event [sessionId: {} ]", sha.getSessionId());
	}
	
	@EventListener
	public void onSocketDisconnected(SessionDisconnectEvent event) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
		LOGGER.info("WebSocket Session Disconnected: {}", event.getMessage());
		LOGGER.info("DisConnect event [sessionId: {}]", sha.getSessionId());
	}
}
