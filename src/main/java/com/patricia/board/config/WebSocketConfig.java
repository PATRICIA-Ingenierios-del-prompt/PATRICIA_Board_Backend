package com.patricia.board.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP con simple broker (in-JVM). ANTES: enableStompBrokerRelay contra el
 * plugin STOMP de RabbitMQ. Se cambio porque Amazon MQ NO permite el plugin
 * STOMP (v3 §6.6): el fan-out cross-pod ahora lo hace el backplane Redis
 * (paquete {@code com.patricia.board.backplane}), no RabbitMQ.
 *
 * El PREFIJO se dejo en "/exchange" (ademas de /topic y /queue) para que los
 * paths existentes que arrancan con /exchange/amq.topic/board.* SIGAN
 * funcionando y no haya que tocar el frontend. Para el simple broker el
 * prefijo es solo un namespace de subscripcion, no hay routing real detras.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/exchange", "/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/board")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        registry.addEndpoint("/ws/board")
                .setAllowedOriginPatterns("*");
    }
}
