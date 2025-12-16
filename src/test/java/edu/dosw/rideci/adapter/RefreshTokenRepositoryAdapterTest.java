package edu.dosw.rideci.adapter;

import edu.dosw.rideci.domain.models.RefreshToken;
import edu.dosw.rideci.infrastructure.persistence.entity.RefreshTokenDocument;
import edu.dosw.rideci.infrastructure.persistence.repository.RefreshTokenRepository;
import edu.dosw.rideci.infrastructure.persistence.repository.RefreshTokenRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RefreshTokenRepositoryAdapter
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryAdapterTest {

    @Mock
    private RefreshTokenRepository redisRepository;

    @InjectMocks
    private RefreshTokenRepositoryAdapter refreshTokenRepositoryAdapter;

    private RefreshToken refreshToken;
    private RefreshTokenDocument refreshTokenDocument;
    private LocalDateTime now;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setup() {
        now = LocalDateTime.of(2025, 11, 27, 15, 55, 0);
        expiresAt = now.plusDays(7); // 7 días después

        refreshToken = RefreshToken.builder()
                .id("token123")
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_example")
                .userAuthId("usuario123")
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        refreshTokenDocument = RefreshTokenDocument.builder()
                .id("token123")
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_example")
                .userAuthId("usuario123")
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();
    }

    @Test
    @DisplayName("Should save RefreshToken - Successfull")
    void shouldSaveRefreshToken() {
        // Given
        when(redisRepository.save(any(RefreshTokenDocument.class))).thenReturn(refreshTokenDocument);

        // When
        RefreshToken result = refreshTokenRepositoryAdapter.save(refreshToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("token123");
        assertThat(result.getToken()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_example");
        assertThat(result.getUserAuthId()).isEqualTo("usuario123");
        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(result.getCreatedAt()).isEqualTo(now);

        verify(redisRepository, times(1)).save(any(RefreshTokenDocument.class));
    }

    @Test
    @DisplayName("Should save RefreshToken without id")
    void shouldSaveRefreshTokenWithoutId() {
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_token")
                .userAuthId("auth456")
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        RefreshTokenDocument savedDocument = RefreshTokenDocument.builder()
                .id("mongoGenerated_id")
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_token")
                .userAuthId("usuario456")
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        when(redisRepository.save(any(RefreshTokenDocument.class))).thenReturn(savedDocument);

        RefreshToken result = refreshTokenRepositoryAdapter.save(newRefreshToken);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("mongoGenerated_id");
        assertThat(result.getToken()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_token");
        assertThat(result.getUserAuthId()).isEqualTo("usuario456");

        verify(redisRepository, times(1)).save(any(RefreshTokenDocument.class));
    }

    @Test
    @DisplayName("Should find RefreshToken by token - Success")
    void shouldFindRefreshTokenByToken() {
        String tokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_example";
        when(redisRepository.findByToken(tokenString)).thenReturn(Optional.of(refreshTokenDocument));

        Optional<RefreshToken> result = refreshTokenRepositoryAdapter.findByToken(tokenString);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("token123");
        assertThat(result.get().getToken()).isEqualTo(tokenString);
        assertThat(result.get().getUserAuthId()).isEqualTo("usuario123");
        assertThat(result.get().getExpiresAt()).isEqualTo(expiresAt);

        verify(redisRepository, times(1)).findByToken(tokenString);
    }

    @Test
    @DisplayName("Should return empty when token does not exist")
    void shouldReturnEmptyWhenTokenNotFound() {
        String nonExistentToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.nonexistent_token";
        when(redisRepository.findByToken(nonExistentToken)).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenRepositoryAdapter.findByToken(nonExistentToken);

        assertThat(result).isEmpty();

        verify(redisRepository, times(1)).findByToken(nonExistentToken);
    }

    @Test
    @DisplayName("Should delete all RefreshTokens by userAuthId")
    void shouldDeleteAllByUserAuthId() {
        String userAuthId = "usuario123";
        doNothing().when(redisRepository).deleteAllByUserAuthId(userAuthId);

        refreshTokenRepositoryAdapter.deleteAllByUserAuthId(userAuthId);

        verify(redisRepository, times(1)).deleteAllByUserAuthId(userAuthId);
    }

    @Test
    @DisplayName("Should delete all tokens for user without exception")
    void shouldDeleteAllTokensWithoutException() {
        String userAuthId = "usuario456";
        doNothing().when(redisRepository).deleteAllByUserAuthId(userAuthId);

        refreshTokenRepositoryAdapter.deleteAllByUserAuthId(userAuthId);

        verify(redisRepository, times(1)).deleteAllByUserAuthId(userAuthId);
        verifyNoMoreInteractions(redisRepository);
    }

    @Test
    @DisplayName("Should attempt to delete all tokens even if user has no tokens")
    void shouldAttemptDeleteAllTokensForUserWithNoTokens() {
        String userAuthId = "usuarioSinTokens";
        doNothing().when(redisRepository).deleteAllByUserAuthId(userAuthId);

        refreshTokenRepositoryAdapter.deleteAllByUserAuthId(userAuthId);

        // Then
        verify(redisRepository, times(1)).deleteAllByUserAuthId(userAuthId);
    }

    @Test
    @DisplayName("Should delete RefreshToken by token")
    void shouldDeleteByToken() {
        String tokenString = refreshToken.getToken();

        doNothing().when(redisRepository).deleteByToken(tokenString);

        refreshTokenRepositoryAdapter.deleteByToken(refreshToken);

        verify(redisRepository, times(1)).deleteByToken(tokenString);
    }

    @Test
    @DisplayName("Should delete RefreshToken by token without exception")
    void shouldDeleteByTokenWithoutException() {
        RefreshToken tokenToDelete = RefreshToken.builder()
                .id("token456")
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token_to_delete")
                .userAuthId("usuario789")
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        String tokenString = tokenToDelete.getToken();

        doNothing().when(redisRepository).deleteByToken(tokenString);

        refreshTokenRepositoryAdapter.deleteByToken(tokenToDelete);

        verify(redisRepository, times(1)).deleteByToken(tokenString);
        verifyNoMoreInteractions(redisRepository);
    }

    @Test
    @DisplayName("Should save RefreshToken with expiration date in the future")
    void shouldSaveRefreshTokenWithFutureExpiration() {
        LocalDateTime futureExpiration = now.plusDays(30);
        RefreshToken longLivedToken = RefreshToken.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.long_lived_token")
                .userAuthId("usuario123")
                .expiresAt(futureExpiration)
                .createdAt(now)
                .build();

        RefreshTokenDocument savedDocument = RefreshTokenDocument.builder()
                .id("tokenConLargaVida")
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.tokenLargaVida")
                .userAuthId("usuario123")
                .expiresAt(futureExpiration)
                .createdAt(now)
                .build();

        when(redisRepository.save(any(RefreshTokenDocument.class))).thenReturn(savedDocument);

        RefreshToken result = refreshTokenRepositoryAdapter.save(longLivedToken);

        assertThat(result).isNotNull();
        assertThat(result.getExpiresAt()).isEqualTo(futureExpiration);
        assertThat(result.getExpiresAt()).isAfter(now);

        verify(redisRepository, times(1)).save(any(RefreshTokenDocument.class));
    }

    @Test
    @DisplayName("Should map all fields correctly from document to domain")
    void shouldMapAllFieldsCorrectlyFromDocumentToDomain() {
        String tokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.complete_token";
        when(redisRepository.findByToken(tokenString)).thenReturn(Optional.of(refreshTokenDocument));

        Optional<RefreshToken> result = refreshTokenRepositoryAdapter.findByToken(tokenString);

        assertThat(result).isPresent();
        RefreshToken token = result.get();
        assertThat(token.getId()).isEqualTo(refreshTokenDocument.getId());
        assertThat(token.getToken()).isEqualTo(refreshTokenDocument.getToken());
        assertThat(token.getUserAuthId()).isEqualTo(refreshTokenDocument.getUserAuthId());
        assertThat(token.getExpiresAt()).isEqualTo(refreshTokenDocument.getExpiresAt());
        assertThat(token.getCreatedAt()).isEqualTo(refreshTokenDocument.getCreatedAt());

        verify(redisRepository, times(1)).findByToken(tokenString);
    }

    @Test
    @DisplayName("Should save multiple RefreshTokens for same user")
    void shouldSaveMultipleTokensForSameUser() {
        String userAuthId = "usuario123";

        RefreshToken token1 = RefreshToken.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token1")
                .userAuthId(userAuthId)
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        RefreshToken token2 = RefreshToken.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token2")
                .userAuthId(userAuthId)
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        RefreshTokenDocument savedDoc1 = RefreshTokenDocument.builder()
                .id("token_id_1")
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token1")
                .userAuthId(userAuthId)
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        RefreshTokenDocument savedDoc2 = RefreshTokenDocument.builder()
                .id("token_id_2")
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token2")
                .userAuthId(userAuthId)
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        when(redisRepository.save(any(RefreshTokenDocument.class)))
                .thenReturn(savedDoc1)
                .thenReturn(savedDoc2);

        RefreshToken result1 = refreshTokenRepositoryAdapter.save(token1);
        RefreshToken result2 = refreshTokenRepositoryAdapter.save(token2);

        assertThat(result1.getUserAuthId()).isEqualTo(userAuthId);
        assertThat(result2.getUserAuthId()).isEqualTo(userAuthId);
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
        assertThat(result1.getToken()).isNotEqualTo(result2.getToken());

        verify(redisRepository, times(2)).save(any(RefreshTokenDocument.class));
    }

    @Test
    @DisplayName("Should handle deletion of non-existent token gracefully")
    void shouldHandleDeletionOfNonExistentToken() {
        RefreshToken nonExistentToken = RefreshToken.builder()
                .id("noExiste")
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.noExiste")
                .userAuthId("usuario999")
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        String tokenString = nonExistentToken.getToken();

        doNothing().when(redisRepository).deleteByToken(tokenString);

        refreshTokenRepositoryAdapter.deleteByToken(nonExistentToken);

        verify(redisRepository, times(1)).deleteByToken(tokenString);
    }
}
