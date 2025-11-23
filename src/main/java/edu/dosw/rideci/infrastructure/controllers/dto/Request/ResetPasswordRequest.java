package edu.dosw.rideci.infrastructure.controllers.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el proceso de recuperación de constraseña.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "El token de recuperación no puede estar vacío")
    private String resetToken;
    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String newPassword;
    @NotBlank(message = "El token de recuperación no puede estar vacío")
    private String confirmPassword;
}
