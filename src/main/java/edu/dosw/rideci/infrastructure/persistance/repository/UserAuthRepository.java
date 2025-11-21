package edu.dosw.rideci.infrastructure.persistance.repository;

import edu.dosw.rideci.infrastructure.persistance.entity.UserAuth;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para UserAuth
 */
@Repository
public interface UserAuthRepository extends MongoRepository<UserAuth, String> {

    Optional<UserAuth> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserAuth> findByUserId(Long userId);
}
