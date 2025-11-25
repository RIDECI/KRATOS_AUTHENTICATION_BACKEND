package edu.dosw.rideci.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ
 * Define colas, exchanges y routing keys DIRECTAMENTE en código
 */
@Configuration
public class RabbitMQConfig {

    public static final String USER_EXCHANGE = "user.exchange";

    public static final String USER_CREATE_QUEUE = "user.create.queue";

    public static final String USER_CREATE_ROUTING_KEY = "auth.user.create";

    @Bean
    public Queue userCreateQueue() {
        return new Queue(USER_CREATE_QUEUE, true); // durable = true
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }


    @Bean
    public Binding userCreateBinding(Queue userCreateQueue, TopicExchange userExchange) {
        return BindingBuilder
                .bind(userCreateQueue)
                .to(userExchange)
                .with(USER_CREATE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
