package com.langia.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.langia.backend.model.AuditLog.AuditAction;

/**
 * Anotação para marcar métodos que devem ser auditados.
 * Quando aplicada a um método de serviço, registra automaticamente
 * as alterações na tabela de auditoria.
 *
 * <p>Exemplo de uso:</p>
 * <pre>
 * {@code
 * @Auditable(entityType = "USER", action = AuditAction.UPDATE)
 * public User updateUser(UUID userId, UpdateUserRequest request) {
 *     // implementação
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Tipo da entidade sendo auditada (ex: "USER", "PREFERENCES").
     */
    String entityType();

    /**
     * Tipo de ação sendo realizada.
     */
    AuditAction action();

    /**
     * Nome do parâmetro que contém o ID da entidade.
     * Se não especificado, tenta encontrar automaticamente.
     */
    String entityIdParam() default "";

    /**
     * Se true, captura o estado anterior da entidade antes da operação.
     * Requer que exista um método findById no repositório correspondente.
     */
    boolean captureOldValue() default true;
}
