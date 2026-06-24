package com.patricia.board.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardState {
    private UUID boardId;
    // Using CopyOnWriteArrayList for thread safety during reads/writes of strokes
    private List<Stroke> strokes = new CopyOnWriteArrayList<>();
    
    public BoardState(UUID boardId) {
        this.boardId = boardId;
    }
}
