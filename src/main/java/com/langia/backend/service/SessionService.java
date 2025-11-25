package com.langia.backend.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.langia.backend.dto.SessionData;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para gerenciamento de sessões de usuário no Redis.
 * Responsável por criar, recuperar e remover sessões com TTL de 1 hora.
 */
@Service
@Slf4j
public class SessionService {

    private static final String SESSION_PREFIX = "session:";
    private static final long SESSION_EXPIRATION_HOURS = 1;

    @Autowired
    private RedisTemplate<String, SessionData> sessionRedisTemplate;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Gera a chave Redis para uma sessão baseada no token.
     *
     * @param token token JWT
     * @return chave formatada para Redis
     */
    private String getSessionKey(String token) {
        return SESSION_PREFIX + token;
    }

    /**
     * Salva uma nova sessão no Redis com TTL de 1 hora.
     *
     * @param token token JWT usado como chave
     * @param sessionData dados da sessão a serem salvos
     */
    public void saveSession(String token, SessionData sessionData) {
        try {
            String key = getSessionKey(token);
            sessionData.setCreatedAt(System.currentTimeMillis());

            sessionRedisTemplate.opsForValue().set(
                    key,
                    sessionData,
                    SESSION_EXPIRATION_HOURS,
                    TimeUnit.HOURS
            );

            log.info("Sessão criada no Redis para usuário: {} (ID: {})",
                    sessionData.getEmail(), sessionData.getUserId());
        } catch (Exception e) {
            log.error("Erro ao salvar sessão no Redis para token: {}", token, e);
            throw new RuntimeException("Falha ao criar sessão no Redis", e);
        }
    }

    /**
     * Recupera os dados de uma sessão pelo token.
     *
     * @param token token JWT
     * @return dados da sessão se existir e for válida, null caso contrário
     */
    public SessionData getSession(String token) {
        try {
            String key = getSessionKey(token);
            SessionData sessionData = sessionRedisTemplate.opsForValue().get(key);

            if (sessionData != null) {
                log.debug("Sessão recuperada do Redis para usuário: {} (ID: {})",
                        sessionData.getEmail(), sessionData.getUserId());
            } else {
                log.debug("Sessão não encontrada ou expirada para token");
            }

            return sessionData;
        } catch (Exception e) {
            log.error("Erro ao recuperar sessão do Redis", e);
            return null;
        }
    }

    /**
     * Remove uma sessão do Redis.
     * Usado principalmente no processo de logout.
     *
     * @param token token JWT da sessão a ser removida
     * @return true se a sessão foi removida, false se não existia
     */
    public boolean removeSession(String token) {
        try {
            String key = getSessionKey(token);
            Boolean deleted = sessionRedisTemplate.delete(key);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("Sessão removida do Redis com sucesso");
                return true;
            } else {
                log.warn("Tentativa de remover sessão inexistente do Redis");
                return false;
            }
        } catch (Exception e) {
            log.error("Erro ao remover sessão do Redis", e);
            return false;
        }
    }

    /**
     * Verifica se uma sessão existe e é válida.
     *
     * @param token token JWT
     * @return true se a sessão existe no Redis, false caso contrário
     */
    public boolean sessionExists(String token) {
        try {
            String key = getSessionKey(token);
            Boolean exists = sessionRedisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Erro ao verificar existência da sessão no Redis", e);
            return false;
        }
    }

    /**
     * Renova o tempo de expiração de uma sessão existente.
     * Útil para implementar "keep-alive" em sessões ativas.
     *
     * @param token token JWT
     * @return true se a sessão foi renovada, false se não existe
     */
    public boolean renewSession(String token) {
        try {
            String key = getSessionKey(token);

            if (Boolean.TRUE.equals(sessionRedisTemplate.hasKey(key))) {
                Boolean renewed = sessionRedisTemplate.expire(
                        key,
                        SESSION_EXPIRATION_HOURS,
                        TimeUnit.HOURS
                );

                if (Boolean.TRUE.equals(renewed)) {
                    log.debug("Sessão renovada no Redis");
                    return true;
                }
            }

            log.warn("Tentativa de renovar sessão inexistente");
            return false;
        } catch (Exception e) {
            log.error("Erro ao renovar sessão no Redis", e);
            return false;
        }
    }

    /**
     * Obtém o tempo restante de uma sessão em segundos.
     *
     * @param token token JWT
     * @return tempo restante em segundos, ou -1 se a sessão não existir
     */
    public long getSessionTTL(String token) {
        try {
            String key = getSessionKey(token);
            Long ttl = sessionRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("Erro ao obter TTL da sessão", e);
            return -1;
        }
    }

    /**
     * Remove todas as sessões de um usuário específico.
     * Útil quando é necessário revogar todos os acessos de um usuário.
     *
     * @param userId ID do usuário
     * @return número de sessões removidas
     */
    public long removeAllUserSessions(String userId) {
        try {
            // Esta implementação requer buscar todas as chaves com o prefixo
            // Em produção, considere usar um índice adicional para mapear userId -> tokens
            log.info("Removendo todas as sessões do usuário: {}", userId);

            // Implementação básica - em produção use um índice secundário
            var keys = sessionRedisTemplate.keys(SESSION_PREFIX + "*");
            long removedCount = 0;

            if (keys != null) {
                for (String key : keys) {
                    SessionData session = sessionRedisTemplate.opsForValue().get(key);
                    if (session != null && session.getUserId().toString().equals(userId)) {
                        sessionRedisTemplate.delete(key);
                        removedCount++;
                    }
                }
            }

            log.info("Removidas {} sessões do usuário: {}", removedCount, userId);
            return removedCount;
        } catch (Exception e) {
            log.error("Erro ao remover todas as sessões do usuário: {}", userId, e);
            return 0;
        }
    }
}
