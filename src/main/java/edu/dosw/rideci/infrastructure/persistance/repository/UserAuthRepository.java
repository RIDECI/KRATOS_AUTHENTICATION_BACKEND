package edu.dosw.rideci.infrastructure.persistance.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.dosw.rideci.infrastructure.persistance.entity.UserAuthDocument;

import java.util.Optional;

public interface UserAuthRepository extends MongoRepository<UserAuthDocument, String> {
    Optional<UserAuthDocument> findByEmail(String email);
    boolean existsByEmail(String email);
}