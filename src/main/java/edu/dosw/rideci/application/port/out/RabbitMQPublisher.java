package edu.dosw.rideci.application.port.out;

import edu.dosw.rideci.infrastructure.config.RabbitMQConfig;
import edu.dosw.rideci.application.events.CreateUserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para publicar mensajes en RabbitMQ
 * Usa constantes de RabbitMQConfig directamente
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica un mensaje para crear un usuario en UserManagement
     */
    public void publishCreateUserMessage(CreateUserMessage message) {
        log.info("Publicando mensaje para crear usuario: {}", message.getEmail());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_EXCHANGE,
                    RabbitMQConfig.USER_CREATE_ROUTING_KEY,
                    message
            );
            log.info("Mensaje publicado exitosamente a RabbitMQ");
        } catch (Exception e) {
            log.error("Error al publicar mensaje en RabbitMQ: {}", e.getMessage(), e);
            throw new RuntimeException("Error al comunicarse con UserManagement", e);
        }
    }
}