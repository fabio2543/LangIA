package com.langia.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.langia.backend.exception.CpfAlreadyExistsException;
import com.langia.backend.exception.EmailAlreadyExistsException;
import com.langia.backend.exception.PhoneAlreadyExistsException;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerUser_shouldNormalizeAndEncryptPassword() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByCpf(any())).thenReturn(false);
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        User savedUser = userService.registerUser(
                " John Doe ",
                "USER@EMAIL.COM ",
                "plainPassword",
                " 12345678901 ",
                " 99999999999 ",
                UserProfile.STUDENT);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getEmail()).isEqualTo("user@email.com");
        assertThat(capturedUser.getCpfString()).isEqualTo("12345678901");
        assertThat(capturedUser.getPhone()).isEqualTo("99999999999");
        assertThat(capturedUser.getName()).isEqualTo("John Doe");
        assertThat(capturedUser.getPassword()).isNotEqualTo("plainPassword");
        assertThat(passwordEncoder.matches("plainPassword", capturedUser.getPassword())).isTrue();
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void registerUser_shouldFailWhenEmailExists() {
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(
                "Name",
                " existing@email.com ",
                "password",
                "123",
                "999",
                UserProfile.STUDENT));
    }

    @Test
    void registerUser_shouldFailWhenCpfExists() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByCpf("123")).thenReturn(true);

        assertThrows(CpfAlreadyExistsException.class, () -> userService.registerUser(
                "Name",
                "email@test.com",
                "password",
                "123",
                "999",
                UserProfile.STUDENT));
    }

    @Test
    void registerUser_shouldFailWhenPhoneExists() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByCpf(any())).thenReturn(false);
        when(userRepository.existsByPhone("999")).thenReturn(true);

        assertThrows(PhoneAlreadyExistsException.class, () -> userService.registerUser(
                "Name",
                "email@test.com",
                "password",
                "123",
                "999",
                UserProfile.STUDENT));
    }

    @Test
    void registerUser_shouldFailWhenRequiredFieldsMissing() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(
                " ",
                " ",
                " ",
                " ",
                " ",
                null));
    }
}
