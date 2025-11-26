package com.langia.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.UserRegistrationDTO;
import com.langia.backend.dto.UserResponseDTO;
import com.langia.backend.model.User;
import com.langia.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsável pelo gerenciamento de usuários.
 * Exceções são tratadas pelo GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Registra um novo usuário no sistema.
     *
     * @param registrationDTO dados de registro do usuário
     * @return UserResponseDTO com status 201 Created
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        log.info("Recebida requisição de registro para email: {}", registrationDTO.getEmail());

        User registeredUser = userService.registerUser(
                registrationDTO.getName(),
                registrationDTO.getEmail(),
                registrationDTO.getPassword(),
                registrationDTO.getCpf(),
                registrationDTO.getPhone(),
                registrationDTO.getProfile());

        log.info("Usuário registrado com sucesso: {} (ID: {})",
                registeredUser.getEmail(), registeredUser.getId());

        UserResponseDTO responseDTO = UserResponseDTO.fromUser(registeredUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
