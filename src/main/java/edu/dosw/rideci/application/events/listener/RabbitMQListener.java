package edu.dosw.rideci.application.events.listener;

import edu.dosw.rideci.application.port.out.UserAuthRepositoryOutPort;
import edu.dosw.rideci.infrastructure.config.RabbitMQConfig;
import edu.dosw.rideci.infrastructure.controllers.dto.Response.UserCreatedResponse;
import edu.dosw.rideci.infrastructure.persistance.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener para recibir mensajes de RabbitMQ
 * Escucha respuestas del microservicio UserManagement
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQListener {

    private final UserAuthRepositoryOutPort userAuthRepositoryOutPort;

    /**
     * Escucha la cola de respuestas cuando un usuario es creado en UserManagement
     * Usa la constante directamente desde RabbitMQConfig
     */
    @RabbitListener(queues = RabbitMQConfig.USER_CREATED_RESPONSE_QUEUE)
    public void handleUserCreatedResponse(UserCreatedResponse response) {
        log.info("Recibido mensaje de usuario creado: userAuthId={}, userId={}",
                response.getUserAuthId(), response.getUserId());

        if (response.isSuccess()) {
            userAuthRepositoryOutPort.findById(response.getUserAuthId())
                    .ifPresent(userAuth -> {
                        userAuth.setUserId(response.getUserId());
                        userAuthRepositoryOutPort.save(userAuth);
                        log.info("UserAuth actualizado con userId: {}", response.getUserId());
                    });
        } else {
            log.error("Error al crear usuario en UserManagement: {}", response.getMessage());
        }
    }
}