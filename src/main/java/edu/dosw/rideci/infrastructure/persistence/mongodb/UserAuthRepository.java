package edu.dosw.rideci.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.dosw.rideci.infrastructure.persistence.entity.UserAuthDocument;

import java.util.Optional;

public interface UserAuthRepository extends MongoRepository<UserAuthDocument, String> {
    Optional<UserAuthDocument> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteByEmail(String userEmail);
    boolean existsById(Long userId);
}