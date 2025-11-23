package edu.dosw.rideci.infrastructure.persistance.repository;
import edu.dosw.rideci.domain.models.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import edu.dosw.rideci.infrastructure.persistance.entity.RefreshTokenDocument;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {
    Optional<RefreshTokenDocument> findByToken(String token);
    void deleteAllByUserAuthId(String userAuthId);
    void deleteByToken(RefreshToken token);
}