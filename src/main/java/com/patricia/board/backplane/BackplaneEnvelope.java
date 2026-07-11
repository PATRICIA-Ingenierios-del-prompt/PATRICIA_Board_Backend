package com.patricia.board.backplane;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Mensaje que viaja por el canal Redis del backplane.
 *
 * Contrato COMPARTIDO con los demas MS de sockets: cada mensaje lleva el
 * destino STOMP completo y el payload como JSON crudo. El relay de cada pod
 * lo reenvia tal cual a su broker local, por lo que el formato del payload
 * es exactamente el que los clientes ya reciben hoy -- el backplane no lo
 * transforma. Ver PATRICIA_Backplane_Spec.txt.
 */
public record BackplaneEnvelope(String destination, JsonNode payload) {
}
