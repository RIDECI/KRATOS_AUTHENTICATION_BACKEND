package edu.dosw.rideci.infrastructure.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.rideci.application.port.out.PasswordResetOutPort;
import edu.dosw.rideci.domain.models.PasswordResetTokenData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Adaptador de Redis para gestión de tokens de password reset
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetAdapter implements PasswordResetOutPort {

    @Qualifier("passwordResetRedisTemplate")
    private final RedisTemplate<String, PasswordResetTokenData> redisTemplate;

    private static final String RESET_TOKEN_PREFIX = "rideci_reset:";
    private static final String ATTEMPT_PREFIX = "reset_attempt:";
    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void saveResetToken(String token, PasswordResetTokenData data, long expirationMinutes) {
        String key = RESET_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, data, expirationMinutes, TimeUnit.MINUTES);
        log.debug("Token de reset guardado en Redis con TTL de {} minutos", expirationMinutes);
    }

    @Override
    public Optional<PasswordResetTokenData> getResetToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        Object data = redisTemplate.opsForValue().get(key);

        if (data == null) {
            log.debug("Token no encontrado en Redis: {}", token);
            return Optional.empty();
        }

        PasswordResetTokenData tokenData = objectMapper.convertValue(data, PasswordResetTokenData.class);
        return Optional.of(tokenData);
    }

    @Override
    public void deleteResetToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Token eliminado de Redis");
    }

    @Override
    public void incrementAttempts(String token) {
        getResetToken(token).ifPresent(data -> {
            data.setAttempts(data.getAttempts() + 1);

            String key = RESET_TOKEN_PREFIX + token;
            Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);

            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.MINUTES);
            }
        });
    }

    @Override
    public void saveResetAttempt(String email) {
        String key = ATTEMPT_PREFIX + email;
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
        log.debug("Intento de reset registrado para: {}", email);
    }

    @Override
    public long countResetAttempts(String email) {
        String key = ATTEMPT_PREFIX + email;
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }
}
