package com.patricia.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patricia.board.dto.BoardResponse;
import com.patricia.board.dto.CreateBoardResponse;
import com.patricia.board.model.BoardState;
import com.patricia.board.model.Stroke;
import com.patricia.board.service.BoardService;
import com.patricia.board.websocket.BoardBroadcaster;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @MockBean
    private BoardBroadcaster broadcaster;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateBoard() throws Exception {
        UUID boardId = UUID.randomUUID();
        when(boardService.createBoard(any())).thenReturn(boardId);

        mockMvc.perform(post("/api/boards"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(boardId.toString()));

        verify(boardService).createBoard(any());
    }

    @Test
    void shouldGetBoard() throws Exception {
        UUID boardId = UUID.randomUUID();
        BoardState boardState = new BoardState(boardId, new ArrayList<>());
        when(boardService.getBoard(boardId)).thenReturn(boardState);

        mockMvc.perform(get("/api/boards/{boardId}", boardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(boardId.toString()))
                .andExpect(jsonPath("$.strokes").isArray());

        verify(boardService).getBoard(boardId);
    }

    @Test
    void shouldReturn404ForMissingBoard() throws Exception {
        UUID boardId = UUID.randomUUID();
        when(boardService.getBoard(boardId)).thenThrow(new NoSuchElementException("Board not found with id: " + boardId));

        mockMvc.perform(get("/api/boards/{boardId}", boardId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Board not found with id: " + boardId));

        verify(boardService).getBoard(boardId);
    }

    @Test
    void shouldDeleteBoard() throws Exception {
        UUID boardId = UUID.randomUUID();
        doNothing().when(boardService).deleteBoard(boardId);

        mockMvc.perform(delete("/api/boards/{boardId}", boardId))
                .andExpect(status().isNoContent());

        verify(boardService).deleteBoard(boardId);
    }

    @Test
    void shouldReturn404OnDeleteForMissingBoard() throws Exception {
        UUID boardId = UUID.randomUUID();
        doThrow(new NoSuchElementException("Board not found with id: " + boardId))
                .when(boardService).deleteBoard(boardId);

        mockMvc.perform(delete("/api/boards/{boardId}", boardId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404));

        verify(boardService).deleteBoard(boardId);
    }

    @Test
    void shouldClearBoard() throws Exception {
        UUID boardId = UUID.randomUUID();
        doNothing().when(boardService).clearBoard(boardId);

        mockMvc.perform(post("/api/boards/{boardId}/clear", boardId))
                .andExpect(status().isOk());

        verify(boardService).clearBoard(boardId);
        verify(broadcaster).send(eq("/exchange/amq.topic/board." + boardId + ".clear"), eq("CLEAR"));
    }

    @Test
    void shouldReturn404OnClearForMissingBoard() throws Exception {
        UUID boardId = UUID.randomUUID();
        doThrow(new NoSuchElementException("Board not found with id: " + boardId))
                .when(boardService).clearBoard(boardId);

        mockMvc.perform(post("/api/boards/{boardId}/clear", boardId))
                .andExpect(status().isNotFound());

        verify(boardService).clearBoard(boardId);
        verifyNoInteractions(broadcaster);
    }

    @Test
    void shouldHandleInvalidUuidFormat() throws Exception {
        // Because of the catch-all Exception.class handler in GlobalExceptionHandler,
        // invalid UUID path variables (which cause MethodArgumentTypeMismatchException)
        // are returned as 500 Internal Server Error. Let's assert that behavior.
        mockMvc.perform(get("/api/boards/invalid-uuid-format"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
