package edu.dosw.rideci.domain.services.impl;

import edu.dosw.rideci.application.dtos.Request.*;
import edu.dosw.rideci.application.dtos.Response.*;
import edu.dosw.rideci.domain.models.enums.Profile;
import edu.dosw.rideci.domain.models.RefreshToken;
import edu.dosw.rideci.domain.models.UserAuth;
import edu.dosw.rideci.application.exceptions.AuthException;
import edu.dosw.rideci.domain.repositories.*;
import edu.dosw.rideci.infrastructure.messaging.publisher.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
public class AuthService {

    private final UserAuthRepository userAuthRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RabbitMQPublisher rabbitMQPublisher;
    private final JWTService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Iniciando registro para email: {}", request.getEmail());

        // 1. Validar que el email no exista
        if (userAuthRepository.existsByEmail(request.getEmail())) {
            log.error("Email ya registrado: {}", request.getEmail());
            throw new AuthException("El email ya está registrado");
        }

        // 2. Hashear la contraseña
        String passwordHash = passwordEncoder.encode(request.getPassword());
        log.debug("Contraseña hasheada correctamente");

        // 3. Crear UserAuth (credenciales)
        UserAuth userAuth = UserAuth.builder()
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .userId(null) // Se actualizará cuando UserManagement responda
                .build();

        userAuth = userAuthRepository.save(userAuth);
        log.info("UserAuth creado con ID: {}", userAuth.getId());

        // 4. Publicar mensaje a RabbitMQ para crear User en microservicio UserManagement
        try {
            CreateUserMessage message = CreateUserMessage.builder()
                    .userAuthId(userAuth.getId())
                    .name(request.getName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .role(request.getRole())
                    .build();

            rabbitMQPublisher.publishCreateUserMessage(message);
            log.info("Mensaje publicado a RabbitMQ para crear usuario");

        } catch (Exception e) {
            log.error("Error al publicar mensaje en RabbitMQ: {}", e.getMessage());
            userAuthRepository.delete(userAuth);
            throw new AuthException("Error al crear el usuario: " + e.getMessage());
        }

        // 5. Generar tokens JWT (sin userId por ahora)
        String accessToken = jwtService.generateAccessToken(
                userAuth.getEmail(),
                userAuth.getRole().toString(),
                null // userId se actualizará después
        );

        String refreshToken = jwtService.generateRefreshToken(
                userAuth.getEmail(),
                null
        );

        saveRefreshToken(refreshToken, userAuth.getId());

        log.info("Registro exitoso para: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(15 * 60L)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para: {}", request.getEmail());

        // 1. Buscar usuario por email
        UserAuth userAuth = userAuthRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", request.getEmail());
                    return new AuthException("Email incorrecto");
                });

        // 2. Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), userAuth.getPasswordHash())) {
            log.error("Contraseña incorrecta para: {}", request.getEmail());
            throw new AuthException("Contraseña incorrecta");
        }

        // 3. Actualizar lastLogin
        userAuth.setLastLogin(LocalDateTime.now());
        userAuthRepository.save(userAuth);

        // 4. Generar tokens JWT
        String accessToken = jwtService.generateAccessToken(
                userAuth.getEmail(),
                userAuth.getRole().toString(),
                userAuth.getUserId()
        );

        String refreshToken = jwtService.generateRefreshToken(
                userAuth.getEmail(),
                userAuth.getUserId()
        );

        saveRefreshToken(refreshToken, userAuth.getId());

        log.info("Login exitoso para: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(15 * 60L)
                .build();
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenString) {
        log.info("Renovando access token");

        // 1. Validar el refresh token
        if (!jwtService.isTokenValid(refreshTokenString)) {
            log.error("Refresh token inválido");
            throw new AuthException("Refresh token inválido o expirado");
        }

        if (!jwtService.isRefreshToken(refreshTokenString)) {
            log.error("El token no es un refresh token");
            throw new AuthException("El token proporcionado no es un refresh token");
        }

        // 2. Buscar refresh token en BD
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> {
                    log.error("Refresh token no encontrado en BD");
                    return new AuthException("Refresh token no válido");
                });

        // 3. Verificar expiración
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.error("Refresh token expirado");
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("Refresh token expirado");
        }

        // 4. Obtener datos del usuario
        UserAuth userAuth = userAuthRepository.findById(refreshToken.getUserAuthId())
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para refresh token");
                    return new AuthException("Usuario no encontrado");
                });

        // 5. Generar nuevo access token
        String newAccessToken = jwtService.generateAccessToken(
                userAuth.getEmail(),
                userAuth.getRole().toString(),
                userAuth.getUserId()
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

        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token guardado en BD");
    }
}