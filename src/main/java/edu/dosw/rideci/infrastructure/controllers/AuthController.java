package edu.dosw.rideci.infrastructure.controllers;

import edu.dosw.rideci.application.service.AuthService;
import edu.dosw.rideci.application.service.PasswordResetService;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.*;
import edu.dosw.rideci.infrastructure.controllers.dto.Response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de Autenticación
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints de autenticación y autorización")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    /**
     * POST /auth/register
     * Registra un nuevo usuario
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario y genera tokens JWT")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - Email: {}", request.getEmail());
        UserResponse response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/login
     * Autentica un usuario existente
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y genera tokens JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - Email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/refresh
     * Renueva el access token usando el refresh token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token", description = "Genera un nuevo access token usando el refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/auth/refresh");
        AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/forgot-password
     * Crea una solicitud para realizar el reset de la contraseña
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Crear solicitud reset contraseña", description = "Manda una solicitud para enviar un correo con cambio de contraseña")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/auth/forgot-password - Email: {}", request.getEmail());

        passwordResetService.requestPasswordReset(request);
        return ResponseEntity.ok(Map.of(
                "message", "Si el email existe, recibirás un código de recuperación"
        ));
    }

    /**
     * POST /auth/reset-password
     * Se encarga de realizar el proceso de cambio de contraseña
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Proceso cambio contraseña", description = "Con el token enviado al correo, continua el proceso de reseteo de contraseña")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/auth/reset-password");
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada con éxito"));
    }
}
