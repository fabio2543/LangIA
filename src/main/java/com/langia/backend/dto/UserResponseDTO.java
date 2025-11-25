package com.langia.backend.dto;

import java.util.UUID;

import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de dados do usuário.
 * NÃO inclui dados sensíveis como senha.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private String cpf;
    private String phone;
    private UserProfile profile;

    /**
     * Converte entidade User para DTO de resposta.
     * Remove dados sensíveis (senha).
     *
     * @param user entidade User
     * @return UserResponseDTO sem dados sensíveis
     */
    public static UserResponseDTO fromUser(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .cpf(user.getCpfString())
                .phone(user.getPhone())
                .profile(user.getProfile())
                .build();
    }
}
