package edu.dosw.rideci.application.events;

import edu.dosw.rideci.domain.models.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO para enviar a UserManagement via RabbitMQ
 * Mensaje para crear un nuevo usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent implements Serializable {

    private Long userId;

    private String name;
    private String email;

    private String identificationType;
    private String identificationNumber;

    private String phoneNumber;
    private String address;

    private String role;
    private String birthOfDate;
}