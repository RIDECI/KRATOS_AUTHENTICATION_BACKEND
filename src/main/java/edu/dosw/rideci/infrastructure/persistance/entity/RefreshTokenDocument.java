package edu.dosw.rideci.infrastructure.persistance.entity;

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
public class RefreshTokenDocument {

    @Id
    private String id;
    @Indexed(unique = true)
    private String token;
    private String userAuthId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
