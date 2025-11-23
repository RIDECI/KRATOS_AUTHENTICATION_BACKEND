package edu.dosw.rideci.infrastructure.controllers;

import edu.dosw.rideci.application.service.AuthService;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.*;
import edu.dosw.rideci.infrastructure.controllers.dto.Response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Envia al correo proporcionado un email que genera un token temporal para recuperar contraseña.
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitud modificar contraseña", description = "Solicitud del correo para restablecer contraseña")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Correo de recuperación enviado");
    }

    /**
     * POST /auth/reset-password
     * Realiza proceso de recuperación de contraseña al acceder al email.
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Proceso para modificar contraseña", description = "Datos necesarios para realizar el cambio de contraseña")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

}
