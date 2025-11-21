package edu.dosw.rideci.application.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendRecoveryEmailMessage {
    private String email;
    private String resetToken;
}
