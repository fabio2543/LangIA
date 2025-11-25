package com.langia.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.langia.backend.dto.SessionData;

/**
 * Configuração do Redis para gerenciamento de sessões.
 * Define o RedisTemplate com serialização apropriada para SessionData.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    /**
     * Configura a factory de conexão com Redis.
     *
     * @return factory de conexão Lettuce configurada
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * Configura o RedisTemplate para SessionData.
     * Usa StringRedisSerializer para chaves e GenericJackson2JsonRedisSerializer para valores.
     *
     * @param connectionFactory factory de conexão Redis
     * @return RedisTemplate configurado
     */
    @Bean
    public RedisTemplate<String, SessionData> sessionRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, SessionData> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Serializa chaves como String
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Serializa valores como JSON
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
