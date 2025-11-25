package com.langia.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.exception.EmailAlreadyExistsException;
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
     * Validates if email already exists, encrypts password, and saves to database.
     *
     * @param name      user's full name
     * @param email     user's email
     * @param password  user's password (will be encrypted)
     * @param cpfString user's CPF
     * @param phone     user's phone number
     * @param profile   user's profile type
     * @return the saved User entity
     * @throws EmailAlreadyExistsException if email already exists
     */
    @Transactional
    public User registerUser(String name, String email, String password, String cpfString, String phone, UserProfile profile) {
        // Validate if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered: " + email);
        }

        // Encrypt password using BCrypt
        // Never save password in plain text
        String encryptedPassword = passwordEncoder.encode(password);

        // Create user entity
        User user = User.builder()
                .name(name)
                .email(email)
                .password(encryptedPassword)
                .cpfString(cpfString)
                .phone(phone)
                .profile(profile)
                .build();

        // Save to database
        User savedUser = userRepository.save(user);
        return savedUser;
    }
}
