package edu.dosw.rideci.adapter;

import edu.dosw.rideci.domain.models.UserAuth;
import edu.dosw.rideci.domain.models.enums.Role;
import edu.dosw.rideci.infrastructure.persistance.entity.UserAuthDocument;
import edu.dosw.rideci.infrastructure.persistance.repository.UserAuthRepository;
import edu.dosw.rideci.infrastructure.persistance.repository.UserAuthRepositoryAdapter;
import edu.dosw.rideci.infrastructure.persistance.repository.mapper.UserAuthMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UserAuthRepositoryAdapter
 */
@ExtendWith(MockitoExtension.class)
class UserAuthRepositoryAdapterTest {

    @Mock
    private UserAuthRepository mongoRepository;

    @Mock
    private UserAuthMapper userAuthMapper;

    @InjectMocks
    private UserAuthRepositoryAdapter userAuthRepositoryAdapter;

    private UserAuth userAuth;
    private UserAuthDocument userAuthDocument;
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        now = LocalDateTime.of(2024, 11, 26, 10, 30, 0);

        userAuth = UserAuth.builder()
                .id("idididid123")
                .email("david.palacios-p@mail.escuelaing.edu.co")
                .institutionalId(1000100282L)
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.STUDENT)
                .userId(null)
                .createdAt(now)
                .lastLogin(now)
                .build();

        userAuthDocument = UserAuthDocument.builder()
                .id("idididid123")
                .email("david.palacios-p@mail.escuelaing.edu.co")
                .institutionalId(1000100282L)
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.STUDENT)
                .userId(null)
                .createdAt(now)
                .lastLogin(now)
                .build();
    }

    @Test
    @DisplayName("Should save UserAuth - Success")
    void shouldSaveUserAuth() {
        when(mongoRepository.save(any(UserAuthDocument.class))).thenReturn(userAuthDocument);
        when(userAuthMapper.toDomain(any(UserAuthDocument.class))).thenReturn(userAuth);

        UserAuth result = userAuthRepositoryAdapter.save(userAuth);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("idididid123");
        assertThat(result.getEmail()).isEqualTo("david.palacios-p@mail.escuelaing.edu.co");
        assertThat(result.getInstitutionalId()).isEqualTo(1000100282L);
        assertThat(result.getRole()).isEqualTo(Role.STUDENT);

        verify(mongoRepository, times(1)).save(any(UserAuthDocument.class));
        verify(userAuthMapper, times(1)).toDomain(any(UserAuthDocument.class));
    }

    @Test
    @DisplayName("Should find UserAuth by id - Success")
    void shouldFindUserAuthById() {
        when(mongoRepository.findById("idididid123")).thenReturn(Optional.of(userAuthDocument));
        when(userAuthMapper.toDomain(userAuthDocument)).thenReturn(userAuth);

        Optional<UserAuth> result = userAuthRepositoryAdapter.findById("idididid123");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("idididid123");
        assertThat(result.get().getEmail()).isEqualTo("david.palacios-p@mail.escuelaing.edu.co");

        verify(mongoRepository, times(1)).findById("idididid123");
        verify(userAuthMapper, times(1)).toDomain(userAuthDocument);
    }

    @Test
    @DisplayName("Should return empty when UserAuth id does not exist")
    void shouldReturnEmptyWhenIdNotFound() {
        when(mongoRepository.findById("noExisteId")).thenReturn(Optional.empty());

        Optional<UserAuth> result = userAuthRepositoryAdapter.findById("noExisteId");

        assertThat(result).isEmpty();

        verify(mongoRepository, times(1)).findById("noExisteId");
        verify(userAuthMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find UserAuth by email - Success")
    void shouldFindUserAuthByEmail() {
        String email = "david.palacios-p@mail.escuelaing.edu.co";
        when(mongoRepository.findByEmail(email)).thenReturn(Optional.of(userAuthDocument));
        when(userAuthMapper.toDomain(userAuthDocument)).thenReturn(userAuth);

        Optional<UserAuth> result = userAuthRepositoryAdapter.findByEmail(email);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        assertThat(result.get().getId()).isEqualTo("idididid123");

        verify(mongoRepository, times(1)).findByEmail(email);
        verify(userAuthMapper, times(1)).toDomain(userAuthDocument);
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailNotFound() {
        String email = "inexistente@mail.escuelaing.edu.co";
        when(mongoRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<UserAuth> result = userAuthRepositoryAdapter.findByEmail(email);

        assertThat(result).isEmpty();

        verify(mongoRepository, times(1)).findByEmail(email);
        verify(userAuthMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        String email = "david.palacios-p@mail.escuelaing.edu.co";
        when(mongoRepository.existsByEmail(email)).thenReturn(true);

        boolean result = userAuthRepositoryAdapter.existsByEmail(email);

        assertThat(result).isTrue();

        verify(mongoRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        String email = "inexistente@mail.escuelaing.edu.co";
        when(mongoRepository.existsByEmail(email)).thenReturn(false);

        boolean result = userAuthRepositoryAdapter.existsByEmail(email);

        assertThat(result).isFalse();

        verify(mongoRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Should update UserAuth - Success")
    void shouldUpdateUserAuth() {
        String userId = "idididid123";
        UserAuth updatedUserAuth = UserAuth.builder()
                .id(userId)
                .email("david.palacios-p@mail.escuelaing.edu.co")
                .institutionalId(1000100282L)
                .passwordHash("$2a$10$newHashedPassword")
                .role(Role.PROFESSOR)
                .userId(null)
                .createdAt(now)
                .lastLogin(now)
                .build();

        UserAuthDocument updatedDocument = UserAuthDocument.builder()
                .id(userId)
                .email("david.palacios-p@mail.escuelaing.edu.co")
                .institutionalId(1000100282L)
                .passwordHash("$2a$10$newHashedPassword")
                .role(Role.PROFESSOR)
                .userId(null)
                .createdAt(now)
                .lastLogin(now)
                .build();

        when(mongoRepository.findById(userId)).thenReturn(Optional.of(userAuthDocument));
        when(mongoRepository.save(any(UserAuthDocument.class))).thenReturn(updatedDocument);
        when(userAuthMapper.toDomain(updatedDocument)).thenReturn(updatedUserAuth);

        UserAuth result = userAuthRepositoryAdapter.update(userId, updatedUserAuth);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getPasswordHash()).isEqualTo("$2a$10$newHashedPassword");
        assertThat(result.getRole()).isEqualTo(Role.PROFESSOR);

        verify(mongoRepository, times(1)).findById(userId);
        verify(mongoRepository, times(1)).save(any(UserAuthDocument.class));
        verify(userAuthMapper, times(1)).toDomain(updatedDocument);
    }

    @Test
    @DisplayName("Should exception when updating not existent UserAuth")
    void shouldThrowExceptionWhenUpdatingNonExistentUserAuth() {
        String userId = "noExiste";
        UserAuth updatedUserAuth = UserAuth.builder()
                .id(userId)
                .passwordHash("$2a$10$newHashedPassword")
                .role(Role.PROFESSOR)
                .build();

        when(mongoRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAuthRepositoryAdapter.update(userId, updatedUserAuth))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");

        verify(mongoRepository, times(1)).findById(userId);
        verify(mongoRepository, never()).save(any());
        verify(userAuthMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should save UserAuth with null userId")
    void shouldSaveUserAuthWithNullUserId() {
        UserAuth userAuthWithNullUserId = UserAuth.builder()
                .id("ididid456")
                .email("diego-c@mail.escuelaing.edu.co")
                .institutionalId(1000100999L)
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.STUDENT)
                .userId(null)
                .createdAt(now)
                .lastLogin(null)
                .build();

        UserAuthDocument documentWithNullUserId = UserAuthDocument.builder()
                .id("ididid456")
                .email("diego-c@mail.escuelaing.edu.co")
                .institutionalId(1000100999L)
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.STUDENT)
                .userId(null)
                .createdAt(now)
                .lastLogin(null)
                .build();

        when(mongoRepository.save(any(UserAuthDocument.class))).thenReturn(documentWithNullUserId);
        when(userAuthMapper.toDomain(documentWithNullUserId)).thenReturn(userAuthWithNullUserId);

        UserAuth result = userAuthRepositoryAdapter.save(userAuthWithNullUserId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isNull();
        assertThat(result.getEmail()).isEqualTo("diego-c@mail.escuelaing.edu.co");

        verify(mongoRepository, times(1)).save(any(UserAuthDocument.class));
        verify(userAuthMapper, times(1)).toDomain(documentWithNullUserId);
    }

    @Test
    @DisplayName("Should save UserAuth with PROFESSOR role")
    void shouldSaveUserAuthWithProfessorRole() {
        UserAuth professor = UserAuth.builder()
                .id("ididid789")
                .email("adriana.pinzon@escuelaing.edu.co")
                .institutionalId(3120321777L)
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.PROFESSOR)
                .userId(null)
                .createdAt(now)
                .lastLogin(now)
                .build();

        UserAuthDocument professorDocument = UserAuthDocument.builder()
                .id("ididid789")
                .email("adriana.pinzon@escuelaing.edu.co")
                .institutionalId(3120321777L)
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.PROFESSOR)
                .userId(null)
                .createdAt(now)
                .lastLogin(now)
                .build();

        when(mongoRepository.save(any(UserAuthDocument.class))).thenReturn(professorDocument);
        when(userAuthMapper.toDomain(professorDocument)).thenReturn(professor);

        UserAuth result = userAuthRepositoryAdapter.save(professor);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.PROFESSOR);
        assertThat(result.getEmail()).isEqualTo("adriana.pinzon@escuelaing.edu.co");

        verify(mongoRepository, times(1)).save(any(UserAuthDocument.class));
        verify(userAuthMapper, times(1)).toDomain(professorDocument);
    }

    @Test
    @DisplayName("Should update only password and role")
    void shouldUpdateOnlyPasswordAndRole() {
        // Given
        String userId = "idididid123";
        String originalEmail = "david.palacios-p@mail.escuelaing.edu.co";
        Long originalInstitutionalId = 1000100282L;
        LocalDateTime originalCreatedAt = now;

        UserAuth updatedUserAuth = UserAuth.builder()
                .passwordHash("$2a$10$newHashedPassword")
                .role(Role.ADMINISTRATOR)
                .build();

        when(mongoRepository.findById(userId)).thenReturn(Optional.of(userAuthDocument));
        when(mongoRepository.save(any(UserAuthDocument.class))).thenAnswer(invocation -> {
            UserAuthDocument doc = invocation.getArgument(0);

            assertThat(doc.getEmail()).isEqualTo(originalEmail);
            assertThat(doc.getInstitutionalId()).isEqualTo(originalInstitutionalId);
            assertThat(doc.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(doc.getPasswordHash()).isEqualTo("$2a$10$newHashedPassword");
            assertThat(doc.getRole()).isEqualTo(Role.ADMINISTRATOR);
            return doc;
        });
        when(userAuthMapper.toDomain(any(UserAuthDocument.class))).thenReturn(userAuth);

        UserAuth result = userAuthRepositoryAdapter.update(userId, updatedUserAuth);

        verify(mongoRepository, times(1)).findById(userId);
        verify(mongoRepository, times(1)).save(any(UserAuthDocument.class));
    }
    @Test
    @DisplayName("Should delete UserAuth - Success")
    void shouldDeleteUserAuth() {
        String userId = "idididid123";
        doNothing().when(mongoRepository).deleteById(userId);

        userAuthRepositoryAdapter.delete(userAuth);

        verify(mongoRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Should delete UserAuth by id without exception")
    void shouldDeleteUserAuthWithoutException() {
        String userId = "idididid123";
        UserAuth userAuthToDelete = UserAuth.builder()
                .id(userId)
                .email("david.palacios-p@mail.escuelaing.edu.co")
                .build();

        doNothing().when(mongoRepository).deleteById(userId);

        userAuthRepositoryAdapter.delete(userAuthToDelete);

        verify(mongoRepository, times(1)).deleteById(userId);
        verifyNoMoreInteractions(mongoRepository);
    }

    @Test
    @DisplayName("Should attempt to delete even if UserAuth does not exist")
    void shouldAttemptDeleteNonExistentUserAuth() {
        String userId = "noExiste";
        UserAuth nonExistentUserAuth = UserAuth.builder()
                .id(userId)
                .email("inexistente@mail.escuelaing.edu.co")
                .build();

        doNothing().when(mongoRepository).deleteById(userId);

        userAuthRepositoryAdapter.delete(nonExistentUserAuth);

        verify(mongoRepository, times(1)).deleteById(userId);
    }
}