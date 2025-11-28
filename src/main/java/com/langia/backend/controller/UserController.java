package com.langia.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.RegisterResponseDTO;
import com.langia.backend.dto.UserRegistrationDTO;
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
     * Registra um novo usuario no sistema.
     * Apos o registro, um e-mail de verificacao e enviado.
     *
     * @param registrationDTO dados de registro do usuario
     * @return RegisterResponseDTO indicando verificacao pendente
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        log.info("Recebida requisicao de registro para email: {}", registrationDTO.getEmail());

        User registeredUser = userService.registerUser(
                registrationDTO.getName(),
                registrationDTO.getEmail(),
                registrationDTO.getPassword(),
                registrationDTO.getCpf(),
                registrationDTO.getPhone(),
                registrationDTO.getProfile());

        log.info("Usuario registrado com verificacao pendente: {} (ID: {})",
                registeredUser.getEmail(), registeredUser.getId());

        RegisterResponseDTO responseDTO = RegisterResponseDTO.pendingVerification(registeredUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
