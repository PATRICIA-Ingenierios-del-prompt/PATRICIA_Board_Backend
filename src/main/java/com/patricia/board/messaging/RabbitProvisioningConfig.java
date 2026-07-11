package com.patricia.board.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitProvisioningConfig {

    public static final String PARCHE_EVENTS_EXCHANGE = "parche.events";

    public static final String PARCHE_CREATED_ROUTING_KEY = "parche.created";
    public static final String BOARD_READY_ROUTING_KEY    = "parche.board.ready";

    public static final String PARCHE_CREATED_QUEUE = "board.parche.created.queue";

    @Bean
    public TopicExchange parcheEventsExchange() {
        // Ya lo declaro Parches como durable/non-auto-delete. Aca solo se
        // declara para bindear la cola. Los flags deben coincidir con Parches
        // o RabbitMQ rechaza la redeclaracion.
        return new TopicExchange(PARCHE_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue parcheCreatedQueue() {
        return QueueBuilder.durable(PARCHE_CREATED_QUEUE).build();
    }

    @Bean
    public Binding parcheCreatedBinding() {
        return BindingBuilder
                .bind(parcheCreatedQueue())
                .to(parcheEventsExchange())
                .with(PARCHE_CREATED_ROUTING_KEY);
    }

    /**
     * Parches usa TypePrecedence.INFERRED asi que NO necesita el header
     * __TypeId__: alcanza con que el JSON tenga los mismos campos que el DTO
     * receptor. Aqui aplicamos la misma configuracion para consistencia.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
