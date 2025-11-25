# Configuração de Segurança - LangIA

Este documento descreve a configuração completa de segurança do sistema LangIA, incluindo autenticação JWT, gerenciamento de sessões e políticas de autorização.

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura de Segurança](#arquitetura-de-segurança)
3. [Componentes Principais](#componentes-principais)
4. [Configuração do Spring Security](#configuração-do-spring-security)
5. [Rotas Públicas e Privadas](#rotas-públicas-e-privadas)
6. [Fluxo de Autenticação](#fluxo-de-autenticação)
7. [Gerenciamento de Sessões](#gerenciamento-de-sessões)
8. [Tratamento de Erros](#tratamento-de-erros)
9. [Testes](#testes)
10. [Exemplos de Uso](#exemplos-de-uso)

---

## Visão Geral

O LangIA utiliza uma arquitetura de segurança **stateless** baseada em:

- **JWT (JSON Web Tokens)** para autenticação
- **Redis** para gerenciamento de sessões
- **Spring Security 6.x** para controle de acesso
- **BCrypt** para hash de senhas

### Características Principais

✅ API RESTful stateless
✅ Tokens JWT com expiração de 1 hora
✅ Validação dupla: JWT + Redis
✅ Revogação imediata via logout
✅ Proteção automática de endpoints
✅ Separação clara de rotas públicas/privadas

---

## Arquitetura de Segurança

```
┌─────────────────────────────────────────────────────────────┐
│                        Cliente HTTP                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              JwtAuthenticationFilter                         │
│  • Extrai token do header Authorization                      │
│  • Valida JWT (assinatura + expiração)                      │
│  • Verifica sessão no Redis                                 │
│  • Injeta SecurityContext                                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Security FilterChain                     │
│  • Verifica rotas públicas (permitAll)                      │
│  • Verifica autenticação (authenticated)                    │
│  • Retorna 401/403 se não autorizado                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  Controller/Service                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Componentes Principais

### 1. SecurityConfig

**Localização:** `src/main/java/com/langia/backend/config/SecurityConfig.java`

**Responsabilidades:**
- Configurar cadeia de filtros de segurança
- Definir rotas públicas e protegidas
- Registrar filtros customizados
- Configurar políticas de sessão

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // Configuração completa...
    }
}
```

### 2. JwtAuthenticationFilter

**Localização:** `src/main/java/com/langia/backend/filter/JwtAuthenticationFilter.java`

**Responsabilidades:**
- Interceptar todas as requisições HTTP
- Extrair e validar token JWT
- Verificar sessão no Redis
- Injetar contexto de segurança

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) {
        // Lógica de validação...
    }
}
```

### 3. AuthenticationService

**Localização:** `src/main/java/com/langia/backend/service/AuthenticationService.java`

**Responsabilidades:**
- Orquestrar processo de login
- Validar credenciais
- Gerenciar sessões (criar, validar, renovar, remover)

```java
@Service
public class AuthenticationService {

    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. Validar credenciais
        // 2. Gerar JWT
        // 3. Salvar sessão no Redis
        // 4. Retornar resposta
    }

    public SessionData validateSession(String token) {
        // Validar JWT + Redis
    }
}
```

### 4. SessionService

**Localização:** `src/main/java/com/langia/backend/service/SessionService.java`

**Responsabilidades:**
- Gerenciar sessões no Redis
- TTL de 1 hora
- Operações CRUD de sessões

---

## Configuração do Spring Security

### Desabilitação de Funcionalidades Desnecessárias

#### CSRF Protection

```java
.csrf(csrf -> csrf.disable())
```

**Motivo:** API stateless não usa cookies de sessão, portanto CSRF não se aplica.

#### Política de Sessão Stateless

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

**Efeitos:**
- Não cria `HttpSession`
- Não armazena `SecurityContext` em sessão
- Cada requisição é independente
- Estado mantido apenas no Redis

### Registro do Filtro JWT

```java
if (jwtAuthenticationFilter != null) {
    http.addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
    );
}
```

**Ordem de Execução:**
1. `JwtAuthenticationFilter` (customizado)
2. `UsernamePasswordAuthenticationFilter` (padrão)
3. Outros filtros do Spring Security

---

## Rotas Públicas e Privadas

### Rotas Públicas (permitAll)

Não exigem autenticação:

| Rota | Descrição |
|------|-----------|
| `/api/auth/login` | Endpoint de login |
| `/api/auth/register` | Cadastro de usuários (auth) |
| `/api/users/register` | Cadastro de usuários (users) |
| `/h2-console/**` | Console H2 (apenas desenvolvimento) |
| `/actuator/**` | Endpoints de monitoramento/health |
| `/error` | Tratamento de erros do Spring |

**Configuração:**

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/api/auth/login",
        "/api/auth/register",
        "/api/users/register",
        "/h2-console/**",
        "/actuator/**",
        "/error"
    ).permitAll()
)
```

### Rotas Protegidas (authenticated)

Todas as demais rotas exigem token JWT válido:

| Rota | Descrição |
|------|-----------|
| `/api/auth/logout` | Encerrar sessão |
| `/api/auth/validate` | Validar token |
| `/api/auth/renew` | Renovar sessão |
| `/**` | Qualquer outra rota |

**Configuração:**

```java
.anyRequest().authenticated()
```

### Matriz de Autorização

| Endpoint | Token | Sessão Redis | Sem Token | Token Inválido | Token Válido |
|----------|-------|--------------|-----------|----------------|--------------|
| `/api/auth/login` | ❌ | ❌ | 200 OK | 200 OK | 200 OK |
| `/api/users/register` | ❌ | ❌ | 200 OK | 200 OK | 200 OK |
| `/api/auth/logout` | ✅ | ✅ | 401 | 401 | 204 |
| `/api/auth/validate` | ✅ | ✅ | 401 | 401 | 200 |
| `/api/auth/renew` | ✅ | ✅ | 401 | 401 | 200 |

---

## Fluxo de Autenticação

### 1. Login

```mermaid
sequenceDiagram
    Cliente->>Controller: POST /api/auth/login
    Controller->>AuthService: login(credentials)
    AuthService->>UserRepository: findByEmail()
    AuthService->>PasswordEncoder: matches(password)
    AuthService->>JwtUtil: generateToken(user)
    AuthService->>PermissionMapper: getPermissions()
    AuthService->>SessionService: saveSession(token)
    SessionService->>Redis: SET session:token
    AuthService->>Controller: LoginResponseDTO
    Controller->>Cliente: 200 OK + token
```

### 2. Requisição Autenticada

```mermaid
sequenceDiagram
    Cliente->>JwtFilter: GET /api/protected
    JwtFilter->>JwtFilter: extractToken()
    JwtFilter->>AuthService: validateSession(token)
    AuthService->>JwtUtil: validateToken()
    AuthService->>SessionService: getSession(token)
    SessionService->>Redis: GET session:token
    SessionService->>AuthService: SessionData
    AuthService->>JwtFilter: SessionData
    JwtFilter->>SecurityContext: setAuthentication()
    JwtFilter->>Controller: continuar requisição
    Controller->>Cliente: 200 OK + dados
```

### 3. Logout

```mermaid
sequenceDiagram
    Cliente->>Controller: POST /api/auth/logout
    Controller->>AuthService: logout(token)
    AuthService->>SessionService: removeSession(token)
    SessionService->>Redis: DEL session:token
    SessionService->>AuthService: true
    AuthService->>Controller: true
    Controller->>Cliente: 204 No Content
```

---

## Gerenciamento de Sessões

### Estrutura de Sessão no Redis

**Chave:** `session:<token>`
**Valor:** JSON do `SessionData`
**TTL:** 1 hora (3600 segundos)

**Exemplo:**

```
KEY: session:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
VALUE: {
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "João Silva",
  "email": "joao@example.com",
  "profile": "STUDENT",
  "permissions": ["view_courses", "view_lessons"],
  "createdAt": 1700000000000
}
TTL: 3600
```

### Operações de Sessão

#### Criar Sessão

```java
sessionService.saveSession(token, sessionData);
// Redis: SET session:token value EX 3600
```

#### Validar Sessão

```java
SessionData data = sessionService.getSession(token);
// Redis: GET session:token
// Retorna: SessionData ou null
```

#### Renovar Sessão

```java
boolean renewed = sessionService.renewSession(token);
// Redis: EXPIRE session:token 3600
```

#### Remover Sessão

```java
boolean removed = sessionService.removeSession(token);
// Redis: DEL session:token
```

### Expiração Automática

- **TTL:** 1 hora desde última atividade
- **Renovação:** Endpoint `/api/auth/renew`
- **Expiração JWT:** Sincronizada com Redis
- **Revogação:** Logout remove do Redis imediatamente

---

## Tratamento de Erros

### Códigos de Status HTTP

| Código | Cenário | Resposta |
|--------|---------|----------|
| 200 | Login bem-sucedido | `LoginResponseDTO` |
| 204 | Logout bem-sucedido | Sem corpo |
| 400 | Validação falhou | `{"message": "Email is required"}` |
| 401 | Credenciais inválidas | `{"message": "Invalid credentials"}` |
| 401 | Token ausente/inválido | `{"message": "Missing or invalid token"}` |
| 403 | Sem permissão | `{"message": "Access denied"}` |
| 500 | Erro interno | `{"message": "An unexpected error occurred"}` |

### Exemplos de Respostas de Erro

#### Credenciais Inválidas (401)

```json
{
  "message": "Invalid credentials"
}
```

#### Validação de Dados (400)

```json
{
  "timestamp": "2025-11-25T00:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email must be valid"
    }
  ]
}
```

#### Token Inválido (401)

```json
{
  "message": "Missing or invalid Authorization header"
}
```

---

## Testes

### Cobertura de Testes

| Componente | Testes | Status |
|------------|--------|--------|
| SecurityConfig | Integração | ✅ |
| JwtAuthenticationFilter | 17 unitários | ✅ 100% |
| AuthenticationService | 16 unitários | ✅ 100% |
| AuthenticationController | 21 integração | ✅ 100% |
| SessionService | 15 integração | ✅ 100% |

### Exemplo de Teste

```java
@Test
void deveInjetarContextoDeSegurancaParaSessaoValida() {
    // Arrange
    request.addHeader("Authorization", "Bearer " + validToken);
    when(authenticationService.validateSession(validToken))
        .thenReturn(validSessionData);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth);
    assertEquals(validSessionData, auth.getPrincipal());
}
```

---

## Exemplos de Uso

### Login

**Request:**

```http
POST /api/auth/login HTTP/1.1
Content-Type: application/json

{
  "email": "joao@example.com",
  "password": "senha123"
}
```

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "João Silva",
  "email": "joao@example.com",
  "profile": "STUDENT",
  "permissions": [
    "view_courses",
    "view_lessons",
    "submit_exercises",
    "view_progress",
    "chat_with_ai",
    "view_profile",
    "update_profile"
  ],
  "expiresIn": 3600000
}
```

### Requisição Autenticada

**Request:**

```http
GET /api/protected-resource HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**

```json
{
  "data": "Dados protegidos"
}
```

### Validar Sessão

**Request:**

```http
GET /api/auth/validate HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**

```json
{
  "valid": true,
  "session": {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "João Silva",
    "email": "joao@example.com",
    "profile": "STUDENT",
    "permissions": ["view_courses", "view_lessons"],
    "createdAt": 1700000000000
  }
}
```

### Renovar Sessão

**Request:**

```http
POST /api/auth/renew HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**

```json
{
  "message": "Session renewed successfully"
}
```

### Logout

**Request:**

```http
POST /api/auth/logout HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (204 No Content):**

```
(sem corpo)
```

---

## Configuração de Ambiente

### application.properties

```properties
# JWT Configuration
jwt.secret=your-secret-key-min-256-bits-change-in-production
jwt.expiration=3600000

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=60000

# Security
spring.security.user.name=admin
spring.security.user.password=admin
```

### Variáveis de Ambiente

```bash
# Produção
export JWT_SECRET="production-secret-key-very-long-and-random"
export JWT_EXPIRATION=3600000
export REDIS_HOST="redis.production.com"
export REDIS_PORT=6379
```

---

## Boas Práticas

### ✅ Segurança

- Sempre use HTTPS em produção
- Mantenha `jwt.secret` seguro e longo (min. 256 bits)
- Não exponha detalhes de erro em produção
- Implemente rate limiting
- Monitore tentativas de login falhas
- Use tokens de curta duração
- Implemente refresh tokens para sessões longas

### ✅ Performance

- Redis para cache de sessões (rápido)
- Índices no banco de dados
- Paginação em listas grandes
- Compressão de respostas JSON

### ✅ Manutenibilidade

- Centralize configurações de segurança
- Documente rotas públicas
- Mantenha testes atualizados
- Use logging apropriado
- Versionamento de API

---

## Troubleshooting

### Problema: 401 em rota pública

**Solução:** Verifique se a rota está em `requestMatchers().permitAll()`

### Problema: Token válido mas 401

**Solução:** Verifique se a sessão existe no Redis (`redis-cli GET session:<token>`)

### Problema: CORS errors

**Solução:** Adicione configuração CORS no `SecurityConfig`

```java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

### Problema: Sessão expira muito rápido

**Solução:** Ajuste `SESSION_EXPIRATION_HOURS` em `SessionService`

---

## Referências

- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [JWT.io](https://jwt.io/)
- [Redis Documentation](https://redis.io/documentation)
- [BCrypt](https://github.com/spring-projects/spring-security/tree/main/crypto)

---

## Changelog

| Versão | Data | Descrição |
|--------|------|-----------|
| 1.0.0 | 2025-11-25 | Implementação inicial do sistema de autenticação JWT |

---

**Última atualização:** 25 de Novembro de 2025
**Mantido por:** Equipe LangIA
