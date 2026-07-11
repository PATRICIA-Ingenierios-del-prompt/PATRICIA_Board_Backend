package com.patricia.board.websocket;

import com.patricia.board.backplane.RedisBackplanePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class BoardBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectProvider<RedisBackplanePublisher> backplane;

    public BoardBroadcaster(SimpMessagingTemplate messagingTemplate,
                            ObjectProvider<RedisBackplanePublisher> backplane) {
        this.messagingTemplate = messagingTemplate;
        this.backplane = backplane;
    }

    public void send(String destination, Object payload) {
        try {
            RedisBackplanePublisher publisher = backplane.getIfAvailable();
            if (publisher != null) {
                publisher.publish(destination, payload);
            } else {
                messagingTemplate.convertAndSend(destination, payload);
            }
        } catch (RuntimeException ex) {
            log.warn("Backplane publish failed for {} -- falling back to local broadcast: {}",
                    destination, ex.getMessage());
            tryLocal(destination, payload);
        }
    }

    private void tryLocal(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (RuntimeException ex) {
            log.warn("Local broadcast also failed for {}: {}", destination, ex.getMessage());
        }
    }
}
