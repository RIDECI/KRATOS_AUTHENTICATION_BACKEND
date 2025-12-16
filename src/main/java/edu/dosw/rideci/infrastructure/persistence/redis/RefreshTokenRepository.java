package edu.dosw.rideci.infrastructure.persistence.redis;

import edu.dosw.rideci.infrastructure.persistence.entity.RefreshTokenDocument;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenDocument, String> {
    Optional<RefreshTokenDocument> findByToken(String token);
    void deleteAllByUserAuthId(String userAuthId);
    void deleteByToken(String token);
}