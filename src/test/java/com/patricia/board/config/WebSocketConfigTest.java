package com.patricia.board.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Post-backplane: la configuracion STOMP ya NO usa enableStompBrokerRelay
 * (RabbitMQ STOMP), sino enableSimpleBroker; el fan-out cross-pod lo hace el
 * Redis backplane. Este test verifica el nuevo cableado.
 */
class WebSocketConfigTest {

    @Test
    void testConfigureMessageBroker_registersSimpleBrokerWithLegacyPrefixes() {
        WebSocketConfig config = new WebSocketConfig();
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        // Los 3 prefijos: /exchange preserva compat con el frontend actual
        // (que aun se suscribe a /exchange/amq.topic/board.<id>), /topic y
        // /queue son la convencion estandar de Spring para futuras rutas.
        verify(registry).enableSimpleBroker("/exchange", "/topic", "/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void testRegisterStompEndpoints_addsWsEndpointsWithAndWithoutSockJs() {
        WebSocketConfig config = new WebSocketConfig();
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
        SockJsServiceRegistration sockJsRegistration = mock(SockJsServiceRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(any(String[].class))).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(sockJsRegistration);

        config.registerStompEndpoints(registry);

        verify(registry, times(2)).addEndpoint("/ws");
        verify(registration, times(2)).setAllowedOriginPatterns("*");
        verify(registration, times(1)).withSockJS();
    }
}
