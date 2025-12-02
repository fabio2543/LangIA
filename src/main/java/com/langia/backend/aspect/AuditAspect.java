package com.langia.backend.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.langia.backend.annotation.Auditable;
import com.langia.backend.model.AuditLog.AuditAction;
import com.langia.backend.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspecto que intercepta métodos anotados com @Auditable
 * e registra automaticamente as operações de auditoria.
 */
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    /**
     * Intercepta métodos anotados com @Auditable e registra a operação.
     */
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String entityType = auditable.entityType();
        AuditAction action = auditable.action();
        UUID entityId = extractEntityId(joinPoint, auditable);
        UUID userId = getCurrentUserId();

        Object oldValue = null;

        // Captura valor anterior para UPDATE e DELETE
        if (auditable.captureOldValue() && action != AuditAction.CREATE && entityId != null) {
            oldValue = captureOldValue(joinPoint, entityId);
        }

        // Executa o método original
        Object result = joinPoint.proceed();

        // Para CREATE, o entityId pode vir do resultado
        if (action == AuditAction.CREATE && entityId == null && result != null) {
            entityId = extractIdFromResult(result);
        }

        // Registra a auditoria
        if (entityId != null) {
            Object newValue = (action == AuditAction.DELETE) ? null : result;
            auditService.log(entityType, entityId, action, oldValue, newValue, userId);
        } else {
            log.warn("Could not extract entity ID for audit log: {} {}", action, entityType);
        }

        return result;
    }

    /**
     * Extrai o ID da entidade dos parâmetros do método.
     */
    private UUID extractEntityId(ProceedingJoinPoint joinPoint, Auditable auditable) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        String entityIdParam = auditable.entityIdParam();

        // Se um parâmetro específico foi definido
        if (!entityIdParam.isEmpty()) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(entityIdParam)) {
                    return toUUID(args[i]);
                }
            }
        }

        // Tenta encontrar automaticamente por nome comum
        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName().toLowerCase();
            if (paramName.equals("id") || paramName.equals("entityid") ||
                paramName.equals("userid") || paramName.endsWith("id")) {
                UUID uuid = toUUID(args[i]);
                if (uuid != null) {
                    return uuid;
                }
            }
        }

        // Tenta encontrar por tipo UUID
        for (Object arg : args) {
            if (arg instanceof UUID) {
                return (UUID) arg;
            }
        }

        return null;
    }

    /**
     * Converte objeto para UUID.
     */
    private UUID toUUID(Object value) {
        if (value instanceof UUID) {
            return (UUID) value;
        }
        if (value instanceof String) {
            try {
                return UUID.fromString((String) value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Extrai ID do resultado do método (para operações CREATE).
     */
    private UUID extractIdFromResult(Object result) {
        try {
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            return toUUID(id);
        } catch (Exception e) {
            log.debug("Could not extract ID from result: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Captura o valor anterior da entidade.
     * Esta implementação básica retorna null - deve ser sobrescrita
     * ou configurada para cada tipo de entidade.
     */
    private Object captureOldValue(ProceedingJoinPoint joinPoint, UUID entityId) {
        // Implementação básica - em casos reais, você pode injetar
        // repositórios específicos ou usar um EntityManager
        return null;
    }

    /**
     * Obtém o ID do usuário atual do contexto de segurança.
     */
    private UUID getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                Object principal = authentication.getPrincipal();

                // Se for uma string (geralmente o userId no JWT)
                if (principal instanceof String) {
                    return UUID.fromString((String) principal);
                }

                // Se for um objeto com método getId
                try {
                    Method getIdMethod = principal.getClass().getMethod("getId");
                    Object id = getIdMethod.invoke(principal);
                    return toUUID(id);
                } catch (Exception e) {
                    log.debug("Could not get user ID from principal: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("Could not get current user ID: {}", e.getMessage());
        }
        return null;
    }
}
