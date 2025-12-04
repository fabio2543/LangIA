package com.langia.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.langia.backend.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Configuração de segurança do Spring Security.
 * Define rotas públicas, rotas protegidas e configura o filtro JWT.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Lazy
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configura a cadeia de filtros de segurança.
     *
     * Define:
     * - Rotas públicas que não exigem autenticação
     * - Rotas protegidas que exigem token JWT válido
     * - Desabilita CSRF do Spring (proteção via SameSite cookie)
     * - Configura política de sessão como STATELESS (usando JWT, não sessões HTTP)
     * - Injeta o filtro JWT na cadeia de filtros
     * - Configura handlers customizados para erros de autenticação/autorização
     *
     * NOTA DE SEGURANÇA (CSRF):
     * O CSRF do Spring Security está desabilitado porque usamos cookies HttpOnly com
     * atributo SameSite=Lax (configurável para Strict em produção). O SameSite previne
     * que o cookie seja enviado em requisições cross-site, fornecendo proteção equivalente
     * ou superior ao token CSRF tradicional para navegadores modernos.
     * Ver: https://owasp.org/www-community/SameSite
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF desabilitado - proteção fornecida pelo atributo SameSite do cookie
            // O cookie JWT usa SameSite=Lax/Strict (configurado via auth.cookie.same-site)
            .csrf(csrf -> csrf.disable())

            // Configura autorização de requisições
            .authorizeHttpRequests(auth -> auth
                // Rotas publicas - nao exigem autenticacao
                .requestMatchers(
                    "/api/auth/login",          // Login
                    "/api/auth/password/**",    // Recuperacao de senha
                    "/api/auth/email/**",       // Verificacao de e-mail
                    "/api/users/register",      // Cadastro de usuarios
                    "/api/profile/languages/available", // Lista de idiomas disponíveis
                    "/h2-console/**",           // Console H2 (apenas dev)
                    "/actuator/**",             // Endpoints de monitoramento
                    "/error"                    // Pagina de erro
                ).permitAll()

                // Todas as outras rotas exigem autenticação
                .anyRequest().authenticated()
            )

            // Configura política de sessão como STATELESS
            // Não cria sessões HTTP pois usamos JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configura handlers customizados para erros de autenticação/autorização
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            );

        // Adiciona o filtro JWT antes do filtro padrão de autenticação
        http.addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}
