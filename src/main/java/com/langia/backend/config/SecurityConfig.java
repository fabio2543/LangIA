package com.langia.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.langia.backend.filter.JwtAuthenticationFilter;

/**
 * Configuração de segurança do Spring Security.
 * Define rotas públicas, rotas protegidas e configura o filtro JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Construtor com injeção de dependência.
     * Usa @Autowired(required = false) para permitir contextos de teste sem o filtro.
     */
    public SecurityConfig(@Autowired(required = false) JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configura a cadeia de filtros de segurança.
     *
     * Define:
     * - Rotas públicas que não exigem autenticação
     * - Rotas protegidas que exigem token JWT válido
     * - Desabilita CSRF (não necessário para API stateless)
     * - Configura política de sessão como STATELESS (usando JWT, não sessões HTTP)
     * - Injeta o filtro JWT na cadeia de filtros
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF pois API é stateless com JWT
            .csrf(csrf -> csrf.disable())

            // Configura autorização de requisições
            .authorizeHttpRequests(auth -> auth
                // Rotas públicas - não exigem autenticação
                .requestMatchers(
                    "/api/auth/login",      // Login
                    "/api/auth/register",   // Cadastro via auth
                    "/api/users/register",  // Cadastro de usuários
                    "/h2-console/**",       // Console H2 (apenas dev)
                    "/actuator/**",         // Endpoints de monitoramento
                    "/error"                // Página de erro
                ).permitAll()

                // Todas as outras rotas exigem autenticação
                .anyRequest().authenticated()
            )

            // Configura política de sessão como STATELESS
            // Não cria sessões HTTP pois usamos JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        // Adiciona o filtro JWT antes do filtro padrão de autenticação, se disponível
        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );
        }

        return http.build();
    }

    /**
     * Bean do encoder de senhas BCrypt.
     * Usado para criptografar senhas com salt aleatório.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
