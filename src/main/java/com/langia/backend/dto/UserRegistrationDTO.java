package com.langia.backend.dto;

import com.langia.backend.model.UserProfile;
import com.langia.backend.validation.ValidCpf;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "CPF is required")
    @ValidCpf(message = "CPF is invalid")
    private String cpf;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotNull(message = "Profile is required")
    private UserProfile profile;
}

