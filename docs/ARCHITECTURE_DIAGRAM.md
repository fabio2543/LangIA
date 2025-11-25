# Diagramas de Arquitetura - Sistema de Autenticação LangIA

## Visão Geral da Arquitetura

```
┌────────────────────────────────────────────────────────────────────────┐
│                           CLIENTE (Frontend)                            │
│                    React / Angular / Vue / Mobile                       │
└───────────────────────────────┬────────────────────────────────────────┘
                                │
                                │ HTTP/HTTPS
                                │
┌───────────────────────────────▼────────────────────────────────────────┐
│                        SPRING BOOT APPLICATION                          │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    SecurityFilterChain                          │  │
│  │                                                                 │  │
│  │  ┌──────────────────────────────────────────────────────────┐  │  │
│  │  │         JwtAuthenticationFilter                          │  │  │
│  │  │  • Extrai token do header Authorization                 │  │  │
│  │  │  • Valida JWT (assinatura + expiração)                  │  │  │
│  │  │  • Consulta Redis (sessão válida?)                      │  │  │
│  │  │  • Injeta SecurityContext                               │  │  │
│  │  └──────────────────────┬───────────────────────────────────┘  │  │
│  │                         │                                       │  │
│  │                         ▼                                       │  │
│  │  ┌──────────────────────────────────────────────────────────┐  │  │
│  │  │     Spring Security Authorization                        │  │  │
│  │  │  • Verifica rotas públicas (.permitAll())               │  │  │
│  │  │  • Verifica rotas protegidas (.authenticated())         │  │  │
│  │  │  • Retorna 401/403 se não autorizado                    │  │  │
│  │  └──────────────────────┬───────────────────────────────────┘  │  │
│  └─────────────────────────┼───────────────────────────────────────┘  │
│                            │                                           │
│                            ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                   CONTROLLERS LAYER                             │  │
│  │                                                                 │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │  │
│  │  │ Authentication   │  │     User         │  │   Course     │ │  │
│  │  │   Controller     │  │  Controller      │  │  Controller  │ │  │
│  │  │                  │  │                  │  │              │ │  │
│  │  │ /api/auth/*      │  │ /api/users/*     │  │ /api/courses │ │  │
│  │  └────────┬─────────┘  └────────┬─────────┘  └──────┬───────┘ │  │
│  └───────────┼─────────────────────┼────────────────────┼─────────┘  │
│              │                     │                    │             │
│              ▼                     ▼                    ▼             │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    SERVICES LAYER                               │  │
│  │                                                                 │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │  │
│  │  │ Authentication   │  │     User         │  │   Course     │ │  │
│  │  │    Service       │  │   Service        │  │   Service    │ │  │
│  │  │                  │  │                  │  │              │ │  │
│  │  │ • login()        │  │ • register()     │  │ • create()   │ │  │
│  │  │ • validate()     │  │ • update()       │  │ • find()     │ │  │
│  │  │ • logout()       │  │ • delete()       │  │ • update()   │ │  │
│  │  │ • renew()        │  │                  │  │              │ │  │
│  │  └────────┬─────────┘  └────────┬─────────┘  └──────┬───────┘ │  │
│  └───────────┼─────────────────────┼────────────────────┼─────────┘  │
│              │                     │                    │             │
│              │                     ▼                    ▼             │
│              │         ┌───────────────────────────────────────────┐ │
│              │         │     JPA REPOSITORIES                      │ │
│              │         │                                           │ │
│              │         │  • UserRepository                         │ │
│              │         │  • CourseRepository                       │ │
│              │         │  • ProfileRepository                      │ │
│              │         │  • FunctionalityRepository                │ │
│              │         └─────────────────┬─────────────────────────┘ │
│              │                           │                           │
│              ▼                           ▼                           │
│  ┌─────────────────────┐   ┌──────────────────────────────────────┐ │
│  │  SessionService     │   │        PostgreSQL Database           │ │
│  │                     │   │                                      │ │
│  │ • saveSession()     │   │  Tables:                             │ │
│  │ • getSession()      │   │  • users                             │ │
│  │ • removeSession()   │   │  • profiles                          │ │
│  │ • renewSession()    │   │  • functionalities                   │ │
│  │                     │   │  • profile_functionalities           │ │
│  └──────────┬──────────┘   └──────────────────────────────────────┘ │
│             │                                                        │
│             ▼                                                        │
│  ┌─────────────────────┐   ┌──────────────────────────────────────┐ │
│  │   JwtUtil           │   │             Redis                    │ │
│  │                     │   │                                      │ │
│  │ • generateToken()   │   │  Keys:                               │ │
│  │ • validateToken()   │   │  session:<token>                     │ │
│  │ • extractEmail()    │   │                                      │ │
│  │ • extractUserId()   │   │  TTL: 3600s (1 hora)                │ │
│  └─────────────────────┘   └──────────────────────────────────────┘ │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

---

## Fluxo de Login

```
 Cliente                Controller           Service            Repository        Redis
    │                       │                   │                    │              │
    │  POST /auth/login     │                   │                    │              │
    ├──────────────────────>│                   │                    │              │
    │                       │                   │                    │              │
    │                       │  login(dto)       │                    │              │
    │                       ├──────────────────>│                    │              │
    │                       │                   │                    │              │
    │                       │                   │  findByEmail()     │              │
    │                       │                   ├───────────────────>│              │
    │                       │                   │                    │              │
    │                       │                   │  User              │              │
    │                       │                   │<───────────────────┤              │
    │                       │                   │                    │              │
    │                       │                   │  BCrypt.matches()  │              │
    │                       │                   │  ✓ Válido          │              │
    │                       │                   │                    │              │
    │                       │                   │  JwtUtil.generate()│              │
    │                       │                   │  → token           │              │
    │                       │                   │                    │              │
    │                       │                   │  PermissionMapper  │              │
    │                       │                   │  → permissions     │              │
    │                       │                   │                    │              │
    │                       │                   │           saveSession(token, data)│
    │                       │                   ├─────────────────────────────────>│
    │                       │                   │                    │              │
    │                       │                   │           SET session:token EX 3600
    │                       │                   │                    │              │
    │                       │                   │                    OK             │
    │                       │                   │<──────────────────────────────────┤
    │                       │                   │                    │              │
    │                       │  LoginResponseDTO │                    │              │
    │                       │<──────────────────┤                    │              │
    │                       │                   │                    │              │
    │  200 OK + token       │                   │                    │              │
    │<──────────────────────┤                   │                    │              │
    │  {                    │                   │                    │              │
    │    token: "...",      │                   │                    │              │
    │    userId: "...",     │                   │                    │              │
    │    permissions: []    │                   │                    │              │
    │  }                    │                   │                    │              │
    │                       │                   │                    │              │
```

---

## Fluxo de Requisição Autenticada

```
 Cliente              Filter            Service         Redis         Controller
    │                   │                  │               │               │
    │  GET /api/courses │                  │               │               │
    │  Authorization:   │                  │               │               │
    │  Bearer <token>   │                  │               │               │
    ├──────────────────>│                  │               │               │
    │                   │                  │               │               │
    │                   │  extractToken()  │               │               │
    │                   │  → token         │               │               │
    │                   │                  │               │               │
    │                   │  validateSession(token)          │               │
    │                   ├─────────────────>│               │               │
    │                   │                  │               │               │
    │                   │                  │  JwtUtil      │               │
    │                   │                  │  .validate()  │               │
    │                   │                  │  ✓ Válido     │               │
    │                   │                  │               │               │
    │                   │                  │  getSession(token)            │
    │                   │                  ├──────────────>│               │
    │                   │                  │               │               │
    │                   │                  │  GET session:token            │
    │                   │                  │               │               │
    │                   │                  │  SessionData  │               │
    │                   │                  │<──────────────┤               │
    │                   │                  │               │               │
    │                   │  SessionData     │               │               │
    │                   │<─────────────────┤               │               │
    │                   │                  │               │               │
    │                   │  SecurityContext │               │               │
    │                   │  .setAuth(data)  │               │               │
    │                   │                  │               │               │
    │                   │  filterChain     │               │               │
    │                   │  .doFilter()     │               │               │
    │                   ├──────────────────────────────────────────────────>│
    │                   │                  │               │               │
    │                   │                  │               │  findAll()    │
    │                   │                  │               │               │
    │                   │                  │               │  List<Course> │
    │                   │                  │               │               │
    │                   │                  │               │  200 OK + data│
    │<──────────────────────────────────────────────────────────────────────┤
    │  [                │                  │               │               │
    │    { id, name },  │                  │               │               │
    │    { id, name }   │                  │               │               │
    │  ]                │                  │               │               │
    │                   │                  │               │               │
```

---

## Fluxo de Logout

```
 Cliente              Controller         Service            Redis
    │                     │                 │                  │
    │  POST /auth/logout  │                 │                  │
    │  Authorization:     │                 │                  │
    │  Bearer <token>     │                 │                  │
    ├────────────────────>│                 │                  │
    │                     │                 │                  │
    │                     │  extractToken() │                  │
    │                     │  → token        │                  │
    │                     │                 │                  │
    │                     │  logout(token)  │                  │
    │                     ├────────────────>│                  │
    │                     │                 │                  │
    │                     │                 │  removeSession(token)
    │                     │                 ├─────────────────>│
    │                     │                 │                  │
    │                     │                 │  DEL session:token
    │                     │                 │                  │
    │                     │                 │  true            │
    │                     │                 │<─────────────────┤
    │                     │                 │                  │
    │                     │  true           │                  │
    │                     │<────────────────┤                  │
    │                     │                 │                  │
    │  204 No Content     │                 │                  │
    │<────────────────────┤                 │                  │
    │                     │                 │                  │
    │                     │                 │                  │
    │  (Token agora       │                 │  (Sessão         │
    │   inválido)         │                 │   removida)      │
    │                     │                 │                  │
```

---

## Camadas da Aplicação

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│                                                              │
│  • Controllers                                               │
│  • Request/Response DTOs                                     │
│  • Validações (@Valid)                                       │
│  • Tratamento de erros (@ControllerAdvice)                   │
│                                                              │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                     SECURITY LAYER                           │
│                                                              │
│  • SecurityConfig                                            │
│  • JwtAuthenticationFilter                                   │
│  • SecurityFilterChain                                       │
│  • AuthenticationService                                     │
│                                                              │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                     BUSINESS LAYER                           │
│                                                              │
│  • Services (@Service)                                       │
│  • Lógica de negócio                                        │
│  • Validações de negócio                                    │
│  • Orquestração de operações                                │
│                                                              │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   PERSISTENCE LAYER                          │
│                                                              │
│  • Repositories (JPA)                                        │
│  • Entities (@Entity)                                        │
│  • Queries customizadas                                      │
│                                                              │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                            │
│                                                              │
│  PostgreSQL                         Redis                   │
│  • users                            • session:<token>        │
│  • profiles                         TTL: 3600s               │
│  • functionalities                                           │
│  • profile_functionalities                                   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Modelo de Dados

### PostgreSQL

```
┌─────────────────────┐
│       users         │
├─────────────────────┤
│ id (UUID)    PK     │
│ name                │
│ email        UNIQUE │
│ password            │
│ cpf_string          │
│ phone        UNIQUE │
│ profile     (ENUM)  │
│ created_at          │
│ updated_at          │
└─────────────────────┘
          │
          │ (profile FK)
          ▼
┌─────────────────────┐
│      profiles       │
├─────────────────────┤
│ id           PK     │
│ code (ENUM)  UNIQUE │
│ name                │
│ description         │
│ hierarchy_level     │
│ active              │
│ created_at          │
│ updated_at          │
└─────────┬───────────┘
          │
          │ (FK)
          ▼
┌──────────────────────────────┐
│   profile_functionalities    │
├──────────────────────────────┤
│ id                    PK     │
│ profile_id            FK ────┤
│ functionality_id      FK ──┐ │
│ granted_by_inheritance     │ │
│ granted_at                 │ │
└────────────────────────────┼─┘
                             │
                             ▼
                    ┌─────────────────────┐
                    │  functionalities    │
                    ├─────────────────────┤
                    │ id           PK     │
                    │ code         UNIQUE │
                    │ description         │
                    │ module (ENUM)       │
                    │ active              │
                    │ created_at          │
                    │ updated_at          │
                    └─────────────────────┘
```

### Redis

```
KEY: session:<token>

VALUE: {
  "userId": "uuid",
  "name": "string",
  "email": "string",
  "profile": "STUDENT|TEACHER|ADMIN",
  "permissions": ["string"],
  "createdAt": timestamp
}

TTL: 3600 seconds (1 hora)
```

---

## Estrutura de Pacotes

```
com.langia.backend
│
├── config/
│   ├── SecurityConfig.java          # Configuração Spring Security
│   ├── RedisConfig.java              # Configuração Redis
│   └── TestSecurityConfig.java       # Config para testes
│
├── controller/
│   ├── AuthenticationController.java # Endpoints de autenticação
│   └── UserController.java           # Endpoints de usuários
│
├── service/
│   ├── AuthenticationService.java    # Lógica de autenticação
│   ├── SessionService.java           # Gerenciamento Redis
│   └── UserService.java              # Lógica de usuários
│
├── filter/
│   └── JwtAuthenticationFilter.java  # Interceptação HTTP
│
├── repository/
│   ├── UserRepository.java           # Acesso banco users
│   ├── ProfileRepository.java        # Acesso banco profiles
│   └── FunctionalityRepository.java  # Acesso banco functionalities
│
├── model/
│   ├── User.java                     # Entidade usuário
│   ├── Profile.java                  # Entidade perfil
│   ├── Functionality.java            # Entidade funcionalidade
│   ├── ProfileFunctionality.java     # Tabela de junção
│   └── UserProfile.java              # Enum de perfis
│
├── dto/
│   ├── LoginRequestDTO.java          # Request de login
│   ├── LoginResponseDTO.java         # Response de login
│   └── SessionData.java              # Dados de sessão
│
├── util/
│   ├── JwtUtil.java                  # Utilitários JWT
│   ├── PermissionMapper.java         # Mapeamento permissões
│   ├── CpfValidator.java             # Validação CPF
│   └── PhoneValidator.java           # Validação telefone
│
├── exception/
│   └── InvalidCredentialsException.java
│
└── validation/
    ├── ValidCpf.java                 # Annotation CPF
    └── ValidPhone.java               # Annotation telefone
```

---

## Tecnologias Utilizadas

```
┌────────────────────────────────────────────┐
│           BACKEND STACK                    │
├────────────────────────────────────────────┤
│ Spring Boot 3.5.7                          │
│ Spring Security 6.x                        │
│ Spring Data JPA                            │
│ Spring Data Redis                          │
├────────────────────────────────────────────┤
│ PostgreSQL 15                              │
│ Redis 7                                    │
├────────────────────────────────────────────┤
│ JWT (io.jsonwebtoken) 0.12.3              │
│ BCrypt (Spring Security Crypto)            │
│ Lombok                                     │
├────────────────────────────────────────────┤
│ JUnit 5                                    │
│ Mockito                                    │
│ MockMvc (Spring Test)                      │
└────────────────────────────────────────────┘
```

---

**Última atualização:** 25 de Novembro de 2025
