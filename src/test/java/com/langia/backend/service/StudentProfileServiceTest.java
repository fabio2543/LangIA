package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.langia.backend.dto.CategoryPreference;
import com.langia.backend.dto.LearningPreferencesDTO;
import com.langia.backend.dto.NotificationSettingsDTO;
import com.langia.backend.dto.SkillAssessmentDTO;
import com.langia.backend.dto.SkillAssessmentResponseDTO;
import com.langia.backend.dto.student.PersonalDataDTO;
import com.langia.backend.dto.student.UpdatePersonalDataRequest;
import com.langia.backend.exception.UserNotFoundException;
import com.langia.backend.model.CefrLevel;
import com.langia.backend.model.DifficultyLevel;
import com.langia.backend.model.LearningObjective;
import com.langia.backend.model.NotificationCategory;
import com.langia.backend.model.NotificationChannel;
import com.langia.backend.model.NotificationSettingsEntity;
import com.langia.backend.model.ReminderFrequency;
import com.langia.backend.model.StudentLearningPreferences;
import com.langia.backend.model.StudentSkillAssessment;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfileDetails;
import com.langia.backend.repository.NotificationSettingsRepository;
import com.langia.backend.repository.StudentLearningPreferencesRepository;
import com.langia.backend.repository.StudentSkillAssessmentRepository;
import com.langia.backend.repository.UserProfileDetailsRepository;
import com.langia.backend.repository.UserRepository;

/**
 * Testes para o serviço de perfil do estudante.
 * Implementa verificações dos critérios de aceite AC-DP-001 a AC-DP-004.
 */
@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileDetailsRepository profileDetailsRepository;

    @Mock
    private StudentLearningPreferencesRepository preferencesRepository;

    @Mock
    private StudentSkillAssessmentRepository assessmentRepository;

    @Mock
    private NotificationSettingsRepository notificationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private StudentProfileService studentProfileService;

    private UUID userId;
    private User testUser;
    private UserProfileDetails testDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .name("João Silva")
                .email("joao.silva@example.com")
                .phone("11987654321")
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now().minusDays(30))
                .createdAt(LocalDateTime.now().minusMonths(6))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        testDetails = UserProfileDetails.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .birthDate(LocalDate.of(2000, 5, 15))
                .nativeLanguage("Português")
                .timezone("America/Sao_Paulo")
                .bio("Estudante de idiomas")
                .build();
    }

    // ========== AC-DP-001: Visualização de dados pessoais ==========

    @Nested
    @DisplayName("AC-DP-001: Visualização de dados pessoais")
    class VisualizacaoDadosPessoais {

        @Test
        @DisplayName("AC-DP-001: Deve retornar dados pessoais completos do estudante")
        void deveRetornarDadosPessoaisCompletos() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));

            // Act
            PersonalDataDTO result = studentProfileService.getPersonalData(userId);

            // Assert
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("João Silva", result.getName());
            assertEquals("joao.silva@example.com", result.getEmail());
            assertEquals("11987654321", result.getPhone());
            assertEquals(LocalDate.of(2000, 5, 15), result.getBirthDate());
            assertEquals("Português", result.getNativeLanguage());
            assertEquals("America/Sao_Paulo", result.getTimezone());
            assertEquals("Estudante de idiomas", result.getBio());
            assertEquals(true, result.isEmailVerified());
            assertNotNull(result.getEmailVerifiedAt());
            assertNotNull(result.getCreatedAt());
            assertNotNull(result.getUpdatedAt());
        }

        @Test
        @DisplayName("AC-DP-001: Deve retornar dados mesmo sem perfil detalhado")
        void deveRetornarDadosMesmoSemPerfilDetalhado() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // Act
            PersonalDataDTO result = studentProfileService.getPersonalData(userId);

            // Assert
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("João Silva", result.getName());
            assertNull(result.getBirthDate());
            assertNull(result.getNativeLanguage());
            assertEquals("America/Sao_Paulo", result.getTimezone()); // Default
            assertNull(result.getBio());
        }

        @Test
        @DisplayName("AC-DP-001: Deve lançar exceção para usuário inexistente")
        void deveLancarExcecaoParaUsuarioInexistente() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UserNotFoundException.class,
                    () -> studentProfileService.getPersonalData(userId));
        }
    }

    // ========== AC-DP-002: Atualização de nome válido ==========

    @Nested
    @DisplayName("AC-DP-002: Atualização de nome válido")
    class AtualizacaoNomeValido {

        @Test
        @DisplayName("AC-DP-002: Deve atualizar nome válido com sucesso")
        void deveAtualizarNomeValidoComSucesso() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileDetailsRepository.save(any(UserProfileDetails.class))).thenReturn(testDetails);

            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .name("Maria Santos")
                    .build();

            // Act
            PersonalDataDTO result = studentProfileService.updatePersonalData(userId, request);

            // Assert
            assertNotNull(result);
            verify(userRepository).save(any(User.class));
            verify(auditService).logUpdate(eq("USER_PERSONAL_DATA"), eq(userId), any(), any(), eq(userId));
        }

        @Test
        @DisplayName("AC-DP-002: Deve atualizar apenas campos presentes na requisição")
        void deveAtualizarApenasOsCamposPresentes() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileDetailsRepository.save(any(UserProfileDetails.class))).thenReturn(testDetails);

            // Apenas atualiza o nome
            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .name("Novo Nome")
                    .build();

            // Act
            studentProfileService.updatePersonalData(userId, request);

            // Assert - os outros campos devem permanecer inalterados
            verify(userRepository).save(any(User.class));
            assertEquals("Novo Nome", testUser.getName());
            // birthDate e outros não devem ser alterados
        }

        @Test
        @DisplayName("AC-DP-002: Deve criar perfil detalhado se não existir")
        void deveCriarPerfilDetalhadoSeNaoExistir() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileDetailsRepository.save(any(UserProfileDetails.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .bio("Nova bio")
                    .build();

            // Act
            PersonalDataDTO result = studentProfileService.updatePersonalData(userId, request);

            // Assert
            assertNotNull(result);
            verify(profileDetailsRepository).save(any(UserProfileDetails.class));
        }

        @Test
        @DisplayName("AC-DP-002: Deve atualizar múltiplos campos simultaneamente")
        void deveAtualizarMultiplosCamposSimultaneamente() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileDetailsRepository.save(any(UserProfileDetails.class))).thenReturn(testDetails);

            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .name("Novo Nome Completo")
                    .birthDate(LocalDate.of(1995, 3, 20))
                    .nativeLanguage("English")
                    .timezone("America/New_York")
                    .bio("Nova biografia")
                    .build();

            // Act
            studentProfileService.updatePersonalData(userId, request);

            // Assert
            assertEquals("Novo Nome Completo", testUser.getName());
            assertEquals(LocalDate.of(1995, 3, 20), testDetails.getBirthDate());
            assertEquals("English", testDetails.getNativeLanguage());
            assertEquals("America/New_York", testDetails.getTimezone());
            assertEquals("Nova biografia", testDetails.getBio());
        }
    }

    // ========== AC-DP-003: Rejeição de nome inválido ==========

    @Nested
    @DisplayName("AC-DP-003: Validação de nome")
    class ValidacaoNome {

        @Test
        @DisplayName("AC-DP-003: Deve aceitar nome com acentos")
        void deveAceitarNomeComAcentos() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileDetailsRepository.save(any(UserProfileDetails.class))).thenReturn(testDetails);

            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .name("José María Ñoño")
                    .build();

            // Act
            PersonalDataDTO result = studentProfileService.updatePersonalData(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals("José María Ñoño", testUser.getName());
        }

        @Test
        @DisplayName("AC-DP-003: Deve aceitar nome com espaços")
        void deveAceitarNomeComEspacos() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileDetailsRepository.save(any(UserProfileDetails.class))).thenReturn(testDetails);

            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .name("Maria da Silva Santos")
                    .build();

            // Act
            PersonalDataDTO result = studentProfileService.updatePersonalData(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals("Maria da Silva Santos", testUser.getName());
        }
    }

    // ========== AC-DP-004: Validação de idade mínima ==========

    @Nested
    @DisplayName("AC-DP-004: Validação de idade mínima")
    class ValidacaoIdadeMinima {

        @Test
        @DisplayName("AC-DP-004: Deve aceitar data de nascimento com idade >= 13")
        void deveAceitarDataNascimentoIdadeValida() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileDetailsRepository.save(any(UserProfileDetails.class))).thenReturn(testDetails);

            // 20 anos de idade
            LocalDate birthDate = LocalDate.now().minusYears(20);

            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .birthDate(birthDate)
                    .build();

            // Act
            PersonalDataDTO result = studentProfileService.updatePersonalData(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(birthDate, testDetails.getBirthDate());
        }

        @Test
        @DisplayName("AC-DP-004: Deve aceitar exatamente 13 anos")
        void deveAceitarExatamente13Anos() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileDetailsRepository.save(any(UserProfileDetails.class))).thenReturn(testDetails);

            LocalDate birthDate = LocalDate.now().minusYears(13);

            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .birthDate(birthDate)
                    .build();

            // Act
            PersonalDataDTO result = studentProfileService.updatePersonalData(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(birthDate, testDetails.getBirthDate());
        }
    }

    // ========== Auditoria ==========

    @Nested
    @DisplayName("Auditoria de alterações")
    class AuditoriaAlteracoes {

        @Test
        @DisplayName("Deve registrar auditoria ao atualizar dados pessoais")
        void deveRegistrarAuditoriaAoAtualizar() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileDetailsRepository.save(any(UserProfileDetails.class))).thenReturn(testDetails);

            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .name("Novo Nome")
                    .build();

            // Act
            studentProfileService.updatePersonalData(userId, request);

            // Assert
            verify(auditService).logUpdate(
                    eq("USER_PERSONAL_DATA"),
                    eq(userId),
                    any(PersonalDataDTO.class),
                    any(PersonalDataDTO.class),
                    eq(userId)
            );
        }

        @Test
        @DisplayName("Não deve registrar auditoria ao apenas consultar dados")
        void naoDeveRegistrarAuditoriaAoConsultar() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(profileDetailsRepository.findByUserId(userId)).thenReturn(Optional.of(testDetails));

            // Act
            studentProfileService.getPersonalData(userId);

            // Assert
            verify(auditService, never()).logUpdate(any(), any(), any(), any(), any());
        }
    }

    // ========== Erro - Usuário não encontrado ==========

    @Nested
    @DisplayName("Tratamento de erros")
    class TratamentoErros {

        @Test
        @DisplayName("Deve lançar exceção ao atualizar usuário inexistente")
        void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            UpdatePersonalDataRequest request = UpdatePersonalDataRequest.builder()
                    .name("Qualquer Nome")
                    .build();

            // Act & Assert
            assertThrows(UserNotFoundException.class,
                    () -> studentProfileService.updatePersonalData(userId, request));
        }
    }

    // ========== AC-LP-001: Visualização de preferências de aprendizado ==========

    @Nested
    @DisplayName("AC-LP-001: Visualização de preferências de aprendizado")
    class VisualizacaoPreferenciasAprendizado {

        @Test
        @DisplayName("AC-LP-001: Deve retornar preferências de aprendizado do estudante")
        void deveRetornarPreferenciasDeAprendizado() {
            // Arrange
            StudentLearningPreferences prefs = StudentLearningPreferences.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .dailyTimeAvailable("MIN_30")
                    .preferredDays(List.of("MONDAY", "WEDNESDAY", "FRIDAY"))
                    .preferredTimes(List.of("MORNING", "EVENING"))
                    .weeklyHoursGoal(10)
                    .topicsOfInterest(List.of("TRAVEL", "BUSINESS"))
                    .customTopics(List.of("Technology"))
                    .preferredFormats(List.of("VIDEO", "AUDIO"))
                    .formatRanking(List.of("VIDEO", "AUDIO", "TEXT"))
                    .primaryObjective(LearningObjective.CAREER)
                    .objectiveDescription("Improve for work")
                    .objectiveDeadline("6_MONTHS")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(prefs));

            // Act
            LearningPreferencesDTO result = studentProfileService.getLearningPreferences(userId);

            // Assert
            assertNotNull(result);
            assertEquals("MIN_30", result.getDailyTimeAvailable());
            assertEquals(3, result.getPreferredDays().size());
            assertEquals(2, result.getPreferredTimes().size());
            assertEquals(10, result.getWeeklyHoursGoal());
            assertEquals(LearningObjective.CAREER, result.getPrimaryObjective());
        }

        @Test
        @DisplayName("AC-LP-001: Deve retornar preferências vazias se não existirem")
        void deveRetornarPreferenciasVaziaSeNaoExistirem() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // Act
            LearningPreferencesDTO result = studentProfileService.getLearningPreferences(userId);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getPreferredDays());
            assertEquals(0, result.getPreferredDays().size());
        }

        @Test
        @DisplayName("AC-LP-001: Deve lançar exceção para usuário inexistente")
        void deveLancarExcecaoParaUsuarioInexistente() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UserNotFoundException.class,
                    () -> studentProfileService.getLearningPreferences(userId));
        }
    }

    // ========== AC-LP-002: Atualização de preferências ==========

    @Nested
    @DisplayName("AC-LP-002: Atualização de preferências de aprendizado")
    class AtualizacaoPreferenciasAprendizado {

        @Test
        @DisplayName("AC-LP-002: Deve atualizar preferências com sucesso")
        void deveAtualizarPreferenciasComSucesso() {
            // Arrange
            StudentLearningPreferences existingPrefs = StudentLearningPreferences.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .dailyTimeAvailable("MIN_30")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(existingPrefs));
            when(preferencesRepository.save(any(StudentLearningPreferences.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LearningPreferencesDTO request = LearningPreferencesDTO.builder()
                    .dailyTimeAvailable("HOUR_1")
                    .preferredDays(List.of("MONDAY", "TUESDAY"))
                    .weeklyHoursGoal(15)
                    .primaryObjective(LearningObjective.HOBBY)
                    .build();

            // Act
            LearningPreferencesDTO result = studentProfileService.updateLearningPreferences(userId, request);

            // Assert
            assertNotNull(result);
            verify(preferencesRepository).save(any(StudentLearningPreferences.class));
            verify(auditService).logUpdate(eq("LEARNING_PREFERENCES"), eq(userId), any(), any(), eq(userId));
        }

        @Test
        @DisplayName("AC-LP-002: Deve criar preferências se não existirem")
        void deveCriarPreferenciaSeNaoExistirem() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.save(any(StudentLearningPreferences.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LearningPreferencesDTO request = LearningPreferencesDTO.builder()
                    .dailyTimeAvailable("HOUR_2")
                    .preferredDays(List.of("SATURDAY", "SUNDAY"))
                    .weeklyHoursGoal(20)
                    .build();

            // Act
            LearningPreferencesDTO result = studentProfileService.updateLearningPreferences(userId, request);

            // Assert
            assertNotNull(result);
            verify(preferencesRepository).save(any(StudentLearningPreferences.class));
        }

        @Test
        @DisplayName("AC-LP-002: Deve registrar auditoria ao atualizar preferências")
        void deveRegistrarAuditoriaAoAtualizarPreferencias() {
            // Arrange
            StudentLearningPreferences existingPrefs = StudentLearningPreferences.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(existingPrefs));
            when(preferencesRepository.save(any(StudentLearningPreferences.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LearningPreferencesDTO request = LearningPreferencesDTO.builder()
                    .dailyTimeAvailable("MIN_30")
                    .build();

            // Act
            studentProfileService.updateLearningPreferences(userId, request);

            // Assert
            verify(auditService).logUpdate(
                    eq("LEARNING_PREFERENCES"),
                    eq(userId),
                    any(LearningPreferencesDTO.class),
                    any(LearningPreferencesDTO.class),
                    eq(userId)
            );
        }
    }

    // ========== AC-LP-003: Validação de preferências ==========

    @Nested
    @DisplayName("AC-LP-003: Validação de preferências")
    class ValidacaoPreferencias {

        @Test
        @DisplayName("AC-LP-003: Deve aceitar horas semanais válidas (1-168)")
        void deveAceitarHorasSemanaisValidas() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.save(any(StudentLearningPreferences.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LearningPreferencesDTO request = LearningPreferencesDTO.builder()
                    .weeklyHoursGoal(40) // Valor válido
                    .build();

            // Act
            LearningPreferencesDTO result = studentProfileService.updateLearningPreferences(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(40, result.getWeeklyHoursGoal());
        }

        @Test
        @DisplayName("AC-LP-003: Deve aceitar múltiplos dias preferidos")
        void deveAceitarMultiplosDiasPreferidos() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.save(any(StudentLearningPreferences.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LearningPreferencesDTO request = LearningPreferencesDTO.builder()
                    .preferredDays(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"))
                    .build();

            // Act
            LearningPreferencesDTO result = studentProfileService.updateLearningPreferences(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.getPreferredDays().size());
        }

        @Test
        @DisplayName("AC-LP-003: Deve aceitar múltiplos formatos preferidos")
        void deveAceitarMultiplosFormatosPreferidos() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.save(any(StudentLearningPreferences.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LearningPreferencesDTO request = LearningPreferencesDTO.builder()
                    .preferredFormats(List.of("VIDEO", "AUDIO", "TEXT", "INTERACTIVE"))
                    .formatRanking(List.of("VIDEO", "INTERACTIVE", "AUDIO", "TEXT"))
                    .build();

            // Act
            LearningPreferencesDTO result = studentProfileService.updateLearningPreferences(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(4, result.getPreferredFormats().size());
            assertEquals(4, result.getFormatRanking().size());
        }

        @Test
        @DisplayName("AC-LP-003: Deve aceitar objetivo de aprendizado válido")
        void deveAceitarObjetivoValido() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.save(any(StudentLearningPreferences.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LearningPreferencesDTO request = LearningPreferencesDTO.builder()
                    .primaryObjective(LearningObjective.TRAVEL)
                    .objectiveDescription("Want to travel to Japan")
                    .objectiveDeadline("1_YEAR")
                    .build();

            // Act
            LearningPreferencesDTO result = studentProfileService.updateLearningPreferences(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(LearningObjective.TRAVEL, result.getPrimaryObjective());
            assertEquals("Want to travel to Japan", result.getObjectiveDescription());
        }
    }

    // ========== AC-SA-001: Visualização de autoavaliações ==========

    @Nested
    @DisplayName("AC-SA-001: Visualização de autoavaliações")
    class VisualizacaoAutoavaliacoes {

        @Test
        @DisplayName("AC-SA-001: Deve retornar lista de autoavaliações do estudante")
        void deveRetornarListaDeAutoavaliacoes() {
            // Arrange
            StudentSkillAssessment assessment = StudentSkillAssessment.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .language("English")
                    .listeningDifficulty(DifficultyLevel.MODERATE)
                    .speakingDifficulty(DifficultyLevel.HIGH)
                    .readingDifficulty(DifficultyLevel.LOW)
                    .writingDifficulty(DifficultyLevel.MODERATE)
                    .selfCefrLevel(CefrLevel.B1)
                    .assessedAt(LocalDateTime.now())
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(assessmentRepository.findByUserIdOrderByAssessedAtDesc(userId))
                    .thenReturn(List.of(assessment));

            // Act
            List<SkillAssessmentResponseDTO> result = studentProfileService.getSkillAssessments(userId);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("English", result.get(0).getLanguage());
            assertEquals(DifficultyLevel.MODERATE, result.get(0).getListeningDifficulty());
            assertEquals(CefrLevel.B1, result.get(0).getSelfCefrLevel());
        }

        @Test
        @DisplayName("AC-SA-001: Deve retornar lista vazia se não houver avaliações")
        void deveRetornarListaVaziaSemAvaliacoes() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(assessmentRepository.findByUserIdOrderByAssessedAtDesc(userId))
                    .thenReturn(List.of());

            // Act
            List<SkillAssessmentResponseDTO> result = studentProfileService.getSkillAssessments(userId);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("AC-SA-001: Deve lançar exceção para usuário inexistente")
        void deveLancarExcecaoParaUsuarioInexistente() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UserNotFoundException.class,
                    () -> studentProfileService.getSkillAssessments(userId));
        }
    }

    // ========== AC-SA-002: Criação de autoavaliação ==========

    @Nested
    @DisplayName("AC-SA-002: Criação de autoavaliação")
    class CriacaoAutoavaliacao {

        @Test
        @DisplayName("AC-SA-002: Deve criar autoavaliação com sucesso")
        void deveCriarAutoavaliacaoComSucesso() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(assessmentRepository.save(any(StudentSkillAssessment.class)))
                    .thenAnswer(invocation -> {
                        StudentSkillAssessment saved = invocation.getArgument(0);
                        saved.setId(UUID.randomUUID());
                        saved.setAssessedAt(LocalDateTime.now());
                        return saved;
                    });

            SkillAssessmentDTO request = new SkillAssessmentDTO();
            request.setLanguage("Spanish");
            request.setListeningDifficulty(DifficultyLevel.LOW);
            request.setSpeakingDifficulty(DifficultyLevel.MODERATE);
            request.setReadingDifficulty(DifficultyLevel.LOW);
            request.setWritingDifficulty(DifficultyLevel.HIGH);
            request.setSelfCefrLevel(CefrLevel.A2);

            // Act
            SkillAssessmentResponseDTO result = studentProfileService.createSkillAssessment(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals("Spanish", result.getLanguage());
            assertEquals(DifficultyLevel.LOW, result.getListeningDifficulty());
            assertEquals(CefrLevel.A2, result.getSelfCefrLevel());
            verify(assessmentRepository).save(any(StudentSkillAssessment.class));
        }

        @Test
        @DisplayName("AC-SA-002: Deve registrar auditoria ao criar autoavaliação")
        void deveRegistrarAuditoriaAoCriarAutoavaliacao() {
            // Arrange
            UUID assessmentId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(assessmentRepository.save(any(StudentSkillAssessment.class)))
                    .thenAnswer(invocation -> {
                        StudentSkillAssessment saved = invocation.getArgument(0);
                        saved.setId(assessmentId);
                        saved.setAssessedAt(LocalDateTime.now());
                        return saved;
                    });

            SkillAssessmentDTO request = new SkillAssessmentDTO();
            request.setLanguage("French");
            request.setListeningDifficulty(DifficultyLevel.NONE);
            request.setSpeakingDifficulty(DifficultyLevel.NONE);
            request.setReadingDifficulty(DifficultyLevel.LOW);
            request.setWritingDifficulty(DifficultyLevel.NONE);

            // Act
            studentProfileService.createSkillAssessment(userId, request);

            // Assert
            verify(auditService).logCreate(
                    eq("SKILL_ASSESSMENT"),
                    eq(assessmentId),
                    any(SkillAssessmentResponseDTO.class),
                    eq(userId)
            );
        }
    }

    // ========== AC-SA-003: Validação de autoavaliação ==========

    @Nested
    @DisplayName("AC-SA-003: Validação de autoavaliação")
    class ValidacaoAutoavaliacao {

        @Test
        @DisplayName("AC-SA-003: Deve aceitar todos os níveis de dificuldade")
        void deveAceitarTodosNiveisDificuldade() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(assessmentRepository.save(any(StudentSkillAssessment.class)))
                    .thenAnswer(invocation -> {
                        StudentSkillAssessment saved = invocation.getArgument(0);
                        saved.setId(UUID.randomUUID());
                        saved.setAssessedAt(LocalDateTime.now());
                        return saved;
                    });

            SkillAssessmentDTO request = new SkillAssessmentDTO();
            request.setLanguage("German");
            request.setListeningDifficulty(DifficultyLevel.NONE);
            request.setSpeakingDifficulty(DifficultyLevel.LOW);
            request.setReadingDifficulty(DifficultyLevel.MODERATE);
            request.setWritingDifficulty(DifficultyLevel.HIGH);
            request.setSelfCefrLevel(CefrLevel.B2);

            // Act
            SkillAssessmentResponseDTO result = studentProfileService.createSkillAssessment(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(DifficultyLevel.NONE, result.getListeningDifficulty());
            assertEquals(DifficultyLevel.LOW, result.getSpeakingDifficulty());
            assertEquals(DifficultyLevel.MODERATE, result.getReadingDifficulty());
            assertEquals(DifficultyLevel.HIGH, result.getWritingDifficulty());
        }

        @Test
        @DisplayName("AC-SA-003: Deve aceitar todos os níveis CEFR")
        void deveAceitarTodosNiveisCEFR() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(assessmentRepository.save(any(StudentSkillAssessment.class)))
                    .thenAnswer(invocation -> {
                        StudentSkillAssessment saved = invocation.getArgument(0);
                        saved.setId(UUID.randomUUID());
                        saved.setAssessedAt(LocalDateTime.now());
                        return saved;
                    });

            SkillAssessmentDTO request = new SkillAssessmentDTO();
            request.setLanguage("Italian");
            request.setListeningDifficulty(DifficultyLevel.LOW);
            request.setSpeakingDifficulty(DifficultyLevel.LOW);
            request.setReadingDifficulty(DifficultyLevel.LOW);
            request.setWritingDifficulty(DifficultyLevel.LOW);
            request.setSelfCefrLevel(CefrLevel.C1);

            // Act
            SkillAssessmentResponseDTO result = studentProfileService.createSkillAssessment(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(CefrLevel.C1, result.getSelfCefrLevel());
        }

        @Test
        @DisplayName("AC-SA-003: Deve aceitar detalhes de habilidades")
        void deveAceitarDetalhesHabilidades() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(assessmentRepository.save(any(StudentSkillAssessment.class)))
                    .thenAnswer(invocation -> {
                        StudentSkillAssessment saved = invocation.getArgument(0);
                        saved.setId(UUID.randomUUID());
                        saved.setAssessedAt(LocalDateTime.now());
                        return saved;
                    });

            SkillAssessmentDTO request = new SkillAssessmentDTO();
            request.setLanguage("Japanese");
            request.setListeningDifficulty(DifficultyLevel.HIGH);
            request.setSpeakingDifficulty(DifficultyLevel.HIGH);
            request.setReadingDifficulty(DifficultyLevel.HIGH);
            request.setWritingDifficulty(DifficultyLevel.HIGH);
            request.setListeningDetails(List.of("Fast speech", "Accents"));
            request.setSpeakingDetails(List.of("Pronunciation", "Fluency"));
            request.setReadingDetails(List.of("Kanji", "Grammar"));
            request.setWritingDetails(List.of("Kanji writing", "Sentence structure"));
            request.setSelfCefrLevel(CefrLevel.A1);

            // Act
            SkillAssessmentResponseDTO result = studentProfileService.createSkillAssessment(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getListeningDetails().size());
            assertEquals(2, result.getSpeakingDetails().size());
            assertEquals(2, result.getReadingDetails().size());
            assertEquals(2, result.getWritingDetails().size());
        }
    }

    // ========== AC-NF-001: Visualização de configurações de notificação ==========

    @Nested
    @DisplayName("AC-NF-001: Visualização de configurações de notificação")
    class VisualizacaoNotificacoes {

        @Test
        @DisplayName("AC-NF-001: Deve retornar configurações de notificação do estudante")
        void deveRetornarConfiguracoesDeNotificacao() {
            // Arrange
            Map<String, Boolean> channels = new HashMap<>();
            channels.put("PUSH", true);
            channels.put("EMAIL", true);
            channels.put("WHATSAPP", false);

            NotificationSettingsEntity settings = NotificationSettingsEntity.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .activeChannels(channels)
                    .reminderFrequency(ReminderFrequency.DAILY)
                    .preferredTimeStart(LocalTime.of(9, 0))
                    .preferredTimeEnd(LocalTime.of(21, 0))
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(notificationRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

            // Act
            NotificationSettingsDTO result = studentProfileService.getNotificationSettings(userId);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getActiveChannels());
            assertEquals(ReminderFrequency.DAILY, result.getReminderFrequency());
            assertEquals("09:00", result.getPreferredTimeStart());
            assertEquals("21:00", result.getPreferredTimeEnd());
        }

        @Test
        @DisplayName("AC-NF-001: Deve retornar configurações padrão se não existirem")
        void deveRetornarConfiguracoesPadraoSeNaoExistirem() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(notificationRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // Act
            NotificationSettingsDTO result = studentProfileService.getNotificationSettings(userId);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getActiveChannels());
            // Valores padrão: PUSH e EMAIL ativos
            assertEquals(true, result.getActiveChannels().get(NotificationChannel.PUSH));
            assertEquals(true, result.getActiveChannels().get(NotificationChannel.EMAIL));
        }

        @Test
        @DisplayName("AC-NF-001: Deve lançar exceção para usuário inexistente")
        void deveLancarExcecaoParaUsuarioInexistente() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UserNotFoundException.class,
                    () -> studentProfileService.getNotificationSettings(userId));
        }
    }

    // ========== AC-NF-002: Atualização de configurações de notificação ==========

    @Nested
    @DisplayName("AC-NF-002: Atualização de configurações de notificação")
    class AtualizacaoNotificacoes {

        @Test
        @DisplayName("AC-NF-002: Deve atualizar configurações de notificação com sucesso")
        void deveAtualizarConfiguracoesComSucesso() {
            // Arrange
            NotificationSettingsEntity existingSettings = NotificationSettingsEntity.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .activeChannels(new HashMap<>())
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(notificationRepository.findByUserId(userId)).thenReturn(Optional.of(existingSettings));
            when(notificationRepository.save(any(NotificationSettingsEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Map<NotificationChannel, Boolean> newChannels = new HashMap<>();
            newChannels.put(NotificationChannel.PUSH, true);
            newChannels.put(NotificationChannel.EMAIL, false);
            newChannels.put(NotificationChannel.WHATSAPP, true);

            NotificationSettingsDTO request = NotificationSettingsDTO.builder()
                    .activeChannels(newChannels)
                    .reminderFrequency(ReminderFrequency.WEEKLY)
                    .preferredTimeStart("08:00")
                    .preferredTimeEnd("22:00")
                    .build();

            // Act
            NotificationSettingsDTO result = studentProfileService.updateNotificationSettings(userId, request);

            // Assert
            assertNotNull(result);
            verify(notificationRepository).save(any(NotificationSettingsEntity.class));
            verify(auditService).logUpdate(eq("NOTIFICATION_SETTINGS"), eq(userId), any(), any(), eq(userId));
        }

        @Test
        @DisplayName("AC-NF-002: Deve criar configurações se não existirem")
        void deveCriarConfiguracoesSeNaoExistirem() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(notificationRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(notificationRepository.save(any(NotificationSettingsEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Map<NotificationChannel, Boolean> channels = new HashMap<>();
            channels.put(NotificationChannel.PUSH, false);
            channels.put(NotificationChannel.EMAIL, true);

            NotificationSettingsDTO request = NotificationSettingsDTO.builder()
                    .activeChannels(channels)
                    .reminderFrequency(ReminderFrequency.ALTERNATE_DAYS)
                    .build();

            // Act
            NotificationSettingsDTO result = studentProfileService.updateNotificationSettings(userId, request);

            // Assert
            assertNotNull(result);
            verify(notificationRepository).save(any(NotificationSettingsEntity.class));
        }

        @Test
        @DisplayName("AC-NF-002: Deve registrar auditoria ao atualizar configurações")
        void deveRegistrarAuditoriaAoAtualizar() {
            // Arrange
            NotificationSettingsEntity existingSettings = NotificationSettingsEntity.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .activeChannels(new HashMap<>())
                    .reminderFrequency(ReminderFrequency.DAILY)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(notificationRepository.findByUserId(userId)).thenReturn(Optional.of(existingSettings));
            when(notificationRepository.save(any(NotificationSettingsEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            NotificationSettingsDTO request = NotificationSettingsDTO.builder()
                    .reminderFrequency(ReminderFrequency.WEEKLY)
                    .quietModeStart("22:00")
                    .quietModeEnd("08:00")
                    .build();

            // Act
            studentProfileService.updateNotificationSettings(userId, request);

            // Assert
            verify(auditService).logUpdate(
                    eq("NOTIFICATION_SETTINGS"),
                    eq(userId),
                    any(NotificationSettingsDTO.class),
                    any(NotificationSettingsDTO.class),
                    eq(userId)
            );
        }

        @Test
        @DisplayName("AC-NF-002: Deve aceitar preferências por categoria")
        void deveAceitarPreferenciasPorCategoria() {
            // Arrange
            NotificationSettingsEntity existingSettings = NotificationSettingsEntity.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .activeChannels(new HashMap<>())
                    .categoryPreferences(new HashMap<>())
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(notificationRepository.findByUserId(userId)).thenReturn(Optional.of(existingSettings));
            when(notificationRepository.save(any(NotificationSettingsEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Map<NotificationCategory, CategoryPreference> categoryPrefs = new HashMap<>();
            categoryPrefs.put(NotificationCategory.STUDY_REMINDERS,
                    new CategoryPreference(true, List.of(NotificationChannel.PUSH, NotificationChannel.EMAIL)));
            categoryPrefs.put(NotificationCategory.MARKETING,
                    new CategoryPreference(false, List.of()));

            NotificationSettingsDTO request = NotificationSettingsDTO.builder()
                    .categoryPreferences(categoryPrefs)
                    .build();

            // Act
            NotificationSettingsDTO result = studentProfileService.updateNotificationSettings(userId, request);

            // Assert
            assertNotNull(result);
            verify(notificationRepository).save(any(NotificationSettingsEntity.class));
        }
    }
}
