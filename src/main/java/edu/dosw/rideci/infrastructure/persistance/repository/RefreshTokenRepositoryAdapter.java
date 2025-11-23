package edu.dosw.rideci.infrastructure.persistance.repository;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import edu.dosw.rideci.application.port.out.RefreshTokenRepositoryOutPort;
import edu.dosw.rideci.domain.models.RefreshToken;
import edu.dosw.rideci.infrastructure.persistance.entity.RefreshTokenDocument;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryOutPort {

    private final RefreshTokenRepository mongoRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenDocument document = RefreshTokenDocument.builder()
                .token(refreshToken.getToken())
                .userAuthId(refreshToken.getUserAuthId())
                .expiresAt(refreshToken.getExpiresAt())
                .createdAt(refreshToken.getCreatedAt())
                .build();

        RefreshTokenDocument saved = mongoRepository.save(document);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return mongoRepository.findByToken(token)
                .map(this::toDomain);
    }

    @Override
    public void deleteAllByUserAuthId(String userAuthId) {
        mongoRepository.deleteAllByUserAuthId(userAuthId);
    }

    @Override
    public void deleteByToken(RefreshToken token) {
        mongoRepository.deleteByToken(token);
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

