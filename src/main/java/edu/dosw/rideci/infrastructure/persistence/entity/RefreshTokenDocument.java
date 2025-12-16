package edu.dosw.rideci.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Entidad Redis para gestionar refresh tokens
 * - El access token dura solo 15 minutos
 * - El refresh token dura 3 días con TTL automático en Redis
 * - Cuando el access token se vence, el cliente usa el refresh token
 *   para obtener un nuevo access token SIN tener que hacer login de nuevo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "refreshTokens")
public class RefreshTokenDocument {

    @Id
    private String id;
    @Indexed
    private String token;
    @Indexed
    private String userAuthId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    @TimeToLive
    private Long ttl;
}