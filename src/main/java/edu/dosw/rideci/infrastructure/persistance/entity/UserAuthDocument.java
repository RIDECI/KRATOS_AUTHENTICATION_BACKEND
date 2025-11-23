package edu.dosw.rideci.infrastructure.persistance.entity;

import edu.dosw.rideci.domain.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento MongoDB que almacena las credenciales de autenticación
 * Este documento se guarda en la colección "user_auth"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "userAuth")
public class UserAuthDocument {

    @Id
    private String id; // MongoDB genera el ID automáticamente
    @Indexed(unique = true)
    private String email;
    private String passwordHash;
    private Role role;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}