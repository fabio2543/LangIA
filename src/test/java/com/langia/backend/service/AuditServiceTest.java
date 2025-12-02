package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.backend.model.AuditLog;
import com.langia.backend.model.AuditLog.AuditAction;
import com.langia.backend.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Testes para o serviço de auditoria.
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuditService auditService;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    private UUID testUserId;
    private UUID testEntityId;

    @BeforeEach
    void setUp() {
        // Configurar ObjectMapper real para serialização JSON
        ObjectMapper objectMapper = new ObjectMapper();
        auditService = new AuditService(auditLogRepository, objectMapper, request);

        testUserId = UUID.randomUUID();
        testEntityId = UUID.randomUUID();
    }

    // ========== Testes de log() ==========

    @Test
    void deveRegistrarLogDeAuditoria() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        auditService.log("USER", testEntityId, AuditAction.UPDATE,
                new TestEntity("old"), new TestEntity("new"), testUserId);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertEquals("USER", savedLog.getEntityType());
        assertEquals(testEntityId, savedLog.getEntityId());
        assertEquals(AuditAction.UPDATE, savedLog.getAction());
        assertEquals(testUserId, savedLog.getUserId());
        assertEquals("192.168.1.100", savedLog.getIpAddress());
        assertEquals("Mozilla/5.0", savedLog.getUserAgent());
        assertNotNull(savedLog.getOldValue());
        assertNotNull(savedLog.getNewValue());
        assertNotNull(savedLog.getCreatedAt());
    }

    @Test
    void deveRegistrarLogComValoresNulos() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        auditService.log("USER", testEntityId, AuditAction.CREATE, null, new TestEntity("new"), testUserId);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertNull(savedLog.getOldValue());
        assertNotNull(savedLog.getNewValue());
    }

    // ========== Testes de logCreate() ==========

    @Test
    void deveRegistrarLogDeCreate() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        auditService.logCreate("PREFERENCES", testEntityId, new TestEntity("new"), testUserId);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertEquals(AuditAction.CREATE, savedLog.getAction());
        assertNull(savedLog.getOldValue());
        assertNotNull(savedLog.getNewValue());
    }

    // ========== Testes de logUpdate() ==========

    @Test
    void deveRegistrarLogDeUpdate() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        auditService.logUpdate("USER", testEntityId, new TestEntity("old"), new TestEntity("new"), testUserId);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertEquals(AuditAction.UPDATE, savedLog.getAction());
        assertNotNull(savedLog.getOldValue());
        assertNotNull(savedLog.getNewValue());
    }

    // ========== Testes de logDelete() ==========

    @Test
    void deveRegistrarLogDeDelete() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        auditService.logDelete("USER", testEntityId, new TestEntity("old"), testUserId);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertEquals(AuditAction.DELETE, savedLog.getAction());
        assertNotNull(savedLog.getOldValue());
        assertNull(savedLog.getNewValue());
    }

    // ========== Testes de captura de IP ==========

    @Test
    void deveCapturarIpDoHeaderXForwardedFor() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        auditService.log("USER", testEntityId, AuditAction.UPDATE, null, null, testUserId);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("10.0.0.1", auditLogCaptor.getValue().getIpAddress());
    }

    @Test
    void deveTruncarUserAgentLongo() {
        // Arrange
        String longUserAgent = "A".repeat(600);
        when(request.getHeader("User-Agent")).thenReturn(longUserAgent);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        auditService.log("USER", testEntityId, AuditAction.UPDATE, null, null, testUserId);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals(500, auditLogCaptor.getValue().getUserAgent().length());
    }

    // ========== Testes de tratamento de erro ==========

    @Test
    void deveContinuarMesmoComErroNoRepositorio() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("DB error"));

        // Act - não deve lançar exceção
        auditService.log("USER", testEntityId, AuditAction.UPDATE, null, null, testUserId);

        // Assert - método foi chamado mesmo com erro
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void deveSerializarObjetoParaJson() {
        // Arrange
        TestEntity entity = new TestEntity("test value");
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        auditService.logCreate("TEST", testEntityId, entity, testUserId);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        String json = auditLogCaptor.getValue().getNewValue();
        assertNotNull(json);
        assertEquals("{\"name\":\"test value\"}", json);
    }

    // ========== Classe auxiliar para testes ==========

    static class TestEntity {
        private String name;

        TestEntity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
