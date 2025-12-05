package edu.dosw.rideci.application.port.out;

import edu.dosw.rideci.domain.models.PasswordResetTokenData;

import java.util.Optional;

public interface PasswordResetOutPort {
    void saveResetToken(String token, PasswordResetTokenData data, long expirationMinutes);
    Optional<PasswordResetTokenData> getResetToken(String token);
    void deleteResetToken(String token);
    void incrementAttempts(String token);
    void saveResetAttempt(String email);
    long countResetAttempts(String email);
}
