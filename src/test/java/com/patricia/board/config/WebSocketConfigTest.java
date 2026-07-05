package com.patricia.board.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.StompBrokerRelayRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;

import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    @Test
    void testConfigureMessageBroker() {
        BoardRabbitProperties properties = new BoardRabbitProperties(
                "localhost",
                61613,
                "guest",
                "guest",
                "guest",
                "guest",
                "/",
                true
        );
        WebSocketConfig config = new WebSocketConfig(properties);
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);
        StompBrokerRelayRegistration relayRegistration = mock(StompBrokerRelayRegistration.class);

        when(registry.enableStompBrokerRelay("/exchange")).thenReturn(relayRegistration);
        when(relayRegistration.setRelayHost(anyString())).thenReturn(relayRegistration);
        when(relayRegistration.setRelayPort(anyInt())).thenReturn(relayRegistration);
        when(relayRegistration.setClientLogin(anyString())).thenReturn(relayRegistration);
        when(relayRegistration.setClientPasscode(anyString())).thenReturn(relayRegistration);
        when(relayRegistration.setSystemLogin(anyString())).thenReturn(relayRegistration);
        when(relayRegistration.setSystemPasscode(anyString())).thenReturn(relayRegistration);
        when(relayRegistration.setVirtualHost(anyString())).thenReturn(relayRegistration);

        config.configureMessageBroker(registry);

        verify(registry).enableStompBrokerRelay("/exchange");
        verify(relayRegistration).setRelayHost("localhost");
        verify(relayRegistration).setRelayPort(61613);
        verify(relayRegistration).setClientLogin("guest");
        verify(relayRegistration).setClientPasscode("guest");
        verify(relayRegistration).setSystemLogin("guest");
        verify(relayRegistration).setSystemPasscode("guest");
        verify(relayRegistration).setVirtualHost("/");
        verify(registry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void testRegisterStompEndpoints() {
        WebSocketConfig config = new WebSocketConfig(new BoardRabbitProperties(
                "localhost",
                61613,
                "guest",
                "guest",
                "guest",
                "guest",
                "/",
                true
        ));
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
