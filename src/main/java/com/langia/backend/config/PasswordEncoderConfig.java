package com.langia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuração do PasswordEncoder separada para evitar dependência circular.
 * O BCryptPasswordEncoder é usado para criptografar senhas com salt aleatório.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Bean do encoder de senhas BCrypt.
     * Separado do SecurityConfig para evitar dependência circular com AuthenticationService.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
