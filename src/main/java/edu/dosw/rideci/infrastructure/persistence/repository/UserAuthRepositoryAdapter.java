package edu.dosw.rideci.infrastructure.persistence.repository;
import edu.dosw.rideci.infrastructure.persistence.repository.mapper.UserAuthMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import edu.dosw.rideci.application.port.out.UserAuthRepositoryOutPort;
import edu.dosw.rideci.domain.models.UserAuth;
import edu.dosw.rideci.infrastructure.persistence.entity.UserAuthDocument;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAuthRepositoryAdapter implements UserAuthRepositoryOutPort {

    private final UserAuthRepository mongoRepository;
    private final UserAuthMapper userAuthMapper;

    @Override
    public UserAuth save(UserAuth userAuth) {
        UserAuthDocument document = new UserAuthDocument(
                userAuth.getId(),
                userAuth.getEmail(),
                userAuth.getInstitutionalId(),
                userAuth.getPasswordHash(),
                userAuth.getRole(),
                userAuth.getUserId(),
                userAuth.getCreatedAt(),
                userAuth.getLastLogin()
        );
        UserAuthDocument saved = mongoRepository.save(document);
        return userAuthMapper.toDomain(saved);
    }

    @Override
    public void delete(UserAuth userAuth) {
        mongoRepository.deleteById(userAuth.getId());
    }

    @Override
    public Optional<UserAuth> findById(String id) {
        return mongoRepository.findById(id)
                .map(userAuthMapper::toDomain);
    }

    @Override
    public Optional<UserAuth> findByEmail(String email) {
        return mongoRepository.findByEmail(email)
                .map(userAuthMapper::toDomain);
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
        return userAuthMapper.toDomain(updated);
    }

    @Override
    public void deleteByEmail(String userEmail) {
        mongoRepository.deleteByEmail(userEmail);
    }

    @Override
    public boolean existsById(Long userId) {
        return mongoRepository.existsById(userId);
    }
}
