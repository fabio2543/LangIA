package com.langia.backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.backend.model.AuditLog;
import com.langia.backend.model.AuditLog.AuditAction;
import com.langia.backend.repository.AuditLogRepository;
import com.langia.backend.util.IpAddressUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável por registrar operações de auditoria.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    /**
     * Registra uma operação de auditoria.
     * Usa propagação REQUIRES_NEW para garantir que o log seja salvo
     * mesmo se a transação principal falhar.
     *
     * @param entityType Tipo da entidade
     * @param entityId   ID da entidade
     * @param action     Tipo de ação
     * @param oldValue   Valor anterior (pode ser null)
     * @param newValue   Novo valor (pode ser null)
     * @param userId     ID do usuário que realizou a operação
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, UUID entityId, AuditAction action,
                    Object oldValue, Object newValue, UUID userId) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .oldValue(toJson(oldValue))
                    .newValue(toJson(newValue))
                    .userId(userId)
                    .ipAddress(getClientIp())
                    .userAgent(getUserAgent())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} on {} ({})",
                    action, entityType, entityId, userId);
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {} on {}: {}",
                    action, entityType, entityId, e.getMessage());
        }
    }

    /**
     * Registra uma operação CREATE.
     */
    public void logCreate(String entityType, UUID entityId, Object newValue, UUID userId) {
        log(entityType, entityId, AuditAction.CREATE, null, newValue, userId);
    }

    /**
     * Registra uma operação UPDATE.
     */
    public void logUpdate(String entityType, UUID entityId, Object oldValue, Object newValue, UUID userId) {
        log(entityType, entityId, AuditAction.UPDATE, oldValue, newValue, userId);
    }

    /**
     * Registra uma operação DELETE.
     */
    public void logDelete(String entityType, UUID entityId, Object oldValue, UUID userId) {
        log(entityType, entityId, AuditAction.DELETE, oldValue, null, userId);
    }

    /**
     * Converte objeto para JSON string.
     */
    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return "{\"error\": \"serialization_failed\"}";
        }
    }

    /**
     * Obtém o IP do cliente da requisição atual.
     */
    private String getClientIp() {
        try {
            return IpAddressUtil.getClientIp(request);
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Obtém o User-Agent da requisição atual.
     */
    private String getUserAgent() {
        try {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.length() > 500) {
                return userAgent.substring(0, 500);
            }
            return userAgent;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
