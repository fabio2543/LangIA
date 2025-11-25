package com.langia.backend.dto;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.langia.backend.model.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para armazenar dados da sessão no Redis.
 * Contém informações necessárias para acesso rápido durante validação de requisições.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionData implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * Conjunto de permissões do perfil.
     */
    private Set<String> permissions;

    /**
     * Timestamp de quando a sessão foi criada.
     */
    private Long createdAt;
}
