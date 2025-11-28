package edu.dosw.rideci.infrastructure.adapters;

import edu.dosw.rideci.application.port.out.TokenProviderOutPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para generar y validar tokens JWT
 * Compatible con JJWT 0.11.5
 */
@Slf4j
@Component
public class JWTTokenProviderAdapter implements TokenProviderOutPort {

    private static final long ACCESS_TOKEN_VALIDITY = 30 * 60 * 1000; // 30 minutos
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 días

    private static final String SECRET_KEY = "UltraSecretoDestroy9778123456789012SuperSeguroParaJWTRideci2025";

    private final Key key;

    public JWTTokenProviderAdapter() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un Access Token (30 minutos)
     */
    @Override
    public String generateAccessToken(String email, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("createdAt", LocalDateTime.now().toString());
        claims.put("userId", userId);
        claims.put("type", "ACCESS");

        return buildToken(claims, email, ACCESS_TOKEN_VALIDITY);
    }

    /**
     * Genera un Refresh Token (7 días)
     */
    public String generateRefreshToken(String email, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "REFRESH");

        return buildToken(claims, email, REFRESH_TOKEN_VALIDITY);
    }

    private String buildToken(Map<String, Object> claims, String subject, long validity) {
        long currentTime = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida si el token es válido
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("Firma del token inválida: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Token malformado: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Token no soportado: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("Token vacío o nulo: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error al validar token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isAccessToken(String token) {
        try {
            String type = (String) getClaims(token).get("type");
            return "ACCESS".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            String type = (String) getClaims(token).get("type");
            return "REFRESH".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae los claims del token
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Object userIdObj = getClaims(token).get("userId");

        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }

        return null;
    }

    public Date getExpirationFromToken(String token) {
        return getClaims(token).getExpiration();
    }

}

