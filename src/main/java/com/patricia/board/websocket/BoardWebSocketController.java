package com.patricia.board.websocket;

import com.patricia.board.dto.CursorMessage;
import com.patricia.board.model.Stroke;
import com.patricia.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class BoardWebSocketController {

    private static final String BROKER_PREFIX = "/exchange";
    private static final String RABBIT_EXCHANGE = "amq.topic";

    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/board/{boardId}/stroke")
    public void handleStroke(@DestinationVariable UUID boardId, @Payload Stroke stroke) {
        // Validation could be added here
        if (stroke == null || stroke.getPoints() == null || stroke.getPoints().isEmpty()) {
            return;
        }
        
        // Add stroke to board state
        boardService.addStroke(boardId, stroke);
        
        // Broadcast the stroke to all subscribers
        messagingTemplate.convertAndSend(brokerDestination(boardId, ""), stroke);
    }

    @MessageMapping("/board/{boardId}/cursor")
    public void handleCursor(@DestinationVariable UUID boardId, @Payload CursorMessage cursorMessage) {
        // Broadcast cursor directly to subscribers without storing
        messagingTemplate.convertAndSend(brokerDestination(boardId, ".cursor"), cursorMessage);
    }

    private String brokerDestination(UUID boardId, String suffix) {
        return BROKER_PREFIX + "/" + RABBIT_EXCHANGE + "/board." + boardId + suffix;
    }
}
