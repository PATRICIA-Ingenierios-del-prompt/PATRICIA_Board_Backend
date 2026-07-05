package com.patricia.board.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final BoardRabbitProperties rabbitProperties;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        var brokerRelay = config.enableStompBrokerRelay("/exchange");
        brokerRelay.setRelayHost(rabbitProperties.relayHost());
        brokerRelay.setRelayPort(rabbitProperties.relayPort());
        brokerRelay.setClientLogin(rabbitProperties.clientLogin());
        brokerRelay.setClientPasscode(rabbitProperties.clientPasscode());
        brokerRelay.setSystemLogin(rabbitProperties.systemLogin());
        brokerRelay.setSystemPasscode(rabbitProperties.systemPasscode());
        brokerRelay.setVirtualHost(rabbitProperties.virtualHost());
        brokerRelay.setAutoStartup(rabbitProperties.autoStartup());

        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint clients will connect to
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Optional fallback, or can just use raw websocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
