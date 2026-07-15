package com.patricia.board.service;

import com.mongodb.client.result.DeleteResult;
import com.patricia.board.model.BoardState;
import com.patricia.board.model.Stroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests against a mocked MongoTemplate: they pin the atomic-update
 * semantics (upsert + $push/$set, never load-modify-save) that make the
 * board safe with 2 pods writing concurrently.
 */
class BoardServiceTest {

    private MongoTemplate mongo;
    private BoardService boardService;

    @BeforeEach
    void setUp() {
        mongo = mock(MongoTemplate.class);
        boardService = new BoardService(mongo);
    }

    @Test
    void createBoard_upsertsAndReturnsCustomId() {
        UUID customId = UUID.randomUUID();
        assertEquals(customId, boardService.createBoard(customId));
        verify(mongo).upsert(any(Query.class), any(Update.class), eq(BoardState.class));
    }

    @Test
    void createBoard_generatesIdWhenMissing() {
        assertNotNull(boardService.createBoard());
        verify(mongo).upsert(any(Query.class), any(Update.class), eq(BoardState.class));
    }

    @Test
    void getBoard_returnsDocument() {
        UUID id = UUID.randomUUID();
        BoardState state = new BoardState(id);
        when(mongo.findById(id, BoardState.class)).thenReturn(state);
        assertSame(state, boardService.getBoard(id));
    }

    @Test
    void getBoard_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(mongo.findById(id, BoardState.class)).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> boardService.getBoard(id));
    }

    @Test
    void addStroke_pushesAtomicallyAndFillsDefaults() {
        UUID id = UUID.randomUUID();
        Stroke stroke = new Stroke(null, "#fff", 3, List.of(), null);

        boardService.addStroke(id, stroke);

        assertNotNull(stroke.getId());
        assertNotNull(stroke.getCreatedAt());
        ArgumentCaptor<Update> update = ArgumentCaptor.forClass(Update.class);
        verify(mongo).upsert(any(Query.class), update.capture(), eq(BoardState.class));
        assertTrue(update.getValue().getUpdateObject().containsKey("$push"),
                "stroke must be appended with $push, not a full-document save");
    }

    @Test
    void clearBoard_setsEmptyStrokes() {
        boardService.clearBoard(UUID.randomUUID());
        ArgumentCaptor<Update> update = ArgumentCaptor.forClass(Update.class);
        verify(mongo).upsert(any(Query.class), update.capture(), eq(BoardState.class));
        assertTrue(((org.bson.Document) update.getValue().getUpdateObject().get("$set")).containsKey("strokes"));
    }

    @Test
    void deleteBoard_throwsWhenNothingDeleted() {
        when(mongo.remove(any(Query.class), eq(BoardState.class))).thenReturn(DeleteResult.acknowledged(0));
        assertThrows(NoSuchElementException.class, () -> boardService.deleteBoard(UUID.randomUUID()));
    }

    @Test
    void deleteBoard_succeedsWhenDeleted() {
        when(mongo.remove(any(Query.class), eq(BoardState.class))).thenReturn(DeleteResult.acknowledged(1));
        assertDoesNotThrow(() -> boardService.deleteBoard(UUID.randomUUID()));
    }
}
