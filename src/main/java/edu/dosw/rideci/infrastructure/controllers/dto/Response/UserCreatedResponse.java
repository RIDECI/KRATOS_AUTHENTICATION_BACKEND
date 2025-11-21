package edu.dosw.rideci.infrastructure.controllers.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Respuesta de UserManagement después de crear el usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedResponse implements Serializable {

    private String userAuthId; // Para saber que UserAuth actualizar
    private Long userId; // El ID del User creado en el microservicio UserManagement
    private boolean success;
    private String message;
}