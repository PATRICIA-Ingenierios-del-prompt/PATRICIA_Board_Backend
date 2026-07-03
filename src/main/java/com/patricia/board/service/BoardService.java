package com.patricia.board.service;

import com.patricia.board.model.BoardState;
import com.patricia.board.model.Stroke;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BoardService {

    private final ConcurrentHashMap<UUID, BoardState> boards = new ConcurrentHashMap<>();

    public UUID createBoard(UUID customId) {
        UUID boardId = customId != null ? customId : UUID.randomUUID();
        boards.put(boardId, new BoardState(boardId));
        return boardId;
    }

    public UUID createBoard() {
        return createBoard(null);
    }

    public BoardState getBoard(UUID boardId) {
        BoardState board = boards.get(boardId);
        if (board == null) {
            throw new NoSuchElementException("Board not found with id: " + boardId);
        }
        return board;
    }

    public void deleteBoard(UUID boardId) {
        if (boards.remove(boardId) == null) {
            throw new NoSuchElementException("Board not found with id: " + boardId);
        }
    }

    public void clearBoard(UUID boardId) {
        BoardState board = getBoard(boardId);
        board.getStrokes().clear();
    }

    public void addStroke(UUID boardId, Stroke stroke) {
        BoardState board = getBoard(boardId);
        // Generates an ID for the stroke if missing, although client usually provides it
        if (stroke.getId() == null) {
            stroke.setId(UUID.randomUUID());
        }
        if (stroke.getCreatedAt() == null) {
            stroke.setCreatedAt(java.time.Instant.now());
        }
        board.getStrokes().add(stroke);
    }
}
