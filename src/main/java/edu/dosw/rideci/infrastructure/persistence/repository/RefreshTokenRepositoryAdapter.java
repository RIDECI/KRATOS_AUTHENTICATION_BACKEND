package edu.dosw.rideci.infrastructure.persistence.repository;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import edu.dosw.rideci.application.port.out.RefreshTokenRepositoryOutPort;
import edu.dosw.rideci.domain.models.RefreshToken;
import edu.dosw.rideci.infrastructure.persistence.entity.RefreshTokenDocument;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryOutPort {

    private static final Long TTL_DAYS = 259200L;

    private final RefreshTokenRepository redisRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenDocument document = RefreshTokenDocument.builder()
                .token(refreshToken.getToken())
                .userAuthId(refreshToken.getUserAuthId())
                .expiresAt(refreshToken.getExpiresAt())
                .createdAt(refreshToken.getCreatedAt())
                .ttl(TTL_DAYS)
                .build();

        RefreshTokenDocument saved = redisRepository.save(document);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return redisRepository.findByToken(token)
                .map(this::toDomain);
    }

    @Override
    public void deleteAllByUserAuthId(String userAuthId) {
        redisRepository.deleteAllByUserAuthId(userAuthId);
    }

    @Override
    public void deleteByToken(RefreshToken token) {
        redisRepository.deleteByToken(token.getToken());
    }

    private RefreshToken toDomain(RefreshTokenDocument document) {
        return RefreshToken.builder()
                .id(document.getId())
                .token(document.getToken())
                .userAuthId(document.getUserAuthId())
                .expiresAt(document.getExpiresAt())
                .createdAt(document.getCreatedAt())
                .build();
    }
}