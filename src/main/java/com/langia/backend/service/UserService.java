package com.langia.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.langia.backend.exception.CpfAlreadyExistsException;
import com.langia.backend.exception.EmailAlreadyExistsException;
import com.langia.backend.exception.PhoneAlreadyExistsException;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
     */
    @Transactional
    public User registerUser(String name, String email, String password, String cpfString, String phone,
            UserProfile profile) {

        String normalizedEmail = email != null ? email.trim().toLowerCase() : null;
        String normalizedCpf = cpfString != null ? cpfString.trim() : null;
        String normalizedPhone = phone != null ? phone.trim() : null;

        if (!StringUtils.hasText(normalizedEmail) || !StringUtils.hasText(normalizedCpf)
                || !StringUtils.hasText(normalizedPhone) || profile == null
                || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Name, email, password, CPF, phone and profile are required.");
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
                .name(name)
                .email(normalizedEmail)
                .password(encryptedPassword)
                .cpfString(normalizedCpf)
                .phone(normalizedPhone)
                .profile(profile)
                .build();

        // Save to database
        User savedUser = userRepository.save(user);
        return savedUser;
    }
}
