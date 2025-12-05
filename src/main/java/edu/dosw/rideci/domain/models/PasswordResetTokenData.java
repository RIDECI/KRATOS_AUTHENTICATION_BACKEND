package edu.dosw.rideci.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenData implements Serializable {
    private String email;
    private LocalDateTime createdAt;
    private int attempts;
}
