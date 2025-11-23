package edu.dosw.rideci.infrastructure.persistance.repository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import edu.dosw.rideci.application.port.out.UserAuthRepositoryOutPort;
import edu.dosw.rideci.domain.models.UserAuth;
import edu.dosw.rideci.infrastructure.persistance.entity.UserAuthDocument;
import edu.dosw.rideci.infrastructure.persistance.repository.UserAuthRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAuthRepositoryAdapter implements UserAuthRepositoryOutPort {

    private final UserAuthRepository mongoRepository;

    @Override
    public UserAuth save(UserAuth userAuth) {
        UserAuthDocument document = new UserAuthDocument(
                userAuth.getId(),
                userAuth.getEmail(),
                userAuth.getPasswordHash(),
                userAuth.getRole(),
                userAuth.getUserId(),
                userAuth.getCreatedAt(),
                userAuth.getLastLogin()
        );
        UserAuthDocument saved = mongoRepository.save(document);
        return toDomain(saved);
    }

    @Override
    public void delete(UserAuth userAuth) {

    }

    @Override
    public Optional<UserAuth> findById(String id) {
        return mongoRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<UserAuth> findByEmail(String email) {
        return mongoRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return mongoRepository.existsByEmail(email);
    }

    @Override
    public UserAuth update(String id, UserAuth userAuth) {
        UserAuthDocument document = mongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        document.setPasswordHash(userAuth.getPasswordHash());
        document.setRole(userAuth.getRole());

        UserAuthDocument updated = mongoRepository.save(document);
        return toDomain(updated);
    }

    private UserAuth toDomain(UserAuthDocument document) {
        return new UserAuth(
                document.getId(),
                document.getEmail(),
                document.getPasswordHash(),
                document.getRole(),
                document.getUserId(),
                document.getCreatedAt(),
                document.getLastLogin()
        );
    }
}