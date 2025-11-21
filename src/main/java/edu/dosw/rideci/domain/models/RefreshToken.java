package edu.dosw.rideci.domain.models;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
    private String id; // ID generado por MongoDB
    private String token; // El refresh token JWT
    private String userAuthId; // Referencia al ID de UserAuth
    private LocalDateTime expiresAt; // Fecha de expiración
    private LocalDateTime createdAt; // Fecha de creación del token
}
