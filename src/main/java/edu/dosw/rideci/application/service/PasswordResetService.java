package edu.dosw.rideci.application.service;

import edu.dosw.rideci.application.events.PasswordResetEvent;
import edu.dosw.rideci.application.port.in.ResetPasswordUseCase;
import edu.dosw.rideci.application.port.out.PasswordResetOutPort;
import edu.dosw.rideci.application.port.out.UserAuthRepositoryOutPort;
import edu.dosw.rideci.domain.models.PasswordResetTokenData;
import edu.dosw.rideci.domain.models.UserAuth;
import edu.dosw.rideci.exceptions.AuthException;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.ForgotPasswordRequest;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.ResetPasswordRequest;
import edu.dosw.rideci.infrastructure.persistence.repository.RabbitEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService implements ResetPasswordUseCase {

    private final UserAuthRepositoryOutPort userAuthRepository;
    private final PasswordResetOutPort resetCachePort;
    private final RabbitEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_LENGTH = 8;
    private static final int EXPIRATION_MINUTES = 15;
    private static final int MAX_ATTEMPTS_PER_HOUR = 3;
    private static final int MAX_VALIDATION_ATTEMPTS = 5;

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request){
        log.info("Solicitud de restablecimiento de contraseña para: {}", request.getEmail());

        validateRateLimit(request.getEmail());

        UserAuth userAuth = userAuthRepository.findByEmail(request.getEmail()).orElse(null);
        if (userAuth == null){
            log.warn("Correo: " + request.getEmail() + " no encontrado");
            return;
        }

        String resetToken = generateResetToken();

        PasswordResetTokenData tokenData = PasswordResetTokenData.builder()
                .email(request.getEmail())
                .createdAt(LocalDateTime.now())
                .attempts(0).build();

        resetCachePort.saveResetToken(resetToken, tokenData, EXPIRATION_MINUTES);
        resetCachePort.saveResetAttempt(request.getEmail());

        publishResetEvent(request.getEmail(), resetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request){
        log.info("Proceso de reseteo de contraseña");

        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new AuthException("Las contraseñas no coinciden");
        }

        PasswordResetTokenData tokenData = validateAndGetTokenData(request.getResetToken());

        if (tokenData.getAttempts() >= MAX_ATTEMPTS_PER_HOUR){
            log.info("Limite de intentos alcanzado");
            resetCachePort.deleteResetToken(request.getResetToken());
            throw new AuthException("Limite de intentos alcanzado");
        }

        resetCachePort.incrementAttempts(request.getResetToken());

        UserAuth userAuth = userAuthRepository.findByEmail(tokenData.getEmail())
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        userAuth.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userAuthRepository.save(userAuth);

        resetCachePort.deleteResetToken(request.getResetToken());

        log.info("Contraseña actualizada correctamente para {}", tokenData.getEmail());
    }

    private PasswordResetTokenData validateAndGetTokenData(String token) {

        PasswordResetTokenData tokenData = resetCachePort.getResetToken(token)
                .orElseThrow(() -> {
                    log.error("Token de reset no encontrado o expirado");
                    return new AuthException("Token inválido o expirado");
                });

        if (tokenData.getAttempts() >= MAX_VALIDATION_ATTEMPTS) {
            log.error("Demasiados intentos de validación para el token");
            resetCachePort.deleteResetToken(token);
            throw new AuthException("Token bloqueado por exceso de intentos");
        }

        resetCachePort.incrementAttempts(token);

        return tokenData;
    }


    private void validateRateLimit(String email) {
        long attempts = resetCachePort.countResetAttempts(email);

        if (attempts >= MAX_ATTEMPTS_PER_HOUR) {
            log.warn("Rate limit excedido para: {}", email);
            throw new AuthException(
                    String.format("Demasiados intentos. Intenta en 1 hora (%d/%d)",
                            attempts, MAX_ATTEMPTS_PER_HOUR)
            );
        }
    }

    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(characters.charAt(random.nextInt(characters.length())));
        }

        return token.toString();
    }

    private void publishResetEvent(String email, String resetToken) {
        try {
            PasswordResetEvent event = PasswordResetEvent.builder()
                    .email(email)
                    .resetCode(resetToken)
                    .expiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                    .expiryMinutes(EXPIRATION_MINUTES)
                    .build();

            eventPublisher.publish(event, "auth.user.resetPassword");
            log.info("Evento de reset publicado para: {}", email);

        } catch (Exception e) {
            log.error("Error al publicar evento: {}", e.getMessage());
            resetCachePort.deleteResetToken(resetToken);
            throw new AuthException("Error al procesar solicitud de recuperación");
        }
    }
}
