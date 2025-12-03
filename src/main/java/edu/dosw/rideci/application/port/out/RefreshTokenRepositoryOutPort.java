package edu.dosw.rideci.application.port.out;

import edu.dosw.rideci.domain.models.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepositoryOutPort {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUserAuthId(String userAuthId);
    void deleteByToken(RefreshToken token);
}