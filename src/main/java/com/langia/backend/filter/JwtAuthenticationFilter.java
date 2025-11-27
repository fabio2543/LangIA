package com.langia.backend.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.langia.backend.dto.SessionData;
import com.langia.backend.service.AuthenticationService;
import com.langia.backend.util.TokenExtractor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro de autenticação JWT que intercepta todas as requisições HTTP.
 * Responsável por extrair, validar tokens JWT e verificar sessões no Redis.
 *
 * Este filtro funciona como guardião da aplicação, validando automaticamente
 * a autenticação antes que as requisições cheguem aos controllers.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private TokenExtractor tokenExtractor;

    /**
     * Método principal do filtro que processa cada requisição HTTP.
     *
     * Fluxo de execução:
     * 1. Extrai token do header Authorization
     * 2. Se não houver token, permite requisição continuar (rota pública)
     * 3. Valida token JWT e verifica sessão no Redis
     * 4. Injeta informações do usuário no SecurityContext
     * 5. Permite requisição continuar para o próximo filtro/controller
     * 6. Limpa contexto de segurança após processamento
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extrai o token JWT priorizando cookie HttpOnly, com fallback para header
            String token = tokenExtractor.extractFromRequest(request);

            // 2. Se não houver token, continua sem autenticação (rota pública)
            if (token == null) {
                log.debug("Requisição sem token JWT - permitindo acesso como anônimo");
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Valida o token e verifica sessão no Redis
            SessionData sessionData = authenticationService.validateSession(token);

            // 4. Se a sessão for válida, injeta o contexto de segurança
            if (sessionData != null) {
                log.debug("Token válido para usuário: {} (ID: {}) com {} permissões",
                        sessionData.getEmail(), sessionData.getUserId(),
                        sessionData.getPermissions() != null ? sessionData.getPermissions().size() : 0);

                // Converte permissões do SessionData para GrantedAuthority do Spring Security
                Collection<? extends GrantedAuthority> authorities =
                    sessionData.getPermissions() != null
                        ? sessionData.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission))
                            .collect(Collectors.toList())
                        : java.util.Collections.emptyList();

                // Cria objeto de autenticação do Spring Security com permissões
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        sessionData,           // Principal (dados do usuário)
                        null,                  // Credentials (não necessárias após autenticação)
                        authorities            // Authorities (permissões convertidas)
                    );

                // Adiciona detalhes da requisição HTTP
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Injeta no contexto de segurança do Spring
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Contexto de segurança configurado para usuário: {} com authorities: {}",
                        sessionData.getEmail(), authorities);
            } else {
                log.warn("Token presente mas inválido ou sessão expirada - acesso negado");
                // Token inválido ou sessão não existe
                // O Spring Security bloqueará automaticamente se a rota for protegida
            }

            // 5. Continua a cadeia de filtros
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Erro ao processar autenticação JWT: {}", e.getMessage(), e);
            // Em caso de erro, limpa o contexto e permite que o Spring Security lide
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Determina se o filtro deve ser executado para uma requisição específica.
     *
     * Pode ser sobrescrito para pular o filtro em rotas específicas se necessário.
     * Por padrão, o filtro processa todas as requisições.
     *
     * @param request requisição HTTP
     * @return true se o filtro não deve ser executado, false caso contrário
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Por padrão, processa todas as requisições
        // A configuração de rotas públicas/protegidas é feita no SecurityConfig
        return false;
    }
}
