package com.patricia.board.messaging;

import com.patricia.board.messaging.event.BoardReadyEvent;
import com.patricia.board.messaging.event.ParcheCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
public class ParcheCreatedListener {

    private final ProvisionBoardForParcheUseCase provisionUseCase;
    private final RabbitTemplate rabbitTemplate;

    public ParcheCreatedListener(ProvisionBoardForParcheUseCase provisionUseCase, RabbitTemplate rabbitTemplate) {
        this.provisionUseCase = provisionUseCase;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitProvisioningConfig.PARCHE_CREATED_QUEUE)
    public void onParcheCreated(ParcheCreatedEvent event) {
        UUID parcheId = event.getParcheId();
        if (parcheId == null) {
            log.warn("parche.created recibido sin parcheId -- descartando");
            return;
        }
        UUID boardId = provisionUseCase.provision(parcheId);
        rabbitTemplate.convertAndSend(
                RabbitProvisioningConfig.PARCHE_EVENTS_EXCHANGE,
                RabbitProvisioningConfig.BOARD_READY_ROUTING_KEY,
                new BoardReadyEvent(parcheId, boardId));
        log.info("Provisioned boardId={} for parcheId={}", boardId, parcheId);
    }
}
