package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.langia.backend.dto.EnrollLanguageRequest;
import com.langia.backend.dto.LanguageDTO;
import com.langia.backend.dto.LanguageEnrollmentDTO;
import com.langia.backend.dto.UpdateLanguageEnrollmentRequest;
import com.langia.backend.exception.BusinessException;
import com.langia.backend.exception.ResourceNotFoundException;
import com.langia.backend.model.Language;
import com.langia.backend.model.Profile;
import com.langia.backend.model.StudentLanguageEnrollment;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.LanguageRepository;
import com.langia.backend.repository.StudentLanguageEnrollmentRepository;
import com.langia.backend.repository.UserRepository;

/**
 * Testes para o serviço de idiomas de estudantes.
 */
@ExtendWith(MockitoExtension.class)
class StudentLanguageServiceTest {

    @Mock
    private StudentLanguageEnrollmentRepository enrollmentRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentLanguageService studentLanguageService;

    private User testUser;
    private Language testLanguage;
    private StudentLanguageEnrollment testEnrollment;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        Profile studentProfile = Profile.builder()
                .id(UUID.randomUUID())
                .code(UserProfile.STUDENT)
                .name("Student")
                .hierarchyLevel(1)
                .active(true)
                .build();

        testUser = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .profile(studentProfile)
                .build();

        testLanguage = Language.builder()
                .code("en")
                .namePt("Inglês")
                .nameEn("English")
                .active(true)
                .build();

        testEnrollment = StudentLanguageEnrollment.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .language(testLanguage)
                .cefrLevel("B1")
                .isPrimary(true)
                .build();
    }

    // ========== Testes de Listagem de Idiomas ==========

    @Test
    void deveListarIdiomasDisponiveis() {
        // Arrange
        when(languageRepository.findByActiveTrueOrderByNamePtAsc())
                .thenReturn(List.of(testLanguage));

        // Act
        List<LanguageDTO> result = studentLanguageService.getAvailableLanguages();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("en", result.get(0).getCode());
    }

    @Test
    void deveListarEnrollmentsDoUsuario() {
        // Arrange
        when(enrollmentRepository.findByUserIdOrderByPrimaryFirst(userId))
                .thenReturn(List.of(testEnrollment));

        // Act
        List<LanguageEnrollmentDTO> result = studentLanguageService.getEnrollments(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isPrimary());
    }

    // ========== Testes de Enrollment ==========

    @Test
    void deveFazerEnrollmentComSucesso() {
        // Arrange
        EnrollLanguageRequest request = new EnrollLanguageRequest();
        request.setLanguageCode("en");
        request.setCefrLevel("A1");
        request.setPrimary(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(languageRepository.findById("en")).thenReturn(Optional.of(testLanguage));
        when(enrollmentRepository.existsByUserIdAndLanguageCode(userId, "en")).thenReturn(false);
        when(enrollmentRepository.countByUserId(userId)).thenReturn(0L);
        when(enrollmentRepository.save(any(StudentLanguageEnrollment.class))).thenReturn(testEnrollment);

        // Act
        LanguageEnrollmentDTO result = studentLanguageService.enroll(userId, request);

        // Assert
        assertNotNull(result);
        verify(enrollmentRepository).save(any(StudentLanguageEnrollment.class));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        // Arrange
        EnrollLanguageRequest request = new EnrollLanguageRequest();
        request.setLanguageCode("en");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            studentLanguageService.enroll(userId, request);
        });
    }

    @Test
    void deveLancarExcecaoQuandoIdiomaNaoExiste() {
        // Arrange
        EnrollLanguageRequest request = new EnrollLanguageRequest();
        request.setLanguageCode("xx");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(languageRepository.findById("xx")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            studentLanguageService.enroll(userId, request);
        });
    }

    @Test
    void deveLancarExcecaoQuandoIdiomaInativo() {
        // Arrange
        testLanguage.setActive(false);
        EnrollLanguageRequest request = new EnrollLanguageRequest();
        request.setLanguageCode("en");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(languageRepository.findById("en")).thenReturn(Optional.of(testLanguage));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            studentLanguageService.enroll(userId, request);
        });
    }

    @Test
    void deveLancarExcecaoQuandoJaEnrolled() {
        // Arrange
        EnrollLanguageRequest request = new EnrollLanguageRequest();
        request.setLanguageCode("en");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(languageRepository.findById("en")).thenReturn(Optional.of(testLanguage));
        when(enrollmentRepository.existsByUserIdAndLanguageCode(userId, "en")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            studentLanguageService.enroll(userId, request);
        });
    }

    @Test
    void deveLancarExcecaoQuandoLimiteDeIdiomasAtingido() {
        // Arrange
        EnrollLanguageRequest request = new EnrollLanguageRequest();
        request.setLanguageCode("en");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(languageRepository.findById("en")).thenReturn(Optional.of(testLanguage));
        when(enrollmentRepository.existsByUserIdAndLanguageCode(userId, "en")).thenReturn(false);
        when(enrollmentRepository.countByUserId(userId)).thenReturn(3L);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            studentLanguageService.enroll(userId, request);
        });
        assertTrue(exception.getMessage().contains("3"));
    }

    // ========== Testes de Update ==========

    @Test
    void deveAtualizarEnrollmentComSucesso() {
        // Arrange
        UpdateLanguageEnrollmentRequest request = new UpdateLanguageEnrollmentRequest();
        request.setCefrLevel("B2");

        when(enrollmentRepository.findByUserIdAndLanguageCode(userId, "en"))
                .thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any(StudentLanguageEnrollment.class)))
                .thenReturn(testEnrollment);

        // Act
        LanguageEnrollmentDTO result = studentLanguageService.updateEnrollment(userId, "en", request);

        // Assert
        assertNotNull(result);
        verify(enrollmentRepository).save(any(StudentLanguageEnrollment.class));
    }

    @Test
    void deveLancarExcecaoQuandoEnrollmentNaoExiste() {
        // Arrange
        UpdateLanguageEnrollmentRequest request = new UpdateLanguageEnrollmentRequest();

        when(enrollmentRepository.findByUserIdAndLanguageCode(userId, "xx"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            studentLanguageService.updateEnrollment(userId, "xx", request);
        });
    }

    // ========== Testes de Unenroll ==========

    @Test
    void deveRemoverEnrollmentComSucesso() {
        // Arrange
        testEnrollment.setPrimary(false);
        when(enrollmentRepository.findByUserIdAndLanguageCode(userId, "en"))
                .thenReturn(Optional.of(testEnrollment));

        // Act
        studentLanguageService.unenroll(userId, "en");

        // Assert
        verify(enrollmentRepository).delete(testEnrollment);
    }

    @Test
    void devePromoverOutroIdiomaoQuandoRemovePrimario() {
        // Arrange
        StudentLanguageEnrollment secondEnrollment = StudentLanguageEnrollment.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .language(Language.builder().code("es").build())
                .isPrimary(false)
                .build();

        when(enrollmentRepository.findByUserIdAndLanguageCode(userId, "en"))
                .thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.findByUserId(userId))
                .thenReturn(List.of(testEnrollment, secondEnrollment));

        // Act
        studentLanguageService.unenroll(userId, "en");

        // Assert
        verify(enrollmentRepository).save(secondEnrollment);
        verify(enrollmentRepository).delete(testEnrollment);
    }

    // ========== Testes de Set Primary ==========

    @Test
    void deveDefinirPrimarioComSucesso() {
        // Arrange
        when(enrollmentRepository.findByUserIdAndLanguageCode(userId, "en"))
                .thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any(StudentLanguageEnrollment.class)))
                .thenReturn(testEnrollment);

        // Act
        LanguageEnrollmentDTO result = studentLanguageService.setPrimary(userId, "en");

        // Assert
        assertNotNull(result);
        verify(enrollmentRepository).save(testEnrollment);
    }
}
