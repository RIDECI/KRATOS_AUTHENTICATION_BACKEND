package edu.dosw.rideci.application.events.listener;

import edu.dosw.rideci.application.events.UserSyncFailedEvent;
import edu.dosw.rideci.application.port.out.UserAuthRepositoryOutPort;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Data
@Component
@Slf4j
public class UserSyncErrorListener {

    private UserAuthRepositoryOutPort userAuthRepositoryOutPort;

    public UserSyncErrorListener(UserAuthRepositoryOutPort userAuthRepositoryOutPort) {
        this.userAuthRepositoryOutPort = userAuthRepositoryOutPort;
    }

    @RabbitListener(queues = "auth.sync.queue")
    public void onUserCreateFailed(UserSyncFailedEvent event) {
        log.error("Compensando creación fallida para {}", event.getEmail());

        userAuthRepositoryOutPort.deleteByEmail(event.getEmail());

        log.info("Compensación realizada correctamente");
    }
}

