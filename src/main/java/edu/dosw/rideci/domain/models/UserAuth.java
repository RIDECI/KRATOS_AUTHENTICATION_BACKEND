package edu.dosw.rideci.domain.models;

import edu.dosw.rideci.domain.models.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAuth {
    private String id; // MongoDB genera el ID automáticamente

    private Long institutionalId;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private Long userId; // Referencia al ID del User en el microservicio UserManagement
    private LocalDateTime createdAt; // Fecha de creación de la cuenta
    private LocalDateTime lastLogin; // Última vez que el usuario hizo login
}