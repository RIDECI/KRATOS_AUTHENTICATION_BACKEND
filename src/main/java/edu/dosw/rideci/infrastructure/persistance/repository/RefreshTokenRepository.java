package edu.dosw.rideci.infrastructure.persistance.repository;

import edu.dosw.rideci.infrastructure.persistance.entity.RefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para RefreshToken
 */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {

    Optional<RefreshTokenDocument> findByToken(String token);

    List<RefreshTokenDocument> findByUserAuthId(String userAuthId);

    void deleteByUserAuthId(String userAuthId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
