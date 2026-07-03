package com.patricia.board.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stroke {
    private UUID id;
    private String color;
    private Integer width;
    private List<Point> points;
    private Instant createdAt;
}
