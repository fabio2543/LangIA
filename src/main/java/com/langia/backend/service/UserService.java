package com.langia.backend.service;

import java.util.Objects;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.langia.backend.exception.CpfAlreadyExistsException;
import com.langia.backend.exception.EmailAlreadyExistsException;
import com.langia.backend.exception.PasswordTooShortException;
import com.langia.backend.exception.PhoneAlreadyExistsException;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.UserRepository;
import com.langia.backend.service.validator.UserDataValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserDataValidator userDataValidator;

    /**
     * Registers a new user in the system.
     * Validates if email, CPF, and phone already exist, encrypts password, and
     * saves to database.
     *
     * @param name      user's name
     * @param email     user's email
     * @param password  user's password (will be encrypted)
     * @param cpfString user's CPF
     * @param phone     user's phone number
     * @param profile   user's profile type
     * @return the saved User entity
     * @throws EmailAlreadyExistsException if email already exists
     * @throws CpfAlreadyExistsException   if CPF already exists
     * @throws PhoneAlreadyExistsException if phone already exists
     * @throws PasswordTooShortException    if password is shorter than 6 characters
     */
    @Transactional
    @SuppressWarnings("null")
    public User registerUser(String name, String email, String password, String cpfString, String phone,
            UserProfile profile) {
        String normalizedName = name != null ? name.trim() : null;
        String normalizedEmail = email != null ? email.trim().toLowerCase() : null;
        String normalizedCpf = userDataValidator.normalizeAndValidateCpf(cpfString);
        String normalizedPhone = userDataValidator.normalizeAndValidatePhone(phone);

        if (!StringUtils.hasText(normalizedName) || !StringUtils.hasText(normalizedEmail) || profile == null
                || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Name, email, password and profile are required.");
        }

        // Validate password minimum length
        if (password.length() < 6) {
            throw new PasswordTooShortException("Password must be at least 6 characters long.");
        }

        // Validate if email already exists
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email already registered: " + normalizedEmail);
        }

        // Validate if CPF already exists
        if (userRepository.existsByCpf(normalizedCpf)) {
            throw new CpfAlreadyExistsException("CPF already registered: " + normalizedCpf);
        }

        // Validate if phone already exists
        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new PhoneAlreadyExistsException("Phone number already registered: " + normalizedPhone);
        }

        // Encrypt password using BCrypt
        // Never save password in plain text
        String encryptedPassword = passwordEncoder.encode(password);

        // Create user entity
        User user = User.builder()
                .name(normalizedName)
                .email(normalizedEmail)
                .password(encryptedPassword)
                .cpfString(normalizedCpf)
                .phone(normalizedPhone)
                .profile(profile)
                .build();

        // Save to database
        return Objects.requireNonNull(userRepository.save(user), "User repository returned null on save.");
    }
}
