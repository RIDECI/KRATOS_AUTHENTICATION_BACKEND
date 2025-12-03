package edu.dosw.rideci.infrastructure.controllers.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@(escuelaing\\.edu\\.co|mail\\.escuelaing\\.edu\\.co)$",
            message = "El correo debe ser de los dominios @escuelaing.edu.co o @mail.escuelaing.edu.co"
    )
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
