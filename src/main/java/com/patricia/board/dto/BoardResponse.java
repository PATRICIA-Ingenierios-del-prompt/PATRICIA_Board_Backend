package com.patricia.board.dto;

import com.patricia.board.model.Stroke;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {
    private UUID boardId;
    private List<Stroke> strokes;
}
