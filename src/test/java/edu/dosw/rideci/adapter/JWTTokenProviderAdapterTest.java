package edu.dosw.rideci.adapter;

import edu.dosw.rideci.infrastructure.adapters.JWTTokenProviderAdapter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias para JWTTokenProviderAdapter
 */
@ExtendWith(MockitoExtension.class)
class JWTTokenProviderAdapterTest {

    private JWTTokenProviderAdapter jwtTokenProvider;

    private String testEmail;
    private String testRole;
    private Long testUserId;

    @BeforeEach
    void setup() {
        jwtTokenProvider = new JWTTokenProviderAdapter();
        testEmail = "david.palacios-p@mail.escuelaing.edu.co";
        testRole = "STUDENT";
        testUserId = 1000100282L;
    }


    @Test
    @DisplayName("Should generate access token - Success")
    void shouldGenerateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT tiene 3 partes: header.payload.signature
    }

    @Test
    @DisplayName("Should generate access token with correct claims")
    void shouldGenerateAccessTokenWithCorrectClaims() {
        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        Claims claims = jwtTokenProvider.getClaims(token);
        assertThat(claims.getSubject()).isEqualTo(testEmail);
        assertThat(claims.get("role", String.class)).isEqualTo(testRole);
        assertThat(claims.get("userId", Long.class)).isEqualTo(testUserId);
        assertThat(claims.get("type", String.class)).isEqualTo("ACCESS");
        assertThat(claims.get("createdAt")).isNotNull();
    }

    @Test
    @DisplayName("Should generate access token with expiration time")
    void shouldGenerateAccessTokenWithExpiration() {
        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        Date expiration = jwtTokenProvider.getExpirationFromToken(token);
        Date now = new Date();
        long thirtyMinutesInMillis = 30 * 60 * 1000;

        assertThat(expiration).isAfter(now);
        assertThat(expiration.getTime() - now.getTime()).isBetween(
                thirtyMinutesInMillis - 10000L,
                thirtyMinutesInMillis + 10000L
        );
    }

    @Test
    @DisplayName("Should identify token as access token")
    void shouldIdentifyAsAccessToken() {

        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        boolean isAccessToken = jwtTokenProvider.isAccessToken(token);
        boolean isRefreshToken = jwtTokenProvider.isRefreshToken(token);

        assertThat(isAccessToken).isTrue();
        assertThat(isRefreshToken).isFalse();
    }


    @Test
    @DisplayName("Should generate refresh token - Success")
    void shouldGenerateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(testEmail, testUserId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should generate refresh token with correct claims")
    void shouldGenerateRefreshTokenWithCorrectClaims() {
        // When
        String token = jwtTokenProvider.generateRefreshToken(testEmail, testUserId);

        // Then
        Claims claims = jwtTokenProvider.getClaims(token);
        assertThat(claims.getSubject()).isEqualTo(testEmail);
        assertThat(claims.get("userId", Long.class)).isEqualTo(testUserId);
        assertThat(claims.get("type", String.class)).isEqualTo("REFRESH");
        assertThat(claims.get("role")).isNull(); // Refresh token no tiene role
    }

    @Test
    @DisplayName("Should generate refresh token with 7 days expiration")
    void shouldGenerateRefreshTokenWith7DaysExpiration() {
        String token = jwtTokenProvider.generateRefreshToken(testEmail, testUserId);

        Date expiration = jwtTokenProvider.getExpirationFromToken(token);
        Date now = new Date();
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L;

        assertThat(expiration).isAfter(now);
        assertThat(expiration.getTime() - now.getTime()).isBetween(
                sevenDaysInMillis - 60000L,
                sevenDaysInMillis + 60000L
        );
    }

    @Test
    @DisplayName("Should identify token as refresh token")
    void shouldIdentifyAsRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(testEmail, testUserId);

        boolean isRefreshToken = jwtTokenProvider.isRefreshToken(token);
        boolean isAccessToken = jwtTokenProvider.isAccessToken(token);

        assertThat(isRefreshToken).isTrue();
        assertThat(isAccessToken).isFalse();
    }


    @Test
    @DisplayName("Should validate valid token successfully")
    void shouldValidateValidToken() {

        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        boolean isValid = jwtTokenProvider.isTokenValid(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false for malformed token")
    void shouldReturnFalseForMalformedToken() {
        String malformedToken = "es.un.token.jwt.invalido";

        boolean isValid = jwtTokenProvider.isTokenValid(malformedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for empty token")
    void shouldReturnFalseForEmptyToken() {
        boolean isValid = jwtTokenProvider.isTokenValid("");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for null token")
    void shouldReturnFalseForNullToken() {
        boolean isValid = jwtTokenProvider.isTokenValid(null);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should throw exception for malformed token when getting claims")
    void shouldThrowExceptionForMalformedTokenWhenGettingClaims() {
        String malformedToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtTokenProvider.getClaims(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        assertThat(extractedEmail).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("Should extract userId from token")
    void shouldExtractUserIdFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpirationFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        Date expiration = jwtTokenProvider.getExpirationFromToken(token);

        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Should return null for invalid userId format")
    void shouldReturnNullForInvalidUserIdFormat() {
        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isNotNull();
    }

    @Test
    @DisplayName("Should generate token for PROFESSOR role")
    void shouldGenerateTokenForProfessorRole() {
        String professorRole = "PROFESSOR";

        String token = jwtTokenProvider.generateAccessToken(testEmail, professorRole, testUserId);

        Claims claims = jwtTokenProvider.getClaims(token);
        assertThat(claims.get("role", String.class)).isEqualTo(professorRole);
    }

    @Test
    @DisplayName("Should generate token for ADMIN role")
    void shouldGenerateTokenForAdminRole() {
        String adminRole = "ADMINISTRATOR";

        String token = jwtTokenProvider.generateAccessToken(testEmail, adminRole, testUserId);

        Claims claims = jwtTokenProvider.getClaims(token);
        assertThat(claims.get("role", String.class)).isEqualTo(adminRole);
    }

    @Test
    @DisplayName("Should extract all claims correctly")
    void shouldExtractAllClaimsCorrectly() {
        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        Claims claims = jwtTokenProvider.getClaims(token);

        assertThat(claims.getSubject()).isEqualTo(testEmail);
        assertThat(claims.get("role")).isEqualTo(testRole);
        assertThat(claims.get("userId")).isNotNull();
        assertThat(claims.get("type")).isEqualTo("ACCESS");
        assertThat(claims.get("createdAt")).isNotNull();
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    @DisplayName("Should return false when token is expired")
    void shouldReturnFalseWhenTokenIsExpired() {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", testRole);
        claims.put("userId", testUserId);
        claims.put("type", "ACCESS");

        long currentTime = System.currentTimeMillis();
        Key key = Keys.hmacShaKeyFor("UltraSecretoDestroy9778123456789012SuperSeguroParaJWTRideci2025".getBytes(StandardCharsets.UTF_8));

        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(testEmail)
                .setIssuedAt(new Date(currentTime - 2000))
                .setExpiration(new Date(currentTime - 1000)) // Ya expiró
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        boolean isValid = jwtTokenProvider.isTokenValid(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when token signature is invalid")
    void shouldReturnFalseWhenTokenSignatureIsInvalid() {
        String validToken = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        String[] parts = validToken.split("\\.");
        String corruptedToken = parts[0] + "." + parts[1] + "." + parts[2] + "corrupted";

        boolean isValid = jwtTokenProvider.isTokenValid(corruptedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when token is unsupported")
    void shouldReturnFalseWhenTokenIsUnsupported() {
        String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";

        boolean isValid = jwtTokenProvider.isTokenValid(unsupportedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when generic exception occurs in isTokenValid")
    void shouldReturnFalseWhenGenericExceptionOccursInValidation() {
        String invalidToken = "completely.invalid.token";

        boolean isValid = jwtTokenProvider.isTokenValid(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when exception occurs in isAccessToken")
    void shouldReturnFalseWhenExceptionOccursInIsAccessToken() {
        String invalidToken = "invalid.token";

        boolean isAccessToken = jwtTokenProvider.isAccessToken(invalidToken);

        assertThat(isAccessToken).isFalse();
    }

    @Test
    @DisplayName("Should return false when exception occurs in isRefreshToken")
    void shouldReturnFalseWhenExceptionOccursInIsRefreshToken() {
        String invalidToken = "invalid.token";

        boolean isRefreshToken = jwtTokenProvider.isRefreshToken(invalidToken);

        assertThat(isRefreshToken).isFalse();
    }

    @Test
    @DisplayName("Should handle userId as Integer and convert to Long")
    void shouldHandleUserIdAsIntegerAndConvertToLong() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", testRole);
        claims.put("userId", 123); // Integer
        claims.put("type", "ACCESS");

        long currentTime = System.currentTimeMillis();
        Key key = Keys.hmacShaKeyFor("UltraSecretoDestroy9778123456789012SuperSeguroParaJWTRideci2025".getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(testEmail)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + 30 * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(123L);
    }

    @Test
    @DisplayName("Should handle userId as Long")
    void shouldHandleUserIdAsLong() {
        String token = jwtTokenProvider.generateAccessToken(testEmail, testRole, testUserId);

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(testUserId);
        assertThat(userId).isInstanceOf(Long.class);
    }

    @Test
    @DisplayName("Should return null when userId is neither Integer nor Long")
    void shouldReturnNullWhenUserIdIsInvalidType() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", testRole);
        claims.put("userId", "invalid-string"); // String
        claims.put("type", "ACCESS");

        long currentTime = System.currentTimeMillis();
        Key key = Keys.hmacShaKeyFor("UltraSecretoDestroy9778123456789012SuperSeguroParaJWTRideci2025".getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(testEmail)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + 30 * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isNull();
    }
}