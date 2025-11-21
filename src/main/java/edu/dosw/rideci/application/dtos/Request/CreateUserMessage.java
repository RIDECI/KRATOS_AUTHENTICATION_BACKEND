package edu.dosw.rideci.application.dtos.Request;

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
public class CreateUserMessage implements Serializable {

    private String userAuthId; // ID del UserAuth en el microservicio de Authentication
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
    private Date dateOfBirth;
    private identificationType identificationType;
    private String identificationNumber;
    private String Address;

}