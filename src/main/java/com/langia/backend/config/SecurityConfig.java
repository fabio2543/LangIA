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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

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
     * - Proteção CSRF via Double Submit Cookie
     * - Configura política de sessão como STATELESS (usando JWT, não sessões HTTP)
     * - Injeta o filtro JWT na cadeia de filtros
     * - Configura handlers customizados para erros de autenticação/autorização
     *
     * NOTA DE SEGURANÇA (CSRF):
     * Proteção em duas camadas:
     * 1. Cookies JWT com SameSite=Lax/Strict (previne ataques cross-site em navegadores modernos)
     * 2. Double Submit Cookie: token CSRF enviado via cookie XSRF-TOKEN e validado via header X-XSRF-TOKEN
     * Ver: https://owasp.org/www-community/SameSite
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configura CSRF com Double Submit Cookie pattern
        // O cookie XSRF-TOKEN é lido pelo frontend e enviado no header X-XSRF-TOKEN
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // Não defer o token - sempre carrega para garantir que o cookie seja setado
        requestHandler.setCsrfRequestAttributeName(null);

        http
            // CSRF habilitado com Double Submit Cookie
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
                .csrfTokenRequestHandler(requestHandler)
                // Ignora CSRF para rotas públicas (login, registro, etc.)
                .ignoringRequestMatchers(
                    "/api/auth/login",
                    "/api/auth/password/**",
                    "/api/auth/email/**",
                    "/api/users/register",
                    "/api/profile/languages/available",
                    "/actuator/**"
                )
            )

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
