package com.patricia.board.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared board state, persisted in Mongo (Atlas) so every pod serves the same
 * canvas. Boards idle for 30 days are auto-deleted by the TTL index — a study
 * session's scribbles are session-state, not an archive.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "boards")
public class BoardState {
    @Id
    private UUID boardId;

    private List<Stroke> strokes = new ArrayList<>();

    @Indexed(expireAfter = "30d")
    private Instant lastActivityAt;

    public BoardState(UUID boardId) {
        this.boardId = boardId;
        this.lastActivityAt = Instant.now();
    }

    public BoardState(UUID boardId, List<Stroke> strokes) {
        this(boardId);
        this.strokes = strokes;
    }
}
