package com.langia.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.exception.CpfAlreadyExistsException;
import com.langia.backend.exception.EmailAlreadyExistsException;
import com.langia.backend.exception.PhoneAlreadyExistsException;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Registra um novo usuário no sistema.
     * Valida se email, CPF e telefone já existem, criptografa a senha e salva no banco.
     *
     * @param name      nome completo do usuário
     * @param email     email do usuário
     * @param password  senha do usuário (será criptografada)
     * @param cpfString CPF do usuário
     * @param phone     telefone do usuário
     * @param profile   perfil do usuário
     * @return a entidade User salva
     * @throws EmailAlreadyExistsException se o email já existe
     * @throws CpfAlreadyExistsException se o CPF já existe
     * @throws PhoneAlreadyExistsException se o telefone já existe
     */
    @Transactional
    public User registerUser(String name, String email, String password, String cpfString, String phone, UserProfile profile) {
        log.info("Iniciando registro de usuário: {}", email);

        // Valida se email já existe
        if (userRepository.existsByEmail(email)) {
            log.warn("Tentativa de registro com email já existente: {}", email);
            throw new EmailAlreadyExistsException();
        }

        // Valida se CPF já existe
        if (userRepository.existsByCpf(cpfString)) {
            log.warn("Tentativa de registro com CPF já existente");
            throw new CpfAlreadyExistsException();
        }

        // Valida se telefone já existe
        if (userRepository.existsByPhone(phone)) {
            log.warn("Tentativa de registro com telefone já existente: {}", phone);
            throw new PhoneAlreadyExistsException();
        }

        // Criptografa a senha usando BCrypt
        String encryptedPassword = passwordEncoder.encode(password);

        // Cria a entidade do usuário
        User user = User.builder()
                .name(name)
                .email(email)
                .password(encryptedPassword)
                .cpfString(cpfString)
                .phone(phone)
                .profile(profile)
                .build();

        // Salva no banco de dados
        User savedUser = userRepository.save(user);
        log.info("Usuário registrado com sucesso: {} (ID: {})", email, savedUser.getId());

        return savedUser;
    }
}
