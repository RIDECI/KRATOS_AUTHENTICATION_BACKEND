package edu.dosw.rideci.application.port.out;

import edu.dosw.rideci.domain.models.UserAuth;
import java.util.Optional;

public interface UserAuthRepositoryOutPort {
    UserAuth save(UserAuth userAuth);
    void delete(UserAuth userAuth);
    Optional<UserAuth> findById(String id);
    Optional<UserAuth> findByEmail(String email);
    boolean existsByEmail(String email);
    UserAuth update(String id, UserAuth userAuth);
}
