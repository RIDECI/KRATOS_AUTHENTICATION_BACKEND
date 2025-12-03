package edu.dosw.rideci.controller;

import edu.dosw.rideci.application.service.AuthService;
import edu.dosw.rideci.domain.models.enums.AccountState;
import edu.dosw.rideci.domain.models.enums.Role;
import edu.dosw.rideci.domain.models.enums.identificationType;
import edu.dosw.rideci.infrastructure.controllers.AuthController;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.LoginRequest;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.RefreshTokenRequest;
import edu.dosw.rideci.infrastructure.controllers.dto.Request.RegisterRequest;
import edu.dosw.rideci.infrastructure.controllers.dto.Response.AuthResponse;
import edu.dosw.rideci.infrastructure.controllers.dto.Response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para AuthController
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private UserResponse userResponse;
    private AuthResponse authResponse;

    LocalDateTime now = LocalDateTime.of(2024, 11, 26, 10, 30, 0);

    @BeforeEach
    void setup() {
        // RegisterRequest
        registerRequest = RegisterRequest.builder()
                .name("David Santiago Palacios Pinzón")
                .email("david.palacios-p@mail.escuelaing.edu.co")
                .password("Contraseña123*")
                .phoneNumber("3193475479")
                .role(Role.STUDENT)
                .identificationType(identificationType.CC)
                .identificationNumber("1016948815")
                .Address("Carrera 119#69-18")
                .institutionalId(1000100282L)
                .build();

        // LoginRequest
        loginRequest = LoginRequest.builder()
                .email("david.palacios-p@mail.escuelaing.edu.co")
                .password("Contraseña123*")
                .build();

        // RefreshTokenRequest
        refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_example")
                .build();

        // UserResponse
        userResponse = UserResponse.builder()
                .userId(1000100282L)
                .name("David Santiago Palacios Pinzón")
                .email("david.palacios-p@mail.escuelaing.edu.co")
                .phoneNumber("3193475479")
                .createdAt(now)
                .role(Role.STUDENT)
                .state(AccountState.PENDING)
                .build();

        // AuthResponse
        authResponse = AuthResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token_example")
                .refreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_example")
                .tokenType("Bearer")
                .expiresIn(900L)
                .institutionalId(1000100282L)
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterUser() throws Exception {
        // Given
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "David Santiago Palacios Pinzón",
                            "email": "david.palacios-p@mail.escuelaing.edu.co",
                            "password": "Contraseña123*",
                            "phoneNumber": "3193475479",
                            "role": "STUDENT",
                            "identificationType": "CC",
                            "identificationNumber": "1016948815",
                            "institutionalId": 1000100282,
                            "address": "Carrera 119#69-18"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1000100282L))
                .andExpect(jsonPath("$.name").value("David Santiago Palacios Pinzón"))
                .andExpect(jsonPath("$.email").value("david.palacios-p@mail.escuelaing.edu.co"))
                .andExpect(jsonPath("$.phoneNumber").value("3193475479"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.state").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").value("2024-11-26T10:30:00"));
    }

    @Test
    @DisplayName("Should fail to register user with invalid email format")
    void shouldFailRegisterUserWithInvalidEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "David Santiago Palacios Pinzón",
                            "email": "david.palacios-p@gmail.com",
                            "password": "Contraseña123*",
                            "phoneNumber": "3193475479",
                            "role": "STUDENT",
                            "identificationType": "CC",
                            "identificationNumber": "1016948815",
                            "institutionalId": 1000100282,
                            "address": "Carrera 119#69-18"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to register user with weak password")
    void shouldFailRegisterUserWithWeakPassword() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "David Santiago Palacios Pinzón",
                            "email": "david.palacios-p@gmail.com",
                            "password": "Contraseña123*",
                            "phoneNumber": "3193475479",
                            "role": "STUDENT",
                            "identificationType": "CC",
                            "identificationNumber": "1016948815",
                            "institutionalId": 1000100282,
                            "address": "Carrera 119#69-18"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to register user with missing required fields")
    void shouldFailRegisterUserWithMissingFields() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "David Santiago Palacios Pinzón",
                            "email": "david.palacios-p@mail.escuelaing.edu.co"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "david.palacios-p@mail.escuelaing.edu.co",
                            "password": "Constraseña123*"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token_example"))
                .andExpect(jsonPath("$.refreshToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_example"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900L))
                .andExpect(jsonPath("$.institutionalId").value(1000100282L));
    }

    @Test
    @DisplayName("Should fail to login with missing email")
    void shouldFailLoginWithMissingEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "password": "Constraseña123*"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to login with missing password")
    void shouldFailLoginWithMissingPassword() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "david.palacios-p@mail.escuelaing.edu.co"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to login with invalid email format")
    void shouldFailLoginWithInvalidEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "email": "david.palacios-p@gmail.com",
                        "password": "Password123!"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should refresh access token successfully")
    void shouldRefreshTokenSuccessfully() throws Exception {
        // Given
        when(authService.refreshAccessToken(anyString())).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_example"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token_example"))
                .andExpect(jsonPath("$.refreshToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_example"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900L))
                .andExpect(jsonPath("$.institutionalId").value(1000100282L));
    }

    @Test
    @DisplayName("Should fail to refresh token with missing refresh token")
    void shouldFailRefreshTokenWithMissingToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to refresh token with empty refresh token")
    void shouldFailRefreshTokenWithEmptyToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "refreshToken": ""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should register user with PROFESSOR role")
    void shouldRegisterProfessor() throws Exception {
        // Given
        UserResponse professorResponse = UserResponse.builder()
                .userId(2000200555L)
                .name("Maria Garcia Lopez")
                .email("maria.garcia@escuelaing.edu.co")
                .phoneNumber("3101234567")
                .createdAt(now)
                .role(Role.PROFESSOR)
                .state(AccountState.PENDING)
                .build();

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(professorResponse);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "Maria Garcia Lopez",
                            "email": "maria.garcia@escuelaing.edu.co",
                            "password": "Password123!",
                            "phoneNumber": "3101234567",
                            "role": "PROFESSOR",
                            "identificationType": "CC",
                            "identificationNumber": "52123456",
                            "institutionalId": 2000200555,
                            "address": "Calle 100 # 20 - 30"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(2000200555L))
                .andExpect(jsonPath("$.name").value("Maria Garcia Lopez"))
                .andExpect(jsonPath("$.email").value("maria.garcia@escuelaing.edu.co"))
                .andExpect(jsonPath("$.role").value("PROFESSOR"))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    @DisplayName("Should register user with TI identification type")
    void shouldRegisterUserWithTI() throws Exception {
        // Given
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "David Santiago Palacios Pinzóm",
                            "email": "david.palacios-p@mail.escuelaing.edu.co",
                            "password": "Contraseña123*",
                            "phoneNumber": "3193475479",
                            "role": "STUDENT",
                            "identificationType": "TI",
                            "identificationNumber": "1016948815",
                            "institutionalId": 1000100282,
                            "address": "Carrea 119#69-18"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should login with valid credentials and return tokens")
    void shouldLoginAndReturnTokens() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "david.palacios-p@mail.escuelaing.edu.co",
                            "password": "Contraseña123*"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.institutionalId").isNumber());
    }
}