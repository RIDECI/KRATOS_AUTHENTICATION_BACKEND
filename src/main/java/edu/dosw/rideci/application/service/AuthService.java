package edu.dosw.rideci.application.service;

import edu.dosw.rideci.application.events.UserEvent;
import edu.dosw.rideci.application.port.in.LoginUserUseCase;
import edu.dosw.rideci.application.port.in.RegisterUserUseCase;
import edu.dosw.rideci.application.port.out.RefreshTokenRepositoryOutPort;
import edu.dosw.rideci.application.port.out.TokenProviderOutPort;
import edu.dosw.rideci.application.port.out.UserAuthRepositoryOutPort;
import edu.dosw.rideci.domain.models.RefreshToken;
import edu.dosw.rideci.domain.models.enums.AccountState;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.*;
import edu.dosw.rideci.infrastructure.controllers.dto.Response.AuthResponse;
import edu.dosw.rideci.infrastructure.controllers.dto.Response.UserResponse;
import edu.dosw.rideci.domain.models.UserAuth;
import edu.dosw.rideci.exceptions.AuthException;
import edu.dosw.rideci.infrastructure.persistence.repository.RabbitEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de Autenticación
 * Maneja: Register, Login, Refresh Token, Switch Profile
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements LoginUserUseCase, RegisterUserUseCase {

    private final UserAuthRepositoryOutPort userAuthRepositoryOutPort;
    private final RefreshTokenRepositoryOutPort refreshTokenRepositoryOutPort;
    private final RabbitEventPublisher eventPublisher;
    private final TokenProviderOutPort tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        log.info("Iniciando registro para email: {}", request.getEmail());

        // 1. Validar que el email no exista
        if (userAuthRepositoryOutPort.existsByEmail(request.getEmail())) {
            log.error("Email ya registrado: {}", request.getEmail());
            throw new AuthException("El email ya está registrado");
        }

        // 2. Hashear la contraseña
        String passwordHash = passwordEncoder.encode(request.getPassword());
        log.debug("Contraseña hasheada correctamente");

        // 3. Crear UserAuth (credenciales)
        UserAuth userAuth = UserAuth.builder()
                .email(request.getEmail())
                .institutionalId(request.getInstitutionalId())
                .passwordHash(passwordHash)
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();
        UserAuth savedUserAuth = userAuthRepositoryOutPort.save(userAuth);
        log.info("UserAuth creado con ID: {}", savedUserAuth.getId());

        // 4. Publicar en RabbitMQ
        try {
            UserEvent message = UserEvent.builder()
                    .userId(request.getInstitutionalId())
                    .name(request.getName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .role(request.getRole().toString())
                    .identificationType(request.getIdentificationType().toString())
                    .identificationNumber(request.getIdentificationNumber())
                    .address(request.getAddress())
                    .build();
            eventPublisher.publish(message,"auth.user.create");
            log.info("Mensaje publicado a RabbitMQ para crear usuario");

        } catch (Exception e) {
            log.error("Error al publicar mensaje en RabbitMQ: {}", e.getMessage());
            userAuthRepositoryOutPort.delete(savedUserAuth);
            throw new AuthException("Error al crear el usuario: " + e.getMessage());
        }

        log.info("Registro exitoso para: {}", request.getEmail());

        return UserResponse.builder()
                .userId(request.getInstitutionalId())
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .createdAt(LocalDateTime.now())
                .role(request.getRole())
                .state(AccountState.PENDING)
                .build();
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para: {}", request.getEmail());

        // 1. Buscar usuario por email
        UserAuth userAuth = userAuthRepositoryOutPort.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", request.getEmail());
                    return new AuthException("Email no registrado");
                });

        // 2. Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), userAuth.getPasswordHash())) {
            log.error("Contraseña incorrecta para: {}", request.getEmail());
            throw new AuthException("Contraseña incorrecta");
        }

        // 3. Actualizar lastLogin
        userAuth.setLastLogin(LocalDateTime.now());
        userAuthRepositoryOutPort.save(userAuth);

        // 4. Generar tokens JWT
        String accessToken = tokenProvider.generateAccessToken(
                userAuth.getEmail(),
                userAuth.getRole().toString(),
                userAuth.getInstitutionalId()
        );

        String refreshToken = tokenProvider.generateRefreshToken(
                userAuth.getEmail(),
                userAuth.getInstitutionalId()
        );

        saveRefreshToken(refreshToken, userAuth.getId());

        log.info("Login exitoso para: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(15 * 60L)
                .institutionalId(userAuth.getInstitutionalId())
                .build();
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenString) {
        log.info("Renovando access token");

        // 1. Validar el refresh token
        if (!tokenProvider.isTokenValid(refreshTokenString)) {
            log.error("Refresh token inválido");
            throw new AuthException("Refresh token inválido o expirado");
        }

        if (!tokenProvider.isRefreshToken(refreshTokenString)) {
            log.error("El token no es un refresh token");
            throw new AuthException("El token proporcionado no es un refresh token");
        }

        // 2. Buscar refresh token en BD
        RefreshToken refreshToken = refreshTokenRepositoryOutPort.findByToken(refreshTokenString)
                .orElseThrow(() -> {
                    log.error("Refresh token no encontrado en BD");
                    return new AuthException("Refresh token no válido");
                });

        // 3. Verificar expiración
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.error("Refresh token expirado");
            refreshTokenRepositoryOutPort.deleteByToken(refreshToken);
            throw new AuthException("Refresh token expirado");
        }

        // 4. Obtener datos del usuario
        UserAuth userAuth = userAuthRepositoryOutPort.findById(refreshToken.getUserAuthId())
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para refresh token");
                    return new AuthException("Usuario no encontrado");
                });

        // 5. Generar nuevo access token
        String newAccessToken = tokenProvider.generateAccessToken(
                userAuth.getEmail(),
                userAuth.getRole().toString(),
                userAuth.getInstitutionalId()
        );

        log.info("Access token renovado exitosamente para: {}", userAuth.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenString) // El mismo refresh token
                .tokenType("Bearer")
                .expiresIn(15 * 60L)
                .build();
    }

    private void saveRefreshToken(String token, String userAuthId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userAuthId(userAuthId)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7 días
                .build();

        refreshTokenRepositoryOutPort.save(refreshToken);
        log.debug("Refresh token guardado en BD");
    }
}
