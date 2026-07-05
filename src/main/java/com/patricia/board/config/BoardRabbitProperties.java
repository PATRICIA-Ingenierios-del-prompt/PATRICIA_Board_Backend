package com.patricia.board.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "board.rabbitmq")
public record BoardRabbitProperties(
        String relayHost,
        int relayPort,
        String clientLogin,
        String clientPasscode,
        String systemLogin,
        String systemPasscode,
        String virtualHost,
        boolean autoStartup
) {
}
