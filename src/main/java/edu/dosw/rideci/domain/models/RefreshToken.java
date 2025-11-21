package edu.dosw.rideci.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento MongoDB para gestionar refresh tokens
 * - El access token dura solo 15 minutos
 * - El refresh token dura 7 días
 * - Cuando el access token se vence, el cliente usa el refresh token
 *   para obtener un nuevo access token SIN tener que hacer login de nuevo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refreshTokens")
public class RefreshToken {

    @Id
    private String id; // ID generado por MongoDB

    @Indexed(unique = true)
    private String token; // El refresh token JWT

    private String userAuthId; // Referencia al ID de UserAuth

    private LocalDateTime expiresAt; // Fecha de expiración

    private LocalDateTime createdAt; // Fecha de creación del token
}
