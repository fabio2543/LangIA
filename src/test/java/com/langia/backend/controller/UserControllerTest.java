package com.langia.backend.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.backend.dto.UserRegistrationDTO;
import com.langia.backend.exception.CpfAlreadyExistsException;
import com.langia.backend.exception.EmailAlreadyExistsException;
import com.langia.backend.exception.PhoneAlreadyExistsException;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.service.UserService;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {})
@SuppressWarnings("null")
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private UserService userService;

        private User testUser;

        @BeforeEach
        void setUp() {
                testUser = User.builder()
                                .id(UUID.randomUUID())
                                .name("John Doe")
                                .email("john@example.com")
                                .password("encryptedPassword")
                                .cpfString("52998224725")
                                .phone("+5511987654321")
                                .profile(UserProfile.STUDENT)
                                .build();
        }

        @Test
        void registerUser_shouldReturn201WhenRegistrationSucceeds() throws Exception {
                UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
                registrationDTO.setName("John Doe");
                registrationDTO.setEmail("john@example.com");
                registrationDTO.setPassword("password123");
                registrationDTO.setCpf("529.982.247-25");
                registrationDTO.setPhone("(11) 98765-4321");
                registrationDTO.setProfile(UserProfile.STUDENT);

                when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                                .thenReturn(testUser);

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(registrationDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                                .andExpect(jsonPath("$.name").value("John Doe"))
                                .andExpect(jsonPath("$.email").value("john@example.com"))
                                .andExpect(jsonPath("$.profile").value("STUDENT"))
                                .andExpect(jsonPath("$.password").doesNotExist())
                                .andExpect(jsonPath("$.cpf").doesNotExist());
        }

        @Test
        void registerUser_shouldReturn400WhenEmailAlreadyExists() throws Exception {
                UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
                registrationDTO.setName("John Doe");
                registrationDTO.setEmail("existing@example.com");
                registrationDTO.setPassword("password123");
                registrationDTO.setCpf("529.982.247-25");
                registrationDTO.setPhone("(11) 98765-4321");
                registrationDTO.setProfile(UserProfile.STUDENT);

                when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                                .thenThrow(new EmailAlreadyExistsException(
                                                "Email already registered: existing@example.com"));

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(registrationDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Validation error"))
                                .andExpect(jsonPath("$.message").value(containsString("Email already registered")));
        }

        @Test
        void registerUser_shouldReturn400WhenCpfAlreadyExists() throws Exception {
                UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
                registrationDTO.setName("John Doe");
                registrationDTO.setEmail("john@example.com");
                registrationDTO.setPassword("password123");
                registrationDTO.setCpf("529.982.247-25");
                registrationDTO.setPhone("(11) 98765-4321");
                registrationDTO.setProfile(UserProfile.STUDENT);

                when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                                .thenThrow(new CpfAlreadyExistsException("CPF already registered: 52998224725"));

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(registrationDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Validation error"))
                                .andExpect(jsonPath("$.message").value(containsString("CPF already registered")));
        }

        @Test
        void registerUser_shouldReturn400WhenPhoneAlreadyExists() throws Exception {
                UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
                registrationDTO.setName("John Doe");
                registrationDTO.setEmail("john@example.com");
                registrationDTO.setPassword("password123");
                registrationDTO.setCpf("529.982.247-25");
                registrationDTO.setPhone("(11) 98765-4321");
                registrationDTO.setProfile(UserProfile.STUDENT);

                when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                                .thenThrow(new PhoneAlreadyExistsException(
                                                "Phone number already registered: +5511987654321"));

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(registrationDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Validation error"))
                                .andExpect(jsonPath("$.message")
                                                .value(containsString("Phone number already registered")));
        }

        @Test
        void registerUser_shouldReturn400WhenValidationFails() throws Exception {
                UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
                registrationDTO.setName("");
                registrationDTO.setEmail("invalid-email");
                registrationDTO.setPassword("");
                registrationDTO.setCpf("");
                registrationDTO.setPhone("");
                registrationDTO.setProfile(null);

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(registrationDTO)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void registerUser_shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
                UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
                registrationDTO.setName("John Doe");
                registrationDTO.setEmail("john@example.com");
                registrationDTO.setPassword("password123");
                registrationDTO.setCpf("529.982.247-25");
                registrationDTO.setPhone("(11) 98765-4321");
                registrationDTO.setProfile(UserProfile.STUDENT);

                when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                                .thenThrow(new RuntimeException("Database connection failed"));

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(registrationDTO)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.error").value("Registration failed"))
                                .andExpect(jsonPath("$.message")
                                                .value("An unexpected error occurred. Please try again later."));
        }

        @Test
        void registerUser_shouldAcceptDifferentUserProfiles() throws Exception {
                UserProfile[] profiles = { UserProfile.STUDENT, UserProfile.TEACHER, UserProfile.ADMIN };

                for (UserProfile profile : profiles) {
                        User user = User.builder()
                                        .id(UUID.randomUUID())
                                        .name("Test User")
                                        .email("test" + profile.name() + "@example.com")
                                        .password("encryptedPassword")
                                        .cpfString("52998224725")
                                        .phone("+5511987654321")
                                        .profile(profile)
                                        .build();

                        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
                        registrationDTO.setName("Test User");
                        registrationDTO.setEmail("test" + profile.name() + "@example.com");
                        registrationDTO.setPassword("password123");
                        registrationDTO.setCpf("529.982.247-25");
                        registrationDTO.setPhone("(11) 98765-4321");
                        registrationDTO.setProfile(profile);

                        when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString(),
                                        any()))
                                        .thenReturn(user);

                        mockMvc.perform(post("/api/users/register")
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content(objectMapper.writeValueAsString(registrationDTO)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.profile").value(profile.name()));
                }
        }
}
