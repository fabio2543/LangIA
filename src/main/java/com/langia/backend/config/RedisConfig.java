package com.langia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.langia.backend.dto.UserSessionDTO;

/**
 * Redis configuration specialized for storing user sessions with JSON serialization.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, UserSessionDTO> userSessionRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, UserSessionDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();

        return template;
    }
}


