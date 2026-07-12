package com.patricia.board.websocket;

import com.patricia.board.dto.CursorMessage;
import com.patricia.board.model.Point;
import com.patricia.board.model.Stroke;
import com.patricia.board.service.BoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class BoardWebSocketControllerTest {

    private BoardService boardService;
    private BoardBroadcaster broadcaster;
    private BoardWebSocketController controller;

    @BeforeEach
    void setUp() {
        boardService = mock(BoardService.class);
        broadcaster = mock(BoardBroadcaster.class);
        controller = new BoardWebSocketController(boardService, broadcaster);
    }

    @Test
    void shouldReturnEarlyWhenStrokeIsNull() {
        UUID boardId = UUID.randomUUID();
        controller.handleStroke(boardId, null);

        verifyNoInteractions(boardService);
        verifyNoInteractions(broadcaster);
    }

    @Test
    void shouldReturnEarlyWhenStrokePointsIsNull() {
        UUID boardId = UUID.randomUUID();
        Stroke stroke = new Stroke();
        stroke.setPoints(null);

        controller.handleStroke(boardId, stroke);

        verifyNoInteractions(boardService);
        verifyNoInteractions(broadcaster);
    }

    @Test
    void shouldReturnEarlyWhenStrokePointsIsEmpty() {
        UUID boardId = UUID.randomUUID();
        Stroke stroke = new Stroke();
        stroke.setPoints(new ArrayList<>());

        controller.handleStroke(boardId, stroke);

        verifyNoInteractions(boardService);
        verifyNoInteractions(broadcaster);
    }

    @Test
    void shouldProcessAndBroadcastValidStroke() {
        UUID boardId = UUID.randomUUID();
        Stroke stroke = new Stroke();
        stroke.setPoints(List.of(new Point(1.0, 2.0)));

        controller.handleStroke(boardId, stroke);

        verify(boardService).addStroke(boardId, stroke);
        verify(broadcaster).send("/exchange/amq.topic/board." + boardId, stroke);
    }

    @Test
    void shouldBroadcastCursorMessage() {
        UUID boardId = UUID.randomUUID();
        CursorMessage cursor = new CursorMessage("user1", 10.5, 20.5);

        controller.handleCursor(boardId, cursor);

        verify(broadcaster).send("/exchange/amq.topic/board." + boardId + ".cursor", cursor);
        verifyNoInteractions(boardService);
    }
}
