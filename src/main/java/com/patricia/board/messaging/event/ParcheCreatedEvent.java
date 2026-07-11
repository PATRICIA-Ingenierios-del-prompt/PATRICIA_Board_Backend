package com.patricia.board.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcheCreatedEvent {
    private UUID sourceEventId;
    private UUID parcheId;
    private String name;
    private String visibility;
    private UUID ownerId;
}
