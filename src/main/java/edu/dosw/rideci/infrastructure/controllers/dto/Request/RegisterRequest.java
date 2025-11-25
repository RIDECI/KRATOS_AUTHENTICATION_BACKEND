package edu.dosw.rideci.infrastructure.controllers.dto.Request;

import edu.dosw.rideci.domain.models.enums.Role;
import edu.dosw.rideci.domain.models.enums.identificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO para el registro de usuarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@(escuelaing\\.edu\\.co|mail\\.escuelaing\\.edu\\.co)$",
            message = "El correo debe ser de los dominios @escuelaing.edu.co o @mail.escuelaing.edu.co"
    )
    @Schema(
            example = "usuario@mail.escuelaing.edu.co"
    )
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, al menos una mayúscula y un carácter especial"
    )
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phoneNumber;

    @NotNull(message = "El rol es obligatorio")
    private Role role; // STUDENT, PROFESSOR, ADMIN
    @NotNull(message = "El tipo de identificación es obligatorio")
    private identificationType identificationType;
    @NotNull(message = "El número de identificación es obligatorio")
    private String identificationNumber;
    @NotNull(message = "La dirección es obligatoria")
    private String Address;
    @NotNull(message = "El carnet institucional es obligatorio")
    private Long institutionalId;
}
