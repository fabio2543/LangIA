package com.langia.backend.dto;

import java.util.Set;
import java.util.UUID;

import com.langia.backend.model.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de login bem-sucedido.
 * Contém todas as informações necessárias para o cliente gerenciar a sessão do usuário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    /**
     * Token JWT gerado para autenticação.
     */
    private String token;

    /**
     * Identificador único do usuário.
     */
    private UUID userId;

    /**
     * Nome completo do usuário.
     */
    private String name;

    /**
     * Email do usuário.
     */
    private String email;

    /**
     * Perfil do usuário no sistema.
     */
    private UserProfile profile;

    /**
     * Conjunto de permissões específicas do perfil.
     */
    private Set<String> permissions;

    /**
     * Tempo em milissegundos até a expiração do token.
     */
    private Long expiresIn;
}
