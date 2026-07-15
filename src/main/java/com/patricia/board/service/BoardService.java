package com.patricia.board.service;

import com.patricia.board.model.BoardState;
import com.patricia.board.model.Stroke;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Board state backed by Mongo (shared across pods). Writes use atomic
 * upsert + $push/$set so two pods editing the same board never lose each
 * other's strokes to a load-modify-save race, and a stroke/clear against a
 * board this pod has never seen just materializes it instead of throwing —
 * with 2 replicas, "board unknown here" is normal, not an error.
 */
@Service
public class BoardService {

    private final MongoTemplate mongo;

    public BoardService(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public UUID createBoard(UUID customId) {
        UUID boardId = customId != null ? customId : UUID.randomUUID();
        // Upsert, NOT save(): re-creating an existing board (frontend
        // self-heal after a 404 from a stale pod) must never wipe strokes.
        mongo.upsert(byId(boardId), touch(new Update()), BoardState.class);
        return boardId;
    }

    public UUID createBoard() {
        return createBoard(null);
    }

    public BoardState getBoard(UUID boardId) {
        BoardState board = mongo.findById(boardId, BoardState.class);
        if (board == null) {
            throw new NoSuchElementException("Board not found with id: " + boardId);
        }
        return board;
    }

    public void deleteBoard(UUID boardId) {
        if (mongo.remove(byId(boardId), BoardState.class).getDeletedCount() == 0) {
            throw new NoSuchElementException("Board not found with id: " + boardId);
        }
    }

    public void clearBoard(UUID boardId) {
        mongo.upsert(byId(boardId), touch(new Update().set("strokes", List.of())), BoardState.class);
    }

    public void addStroke(UUID boardId, Stroke stroke) {
        if (stroke.getId() == null) {
            stroke.setId(UUID.randomUUID());
        }
        if (stroke.getCreatedAt() == null) {
            stroke.setCreatedAt(Instant.now());
        }
        mongo.upsert(byId(boardId), touch(new Update().push("strokes", stroke)), BoardState.class);
    }

    private static Query byId(UUID boardId) {
        return new Query(Criteria.where("_id").is(boardId));
    }

    /** Refresh the TTL clock on every write. */
    private static Update touch(Update update) {
        return update.set("lastActivityAt", Instant.now());
    }
}
