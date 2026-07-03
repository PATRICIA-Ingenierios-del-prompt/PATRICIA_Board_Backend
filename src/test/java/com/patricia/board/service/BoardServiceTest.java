package com.patricia.board.service;

import com.patricia.board.model.BoardState;
import com.patricia.board.model.Point;
import com.patricia.board.model.Stroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BoardServiceTest {

    private BoardService boardService;

    @BeforeEach
    void setUp() {
        boardService = new BoardService();
    }

    @Test
    void shouldCreateBoard() {
        UUID boardId = boardService.createBoard();
        assertNotNull(boardId);

        BoardState board = boardService.getBoard(boardId);
        assertNotNull(board);
        assertEquals(boardId, board.getBoardId());
        assertTrue(board.getStrokes().isEmpty());
    }

    @Test
    void shouldThrowWhenBoardDoesNotExist() {
        UUID randomId = UUID.randomUUID();
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> boardService.getBoard(randomId));
        assertEquals("Board not found with id: " + randomId, ex.getMessage());
    }

    @Test
    void shouldDeleteBoard() {
        UUID boardId = boardService.createBoard();
        assertNotNull(boardService.getBoard(boardId));

        boardService.deleteBoard(boardId);
        assertThrows(NoSuchElementException.class, () -> boardService.getBoard(boardId));
    }

    @Test
    void shouldThrowOnDeleteWhenBoardDoesNotExist() {
        UUID randomId = UUID.randomUUID();
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> boardService.deleteBoard(randomId));
        assertEquals("Board not found with id: " + randomId, ex.getMessage());
    }

    @Test
    void shouldClearBoard() {
        UUID boardId = boardService.createBoard();
        Stroke stroke = new Stroke();
        stroke.setPoints(List.of(new Point(1.0, 2.0)));
        boardService.addStroke(boardId, stroke);

        assertFalse(boardService.getBoard(boardId).getStrokes().isEmpty());

        boardService.clearBoard(boardId);
        assertTrue(boardService.getBoard(boardId).getStrokes().isEmpty());
    }

    @Test
    void shouldThrowOnClearWhenBoardDoesNotExist() {
        UUID randomId = UUID.randomUUID();
        assertThrows(NoSuchElementException.class, () -> boardService.clearBoard(randomId));
    }

    @Test
    void shouldAddStrokeWithGeneratedFields() {
        UUID boardId = boardService.createBoard();
        Stroke stroke = new Stroke();
        stroke.setPoints(new ArrayList<>());

        boardService.addStroke(boardId, stroke);

        BoardState board = boardService.getBoard(boardId);
        assertEquals(1, board.getStrokes().size());
        
        Stroke addedStroke = board.getStrokes().get(0);
        assertNotNull(addedStroke.getId());
        assertNotNull(addedStroke.getCreatedAt());
        assertSame(stroke, addedStroke);
    }

    @Test
    void shouldAddStrokeWithPresetFields() {
        UUID boardId = boardService.createBoard();
        UUID strokeId = UUID.randomUUID();
        Instant timestamp = Instant.now().minusSeconds(10);
        Stroke stroke = new Stroke(strokeId, "#FF0000", 5, List.of(new Point(0.0, 0.0)), timestamp);

        boardService.addStroke(boardId, stroke);

        BoardState board = boardService.getBoard(boardId);
        assertEquals(1, board.getStrokes().size());

        Stroke addedStroke = board.getStrokes().get(0);
        assertEquals(strokeId, addedStroke.getId());
        assertEquals(timestamp, addedStroke.getCreatedAt());
        assertEquals("#FF0000", addedStroke.getColor());
        assertEquals(5, addedStroke.getWidth());
    }

    @Test
    void shouldHandleConcurrentModifications() throws InterruptedException {
        UUID boardId = boardService.createBoard();
        int threads = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    Stroke stroke = new Stroke();
                    stroke.setPoints(List.of(new Point((double) j, (double) j)));
                    boardService.addStroke(boardId, stroke);
                    boardService.getBoard(boardId).getStrokes();
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        BoardState board = boardService.getBoard(boardId);
        assertEquals(threads * operationsPerThread, board.getStrokes().size());
    }
}
