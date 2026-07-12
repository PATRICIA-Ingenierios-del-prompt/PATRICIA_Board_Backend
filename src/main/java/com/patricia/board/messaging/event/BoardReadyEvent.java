package com.patricia.board.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardReadyEvent {
    private UUID parcheId;
    private UUID canvasId;
}
