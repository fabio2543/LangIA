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
 * Responsável por criar, recuperar e remover sessões com TTL configurável.
 */
@Service
@Slf4j
public class SessionService {

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";

    @Autowired
    private RedisTemplate<String, SessionData> sessionRedisTemplate;

    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;

    @Value("${jwt.expiration}")
    private Long jwtExpirationMs;

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
     * Salva uma nova sessão no Redis com TTL baseado na configuração jwt.expiration.
     *
     * @param token token JWT usado como chave
     * @param sessionData dados da sessão a serem salvos
     */
    public void saveSession(String token, SessionData sessionData) {
        try {
            String key = getSessionKey(token);
            sessionData.setCreatedAt(System.currentTimeMillis());

            // Usa o tempo de expiração configurado (mesmo do JWT)
            sessionRedisTemplate.opsForValue().set(
                    key,
                    sessionData,
                    jwtExpirationMs,
                    TimeUnit.MILLISECONDS
            );

            // Adiciona token ao índice de sessões do usuário
            String userSessionsKey = USER_SESSIONS_PREFIX + sessionData.getUserId();
            stringRedisTemplate.opsForSet().add(userSessionsKey, token);
            stringRedisTemplate.expire(userSessionsKey, jwtExpirationMs, TimeUnit.MILLISECONDS);

            log.info("Sessão criada no Redis para usuário: {} (ID: {}) com TTL de {}ms",
                    sessionData.getEmail(), sessionData.getUserId(), jwtExpirationMs);
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

            // Recupera a sessão antes de remover para limpar o índice
            SessionData sessionData = sessionRedisTemplate.opsForValue().get(key);

            Boolean deleted = sessionRedisTemplate.delete(key);

            // Remove do índice de sessões do usuário
            if (sessionData != null) {
                String userSessionsKey = USER_SESSIONS_PREFIX + sessionData.getUserId();
                stringRedisTemplate.opsForSet().remove(userSessionsKey, token);
            }

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
     * Usa o mesmo TTL configurado em jwt.expiration.
     *
     * @param token token JWT
     * @return true se a sessão foi renovada, false se não existe
     */
    public boolean renewSession(String token) {
        try {
            String key = getSessionKey(token);

            if (Boolean.TRUE.equals(sessionRedisTemplate.hasKey(key))) {
                // Renova com o mesmo TTL configurado
                Boolean renewed = sessionRedisTemplate.expire(
                        key,
                        jwtExpirationMs,
                        TimeUnit.MILLISECONDS
                );

                if (Boolean.TRUE.equals(renewed)) {
                    log.debug("Sessão renovada no Redis com TTL de {}ms", jwtExpirationMs);
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
     * Usa índice auxiliar (user_sessions:userId) para performance O(1) ao invés de
     * scan O(N) no Redis, evitando operações custosas em produção.
     *
     * @param userId ID do usuário
     * @return número de sessões removidas
     */
    public long removeAllUserSessions(String userId) {
        try {
            log.info("Removendo todas as sessões do usuário: {}", userId);
            long removedCount = 0;

            // Usa índice auxiliar para buscar tokens do usuário (O(1))
            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            var tokens = stringRedisTemplate.opsForSet().members(userSessionsKey);

            if (tokens != null && !tokens.isEmpty()) {
                for (String token : tokens) {
                    String sessionKey = getSessionKey(token);
                    Boolean deleted = sessionRedisTemplate.delete(sessionKey);
                    if (Boolean.TRUE.equals(deleted)) {
                        removedCount++;
                    }
                }

                // Remove o índice de sessões do usuário
                stringRedisTemplate.delete(userSessionsKey);
            }

            log.info("Removidas {} sessões do usuário: {}", removedCount, userId);
            return removedCount;
        } catch (Exception e) {
            log.error("Erro ao remover todas as sessões do usuário: {}", userId, e);
            return 0;
        }
    }
}
