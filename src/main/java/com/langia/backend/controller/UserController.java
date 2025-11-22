package com.langia.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.UserRegistrationDTO;
import com.langia.backend.dto.UserResponseDTO;
import com.langia.backend.exception.CpfAlreadyExistsException;
import com.langia.backend.exception.EmailAlreadyExistsException;
import com.langia.backend.exception.PhoneAlreadyExistsException;
import com.langia.backend.model.User;
import com.langia.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Endpoint to register a new user.
     * Validates the DTO, checks for duplicate email, CPF, and phone, encrypts
     * password, and saves to database.
     *
     * @param registrationDTO user registration data
     * @return ResponseEntity with created user and status 201, or error message
     *         with status 400
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        try {
            // Call service to register user
            User registeredUser = userService.registerUser(
                    registrationDTO.getName(),
                    registrationDTO.getEmail(),
                    registrationDTO.getPassword(),
                    registrationDTO.getCpf(),
                    registrationDTO.getPhone(),
                    registrationDTO.getProfile());

            // Convert User entity to DTO (excluding sensitive data like password and CPF)
            UserResponseDTO responseDTO = UserResponseDTO.fromUser(registeredUser);

            // Return 201 Created with the user response DTO (without sensitive data)
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (EmailAlreadyExistsException e) {
            // Handle duplicate email error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Email already registered");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (CpfAlreadyExistsException e) {
            // Handle duplicate CPF error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "CPF already registered");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (PhoneAlreadyExistsException e) {
            // Handle duplicate phone error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Phone number already registered");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // Handle other unexpected errors
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
