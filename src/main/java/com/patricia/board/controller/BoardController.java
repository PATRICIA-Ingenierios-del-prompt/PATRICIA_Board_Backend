package com.patricia.board.controller;

import com.patricia.board.dto.BoardResponse;
import com.patricia.board.dto.CreateBoardResponse;
import com.patricia.board.model.BoardState;
import com.patricia.board.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Tag(name = "Board Management", description = "Endpoints for managing collaborative whiteboards")
public class BoardController {

    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @Operation(summary = "Create a new board", description = "Creates a new empty board (optionally with a custom ID) and returns its unique ID.")
    public ResponseEntity<CreateBoardResponse> createBoard(@RequestParam(required = false) UUID customId) {
        UUID boardId = boardService.createBoard(customId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateBoardResponse(boardId));
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "Get a board by ID", description = "Retrieves the current state (all strokes) of the specified board.")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable UUID boardId) {
        BoardState board = boardService.getBoard(boardId);
        return ResponseEntity.ok(new BoardResponse(board.getBoardId(), board.getStrokes()));
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "Delete a board", description = "Completely removes a board from memory.")
    public ResponseEntity<Void> deleteBoard(@PathVariable UUID boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{boardId}/clear")
    @Operation(summary = "Clear a board", description = "Removes all strokes from the specified board and broadcasts a clear event to all active participants.")
    public ResponseEntity<Void> clearBoard(@PathVariable UUID boardId) {
        boardService.clearBoard(boardId);
        // Broadcast clear event to all subscribers
        messagingTemplate.convertAndSend("/topic/board/" + boardId + "/clear", "CLEAR");
        return ResponseEntity.ok().build();
    }
}
