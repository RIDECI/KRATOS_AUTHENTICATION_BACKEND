package edu.dosw.rideci.infrastructure.controllers.dto.Request;

import edu.dosw.rideci.domain.models.enums.Role;
import edu.dosw.rideci.domain.models.enums.identificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phoneNumber;

    @NotNull(message = "El rol es obligatorio")
    private Role role; // STUDENT, PROFESSOR, ADMIN
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private Date dateOfBirth;
    @NotNull(message = "El tipo de identificación es obligatorio")
    private identificationType identificationType;
    @NotNull(message = "El número de identificación es obligatorio")
    private String identificationNumber;
    @NotNull(message = "La dirección es obligatoria")
    private String Address;
}
