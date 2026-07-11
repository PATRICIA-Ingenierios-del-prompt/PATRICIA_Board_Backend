package com.patricia.board.backplane;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion del backplane de sockets (Redis pub/sub, Cluster #2).
 *
 * Redis SEPARADO al que se use para cache/estado: este cluster lo COMPARTEN
 * todos los MS con sockets (Location, Notification, Communication, Parques,
 * Board) y solo transporta broadcasts efimeros entre pods. Cada MS publica y
 * se suscribe unicamente a SU canal, namespaced por servicio:
 * {@code patricia:backplane:<servicio>}.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "backplane")
public class BackplaneProperties {

    /** Apagado por defecto: un solo pod no necesita backplane (dev local). */
    private boolean enabled = false;

    /** Canal propio de este MS dentro del cluster compartido. */
    private String channel = "patricia:backplane:board";

    private Redis redis = new Redis();

    @Getter
    @Setter
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
    }
}
