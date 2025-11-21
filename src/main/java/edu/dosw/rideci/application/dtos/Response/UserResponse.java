package edu.dosw.rideci.application.dtos.Response;

import edu.dosw.rideci.domain.models.enums.AccountState;
import edu.dosw.rideci.domain.models.enums.Profile;
import edu.dosw.rideci.domain.models.enums.Role;
import edu.dosw.rideci.domain.models.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa un User del microservicio UserManagement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private Role role;
    private AccountState state;
}
