package com.patricia.board.messaging;

import com.patricia.board.service.BoardService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class ProvisionBoardForParcheUseCase {

    private final Map<UUID, UUID> parcheIdToBoardId = new ConcurrentHashMap<>();
    private final BoardService boardService;

    public ProvisionBoardForParcheUseCase(BoardService boardService) {
        this.boardService = boardService;
    }

    /** @return el boardId (nuevo o el previamente reservado para ese parche). */
    public UUID provision(UUID parcheId) {
        return parcheIdToBoardId.computeIfAbsent(parcheId, id -> boardService.createBoard(null));
    }
}
