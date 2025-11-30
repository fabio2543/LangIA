package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.langia.backend.exception.EmailAlreadyExistsException;
import com.langia.backend.model.Profile;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.ProfileRepository;
import com.langia.backend.repository.UserRepository;

/**
 * Testes para o serviço de registro de usuários.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private UserService userService;

    private String testName;
    private String testEmail;
    private String testPassword;
    private String testCpf;
    private String testPhone;
    private UserProfile testProfile;
    private User savedUser;
    private Profile studentProfile;
    private Profile teacherProfile;
    private Profile adminProfile;

    @BeforeEach
    void setUp() {
        testName = "João Silva";
        testEmail = "joao.silva@example.com";
        testPassword = "senha123";
        testCpf = "11144477735";
        testPhone = "11987654321";
        testProfile = UserProfile.STUDENT;

        // Cria perfis de teste
        studentProfile = Profile.builder()
                .id(UUID.randomUUID())
                .code(UserProfile.STUDENT)
                .name("Student")
                .hierarchyLevel(1)
                .active(true)
                .build();

        teacherProfile = Profile.builder()
                .id(UUID.randomUUID())
                .code(UserProfile.TEACHER)
                .name("Teacher")
                .hierarchyLevel(2)
                .active(true)
                .build();

        adminProfile = Profile.builder()
                .id(UUID.randomUUID())
                .code(UserProfile.ADMIN)
                .name("Admin")
                .hierarchyLevel(3)
                .active(true)
                .build();

        savedUser = User.builder()
                .id(UUID.randomUUID())
                .name(testName)
                .email(testEmail)
                .password("$2a$12$hashedPassword")
                .cpfString(testCpf)
                .phone(testPhone)
                .profile(studentProfile)
                .build();
    }

    // ========== Testes de Registro com Sucesso ==========

    @Test
    void deveRegistrarUsuarioComSucesso() {
        // Arrange
        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(profileRepository.findByCode(testProfile)).thenReturn(Optional.of(studentProfile));
        when(passwordEncoder.encode(testPassword)).thenReturn("$2a$12$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(
                testName, testEmail, testPassword, testCpf, testPhone, testProfile);

        // Assert
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(testName, result.getName());
        assertEquals(testEmail, result.getEmail());
        assertEquals("$2a$12$hashedPassword", result.getPassword());
        assertEquals(testCpf, result.getCpfString());
        assertEquals(testPhone, result.getPhone());
        assertEquals(testProfile, result.getProfileCode());

        // Verifica que o email foi verificado
        verify(userRepository).existsByEmail(testEmail);
        // Verifica que a senha foi criptografada
        verify(passwordEncoder).encode(testPassword);
        // Verifica que o usuário foi salvo
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveIncluirNameNoUsuarioRegistrado() {
        // Arrange
        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(profileRepository.findByCode(testProfile)).thenReturn(Optional.of(studentProfile));
        when(passwordEncoder.encode(testPassword)).thenReturn("$2a$12$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // CRÍTICO: verifica que o name foi passado para o User.builder
            assertNotNull(user.getName());
            assertEquals(testName, user.getName());
            return savedUser;
        });

        // Act
        User result = userService.registerUser(
                testName, testEmail, testPassword, testCpf, testPhone, testProfile);

        // Assert
        assertNotNull(result.getName());
        assertEquals(testName, result.getName());
    }

    @Test
    void deveCriptografarSenhaComBCrypt() {
        // Arrange
        String plainPassword = "minhasenha123";
        String hashedPassword = "$2a$12$hash.muito.longo.e.seguro";

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(profileRepository.findByCode(testProfile)).thenReturn(Optional.of(studentProfile));
        when(passwordEncoder.encode(plainPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        userService.registerUser(
                testName, testEmail, plainPassword, testCpf, testPhone, testProfile);

        // Assert
        // Verifica que a senha foi criptografada
        verify(passwordEncoder).encode(plainPassword);
        // Verifica que o save foi chamado (a senha criptografada foi salva)
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveRegistrarUsuarioComPerfilTeacher() {
        // Arrange
        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(profileRepository.findByCode(UserProfile.TEACHER)).thenReturn(Optional.of(teacherProfile));
        when(passwordEncoder.encode(testPassword)).thenReturn("$2a$12$hashedPassword");

        User teacherUser = User.builder()
                .id(UUID.randomUUID())
                .name(testName)
                .email(testEmail)
                .password("$2a$12$hashedPassword")
                .cpfString(testCpf)
                .phone(testPhone)
                .profile(teacherProfile)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(teacherUser);

        // Act
        User result = userService.registerUser(
                testName, testEmail, testPassword, testCpf, testPhone, UserProfile.TEACHER);

        // Assert
        assertEquals(UserProfile.TEACHER, result.getProfileCode());
    }

    @Test
    void deveRegistrarUsuarioComPerfilAdmin() {
        // Arrange
        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(profileRepository.findByCode(UserProfile.ADMIN)).thenReturn(Optional.of(adminProfile));
        when(passwordEncoder.encode(testPassword)).thenReturn("$2a$12$hashedPassword");

        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .name(testName)
                .email(testEmail)
                .password("$2a$12$hashedPassword")
                .cpfString(testCpf)
                .phone(testPhone)
                .profile(adminProfile)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        // Act
        User result = userService.registerUser(
                testName, testEmail, testPassword, testCpf, testPhone, UserProfile.ADMIN);

        // Assert
        assertEquals(UserProfile.ADMIN, result.getProfileCode());
    }

    // ========== Testes de Validação de Email Duplicado ==========

    @Test
    void deveRejeitarEmailJaExistente() {
        // Arrange
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(
                        testName, testEmail, testPassword, testCpf, testPhone, testProfile));

        assertEquals("Email already registered: " + testEmail, exception.getMessage());

        // Verifica que a senha NÃO foi criptografada
        verify(passwordEncoder, never()).encode(anyString());
        // Verifica que o usuário NÃO foi salvo
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveVerificarEmailAntesDeProcessar() {
        // Arrange
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.registerUser(
                        testName, testEmail, testPassword, testCpf, testPhone, testProfile));

        // Verifica que foi a PRIMEIRA coisa verificada
        verify(userRepository).existsByEmail(testEmail);
        // E que NADA MAIS foi executado
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== Testes de Dados Completos ==========

    @Test
    void devePreservarTodosDadosDoUsuario() {
        // Arrange
        String specificName = "Maria Santos";
        String specificEmail = "maria.santos@example.com";
        String specificPassword = "senha456";
        String specificCpf = "11144477735";
        String specificPhone = "11987654321";
        UserProfile specificProfile = UserProfile.TEACHER;

        when(userRepository.existsByEmail(specificEmail)).thenReturn(false);
        when(profileRepository.findByCode(specificProfile)).thenReturn(Optional.of(teacherProfile));
        when(passwordEncoder.encode(specificPassword)).thenReturn("$2a$12$hashedPassword");

        User specificUser = User.builder()
                .id(UUID.randomUUID())
                .name(specificName)
                .email(specificEmail)
                .password("$2a$12$hashedPassword")
                .cpfString(specificCpf)
                .phone(specificPhone)
                .profile(teacherProfile)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(specificUser);

        // Act
        User result = userService.registerUser(
                specificName, specificEmail, specificPassword, specificCpf, specificPhone, specificProfile);

        // Assert - verifica que TODOS os dados foram preservados
        assertEquals(specificName, result.getName());
        assertEquals(specificEmail, result.getEmail());
        assertEquals(specificCpf, result.getCpfString());
        assertEquals(specificPhone, result.getPhone());
        assertEquals(specificProfile, result.getProfileCode());
        assertNotNull(result.getPassword());
    }
}
