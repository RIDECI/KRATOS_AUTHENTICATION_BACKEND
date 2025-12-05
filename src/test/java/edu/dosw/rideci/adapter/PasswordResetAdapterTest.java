package edu.dosw.rideci.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.rideci.domain.models.PasswordResetTokenData;
import edu.dosw.rideci.infrastructure.adapters.PasswordResetAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PasswordResetAdapter
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetAdapterTest {

    @Mock
    private RedisTemplate<String, PasswordResetTokenData> redisTemplate;

    @Mock
    private ValueOperations<String, PasswordResetTokenData> valueOperations;

    @Mock
    private ListOperations<String, PasswordResetTokenData> listOperations;

    @Mock
    private ObjectMapper objectMapper;

    private PasswordResetAdapter passwordResetAdapter;

    private String testToken;
    private String testEmail;
    private PasswordResetTokenData testTokenData;
    private LocalDateTime testCreatedAt;

    @BeforeEach
    void setup() {
        passwordResetAdapter = new PasswordResetAdapter(redisTemplate);
        ReflectionTestUtils.setField(passwordResetAdapter, "objectMapper", objectMapper);

        testToken = "test-reset-token-123456";
        testEmail = "david.palacios-p@mail.escuelaing.edu.co";
        testCreatedAt = LocalDateTime.now();

        testTokenData = PasswordResetTokenData.builder()
                .email(testEmail)
                .createdAt(testCreatedAt)
                .attempts(0)
                .build();
    }

    @Test
    @DisplayName("Should save reset token successfully")
    void shouldSaveResetToken() {
        // Given
        long expirationMinutes = 15L;
        String expectedKey = "rideci_reset:" + testToken;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        passwordResetAdapter.saveResetToken(testToken, testTokenData, expirationMinutes);

        // Then
        verify(valueOperations).set(expectedKey, testTokenData, expirationMinutes, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Should get reset token successfully")
    void shouldGetResetToken() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(testTokenData);
        when(objectMapper.convertValue(testTokenData, PasswordResetTokenData.class)).thenReturn(testTokenData);

        // When
        Optional<PasswordResetTokenData> result = passwordResetAdapter.getResetToken(testToken);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(testEmail);
        assertThat(result.get().getCreatedAt()).isEqualTo(testCreatedAt);
        assertThat(result.get().getAttempts()).isEqualTo(0);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("Should return empty when token not found")
    void shouldReturnEmptyWhenTokenNotFound() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(null);

        // When
        Optional<PasswordResetTokenData> result = passwordResetAdapter.getResetToken(testToken);

        // Then
        assertThat(result).isEmpty();
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("Should delete reset token successfully")
    void shouldDeleteResetToken() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;

        // When
        passwordResetAdapter.deleteResetToken(testToken);

        // Then
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    @DisplayName("Should increment attempts successfully")
    void shouldIncrementAttempts() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(testTokenData);
        when(objectMapper.convertValue(testTokenData, PasswordResetTokenData.class)).thenReturn(testTokenData);
        when(redisTemplate.getExpire(expectedKey, TimeUnit.MINUTES)).thenReturn(10L);

        // When
        passwordResetAdapter.incrementAttempts(testToken);

        // Then
        assertThat(testTokenData.getAttempts()).isEqualTo(1);
        verify(valueOperations).set(eq(expectedKey), any(PasswordResetTokenData.class), eq(10L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Should not increment attempts when token not found")
    void shouldNotIncrementAttemptsWhenTokenNotFound() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(null);

        // When
        passwordResetAdapter.incrementAttempts(testToken);

        // Then
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Should not increment attempts when TTL is null")
    void shouldNotIncrementAttemptsWhenTTLIsNull() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(testTokenData);
        when(objectMapper.convertValue(testTokenData, PasswordResetTokenData.class)).thenReturn(testTokenData);
        when(redisTemplate.getExpire(expectedKey, TimeUnit.MINUTES)).thenReturn(null);

        // When
        passwordResetAdapter.incrementAttempts(testToken);

        // Then
        verify(valueOperations, times(1)).get(expectedKey);
        verify(valueOperations, never()).set(eq(expectedKey), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Should not increment attempts when TTL is zero or negative")
    void shouldNotIncrementAttemptsWhenTTLIsZeroOrNegative() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(testTokenData);
        when(objectMapper.convertValue(testTokenData, PasswordResetTokenData.class)).thenReturn(testTokenData);
        when(redisTemplate.getExpire(expectedKey, TimeUnit.MINUTES)).thenReturn(0L);

        // When
        passwordResetAdapter.incrementAttempts(testToken);

        // Then
        verify(valueOperations, never()).set(eq(expectedKey), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Should save reset attempt successfully")
    void shouldSaveResetAttempt() {
        // Given
        String expectedKey = "reset_attempt:" + testEmail;
        when(redisTemplate.expire(expectedKey, 1, TimeUnit.HOURS)).thenReturn(true);

        // When
        passwordResetAdapter.saveResetAttempt(testEmail);

        // Then
        verify(redisTemplate).expire(expectedKey, 1, TimeUnit.HOURS);
    }

    @Test
    @DisplayName("Should count reset attempts successfully")
    void shouldCountResetAttempts() {
        // Given
        String expectedKey = "reset_attempt:" + testEmail;
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(expectedKey)).thenReturn(3L);

        // When
        long count = passwordResetAdapter.countResetAttempts(testEmail);

        // Then
        assertThat(count).isEqualTo(3L);
        verify(listOperations).size(expectedKey);
    }

    @Test
    @DisplayName("Should return zero when count attempts returns null")
    void shouldReturnZeroWhenCountAttemptsReturnsNull() {
        // Given
        String expectedKey = "reset_attempt:" + testEmail;
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(expectedKey)).thenReturn(null);

        // When
        long count = passwordResetAdapter.countResetAttempts(testEmail);

        // Then
        assertThat(count).isEqualTo(0L);
        verify(listOperations).size(expectedKey);
    }

    @Test
    @DisplayName("Should save token with different expiration times")
    void shouldSaveTokenWithDifferentExpirationTimes() {
        // Given
        long expirationMinutes = 30L;
        String expectedKey = "rideci_reset:" + testToken;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        passwordResetAdapter.saveResetToken(testToken, testTokenData, expirationMinutes);

        // Then
        verify(valueOperations).set(expectedKey, testTokenData, expirationMinutes, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Should handle multiple increment attempts")
    void shouldHandleMultipleIncrementAttempts() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(testTokenData);
        when(objectMapper.convertValue(testTokenData, PasswordResetTokenData.class)).thenReturn(testTokenData);
        when(redisTemplate.getExpire(expectedKey, TimeUnit.MINUTES)).thenReturn(10L);

        // When
        passwordResetAdapter.incrementAttempts(testToken);
        passwordResetAdapter.incrementAttempts(testToken);
        passwordResetAdapter.incrementAttempts(testToken);

        // Then
        assertThat(testTokenData.getAttempts()).isEqualTo(3);
        verify(valueOperations, times(3)).set(eq(expectedKey), any(PasswordResetTokenData.class), eq(10L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Should get token with all data fields")
    void shouldGetTokenWithAllDataFields() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;
        PasswordResetTokenData complexTokenData = PasswordResetTokenData.builder()
                .email(testEmail)
                .createdAt(testCreatedAt)
                .attempts(2)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(complexTokenData);
        when(objectMapper.convertValue(complexTokenData, PasswordResetTokenData.class)).thenReturn(complexTokenData);

        // When
        Optional<PasswordResetTokenData> result = passwordResetAdapter.getResetToken(testToken);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(testEmail);
        assertThat(result.get().getCreatedAt()).isEqualTo(testCreatedAt);
        assertThat(result.get().getAttempts()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle different email formats for reset attempts")
    void shouldHandleDifferentEmailFormatsForResetAttempts() {
        // Given
        String professorEmail = "maria.garcia@escuelaing.edu.co";
        String expectedKey = "reset_attempt:" + professorEmail;
        when(redisTemplate.expire(expectedKey, 1, TimeUnit.HOURS)).thenReturn(true);

        // When
        passwordResetAdapter.saveResetAttempt(professorEmail);

        // Then
        verify(redisTemplate).expire(expectedKey, 1, TimeUnit.HOURS);
    }

    @Test
    @DisplayName("Should save token with zero attempts initially")
    void shouldSaveTokenWithZeroAttemptsInitially() {
        // Given
        long expirationMinutes = 15L;
        String expectedKey = "rideci_reset:" + testToken;
        PasswordResetTokenData newTokenData = PasswordResetTokenData.builder()
                .email(testEmail)
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .build();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        passwordResetAdapter.saveResetToken(testToken, newTokenData, expirationMinutes);

        // Then
        verify(valueOperations).set(expectedKey, newTokenData, expirationMinutes, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Should increment attempts from zero to one")
    void shouldIncrementAttemptsFromZeroToOne() {
        // Given
        String expectedKey = "rideci_reset:" + testToken;
        PasswordResetTokenData freshToken = PasswordResetTokenData.builder()
                .email(testEmail)
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(freshToken);
        when(objectMapper.convertValue(freshToken, PasswordResetTokenData.class)).thenReturn(freshToken);
        when(redisTemplate.getExpire(expectedKey, TimeUnit.MINUTES)).thenReturn(15L);

        // When
        passwordResetAdapter.incrementAttempts(testToken);

        // Then
        assertThat(freshToken.getAttempts()).isEqualTo(1);
    }
}