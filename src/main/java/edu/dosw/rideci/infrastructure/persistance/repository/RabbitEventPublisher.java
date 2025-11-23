package edu.dosw.rideci.infrastructure.persistance.repository;

import edu.dosw.rideci.application.events.UserEvent;
import edu.dosw.rideci.infrastructure.config.RabbitMQConfig;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import edu.dosw.rideci.application.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(Object event, String routingKey) {
        rabbitTemplate.convertAndSend("user.exchange", routingKey, event);
    }
}
