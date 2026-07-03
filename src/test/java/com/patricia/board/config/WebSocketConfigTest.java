package com.patricia.board.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;

import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    @Test
    void testConfigureMessageBroker() {
        WebSocketConfig config = new WebSocketConfig();
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic");
        verify(registry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void testRegisterStompEndpoints() {
        WebSocketConfig config = new WebSocketConfig();
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
        SockJsServiceRegistration sockJsRegistration = mock(SockJsServiceRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns("*")).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(sockJsRegistration);

        config.registerStompEndpoints(registry);

        verify(registry, times(2)).addEndpoint("/ws");
        verify(registration, times(2)).setAllowedOriginPatterns("*");
        verify(registration, times(1)).withSockJS();
    }
}
