package com.langia.backend.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.langia.backend.dto.LoginRequestDTO;
import com.langia.backend.dto.LoginResponseDTO;
import com.langia.backend.dto.SessionData;
import com.langia.backend.model.User;
import com.langia.backend.repository.UserRepository;
import com.langia.backend.util.JwtUtil;
import com.langia.backend.util.PermissionMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço central de autenticação.
 * Orquestra o processo completo de login, validação de credenciais,
 * geração de tokens JWT e gerenciamento de sessões no Redis.
 */
@Service
@Slf4j
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PermissionMapper permissionMapper;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Realiza o processo completo de autenticação do usuário.
     *
     * Fluxo:
     * 1. Busca o usuário pelo email
     * 2. Valida a senha usando BCrypt
     * 3. Gera token JWT
     * 4. Salva sessão no Redis
     * 5. Busca permissões do perfil
     * 6. Retorna resposta completa com token e dados do usuário
     *
     * @param loginRequest credenciais de login (email e senha)
     * @return resposta de login com token e informações do usuário
     * @throws RuntimeException se as credenciais forem inválidas
     */
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Tentativa de login para o email: {}", loginRequest.getEmail());

        // 1. Busca o usuário pelo email
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            log.warn("Tentativa de login com email não cadastrado: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOptional.get();

        // 2. Valida a senha usando BCrypt
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Tentativa de login com senha incorreta para o email: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        log.info("Credenciais válidas para usuário: {} (ID: {})", user.getEmail(), user.getId());

        // 3. Gera token JWT
        String token = jwtUtil.generateToken(user);

        // 4. Busca permissões do perfil
        Set<String> permissions = permissionMapper.getPermissionsForProfile(user.getProfile());

        // 5. Salva sessão no Redis
        SessionData sessionData = SessionData.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profile(user.getProfile())
                .permissions(permissions)
                .build();

        sessionService.saveSession(token, sessionData);

        log.info("Login bem-sucedido para usuário: {} (Perfil: {})", user.getEmail(), user.getProfile());

        // 6. Retorna resposta completa
        return LoginResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profile(user.getProfile())
                .permissions(permissions)
                .expiresIn(jwtExpiration)
                .build();
    }

    /**
     * Valida se uma sessão ainda é válida.
     * Verifica tanto a validade do token JWT quanto a existência da sessão no Redis.
     *
     * @param token token JWT a ser validado
     * @return dados da sessão se válida, null caso contrário
     */
    public SessionData validateSession(String token) {
        log.debug("Validando sessão para token");

        // Valida o token JWT (assinatura e expiração)
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token JWT inválido ou expirado");
            return null;
        }

        // Busca a sessão no Redis
        SessionData sessionData = sessionService.getSession(token);

        if (sessionData == null) {
            log.warn("Sessão não encontrada no Redis para token válido");
            return null;
        }

        log.debug("Sessão válida para usuário: {} (ID: {})",
                sessionData.getEmail(), sessionData.getUserId());

        return sessionData;
    }

    /**
     * Realiza o logout do usuário, removendo a sessão do Redis.
     * O token será invalidado imediatamente, mesmo que ainda não tenha expirado.
     *
     * @param token token JWT da sessão a ser encerrada
     * @return true se o logout foi bem-sucedido, false se a sessão não existia
     */
    public boolean logout(String token) {
        log.info("Processando logout");

        boolean removed = sessionService.removeSession(token);

        if (removed) {
            log.info("Logout realizado com sucesso");
        } else {
            log.warn("Tentativa de logout de sessão inexistente ou já expirada");
        }

        return removed;
    }

    /**
     * Verifica se um token representa uma sessão válida e ativa.
     * Método auxiliar para uso em filtros de interceptação.
     *
     * @param token token JWT
     * @return true se a sessão for válida e ativa, false caso contrário
     */
    public boolean isSessionValid(String token) {
        return validateSession(token) != null;
    }

    /**
     * Renova o tempo de expiração de uma sessão ativa.
     * Útil para implementar mecanismo de "keep-alive" em sessões de usuários ativos.
     *
     * @param token token JWT
     * @return true se a sessão foi renovada com sucesso, false caso contrário
     */
    public boolean renewSession(String token) {
        log.debug("Renovando sessão");

        // Valida que o token JWT ainda é válido
        if (!jwtUtil.validateToken(token)) {
            log.warn("Tentativa de renovar sessão com token JWT inválido");
            return false;
        }

        // Renova a expiração no Redis
        boolean renewed = sessionService.renewSession(token);

        if (renewed) {
            log.debug("Sessão renovada com sucesso");
        } else {
            log.warn("Falha ao renovar sessão - sessão pode não existir");
        }

        return renewed;
    }
}
