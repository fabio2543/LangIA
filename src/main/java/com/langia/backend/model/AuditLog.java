package com.langia.backend.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade para registro de auditoria de alterações no sistema.
 * Armazena todas as operações de criação, atualização e exclusão.
 */
@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Tipo da entidade alterada (ex: USER, PREFERENCES, NOTIFICATION_SETTINGS)
     */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    /**
     * ID da entidade alterada
     */
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    /**
     * Tipo de operação realizada
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditAction action;

    /**
     * Estado anterior da entidade (JSON) - null para CREATE
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;

    /**
     * Novo estado da entidade (JSON) - null para DELETE
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    /**
     * ID do usuário que realizou a operação
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Endereço IP de origem da requisição
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User-Agent do cliente
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Timestamp da operação
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Enum para tipos de ação de auditoria
     */
    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE
    }
}
