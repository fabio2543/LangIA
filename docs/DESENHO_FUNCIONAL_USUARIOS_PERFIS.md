# Desenho Funcional - Sistema de Usuários e Perfis
## LangIA Platform

---

## 📋 Sumário
1. [Visão Geral](#visão-geral)
2. [Modelo de Dados](#modelo-de-dados)
3. [Tipos de Perfis](#tipos-de-perfis)
4. [Fluxos Funcionais](#fluxos-funcionais)
5. [Autenticação e Autorização](#autenticação-e-autorização)
6. [Segurança](#segurança)
7. [Regras de Negócio](#regras-de-negócio)

---

## 🎯 Visão Geral

O sistema de usuários e perfis do LangIA é baseado em uma arquitetura de **controle de acesso baseado em perfis (Profile-Based Access Control)**, onde cada usuário possui um perfil específico que determina suas permissões e funcionalidades disponíveis.

### Componentes Principais
- **User Entity**: Entidade principal de usuário
- **UserProfile Enum**: Definição dos tipos de perfil
- **JWT Authentication**: Sistema de autenticação baseado em tokens
- **BCrypt Encryption**: Criptografia de senhas

---

## 🗃️ Modelo de Dados

### Entidade User
```
┌─────────────────────────────────────┐
│           USER TABLE                │
├─────────────────────────────────────┤
│ id            UUID (PK)             │
│ name          VARCHAR(255)          │
│ email         VARCHAR(255) UNIQUE   │
│ cpf_string    VARCHAR(255)          │
│ password      VARCHAR(255)          │
│ profile       ENUM                  │
│ phone         VARCHAR(20) UNIQUE    │
│ created_at    TIMESTAMP             │
│ updated_at    TIMESTAMP             │
└─────────────────────────────────────┘
```

### Campos Detalhados

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `id` | UUID | Auto-gerado | Identificador único do usuário |
| `name` | String | @NotBlank | Nome completo do usuário |
| `email` | String | @Email, @NotBlank, Único | Email para login e comunicação |
| `cpf_string` | String | @NotBlank | CPF do usuário |
| `password` | String | @NotBlank, Min 6 chars | Senha criptografada (BCrypt) |
| `profile` | Enum | @NotNull | Perfil/Tipo de usuário |
| `phone` | String | @NotBlank, Único | Telefone único do usuário |
| `created_at` | Timestamp | Auto-gerado | Data de criação do registro |
| `updated_at` | Timestamp | Auto-atualizado | Data da última atualização |

---

## 👥 Tipos de Perfis

O sistema suporta 3 tipos de perfis, cada um com permissões e funcionalidades específicas:

```
┌──────────────────────────────────────────────────────────┐
│                    USER PROFILES                          │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
│  │   STUDENT   │  │   TEACHER   │  │    ADMIN    │      │
│  └─────────────┘  └─────────────┘  └─────────────┘      │
│       │                 │                 │              │
│       └─────────────────┴─────────────────┘              │
│                    Hierarquia                            │
└──────────────────────────────────────────────────────────┘
```

### 1. STUDENT (Estudante)
**Funcionalidades:**
- Acesso a conteúdos educacionais
- Interação com assistentes IA
- Visualização de materiais didáticos
- Comunicação via WhatsApp com bot
- Acompanhamento de progresso

**Permissões:**
- ✅ Leitura de conteúdos
- ✅ Interação com IA
- ❌ Criação de conteúdos
- ❌ Gerenciamento de usuários

### 2. TEACHER (Professor)
**Funcionalidades:**
- Todas as funcionalidades de STUDENT +
- Criação de conteúdos educacionais
- Gerenciamento de turmas
- Acompanhamento de estudantes
- Configuração de assistentes IA
- Análise de desempenho

**Permissões:**
- ✅ Leitura de conteúdos
- ✅ Interação com IA
- ✅ Criação/edição de conteúdos
- ✅ Gerenciamento de turmas
- ❌ Gerenciamento de usuários
- ❌ Configurações do sistema

### 3. ADMIN (Administrador)
**Funcionalidades:**
- Todas as funcionalidades de TEACHER +
- Gerenciamento completo de usuários
- Configurações do sistema
- Monitoramento de uso
- Acesso a logs e métricas
- Gestão de integrações (Gemini, OpenAI, Evolution API)

**Permissões:**
- ✅ Acesso total ao sistema
- ✅ Gerenciamento de todos os usuários
- ✅ Configurações do sistema
- ✅ Acesso a logs e métricas

---

## 🔄 Fluxos Funcionais

### Fluxo 1: Registro de Novo Usuário

```
┌──────────┐
│ Cliente  │
└────┬─────┘
     │
     │ POST /api/users/register
     │ {
     │   "name": "João Silva",
     │   "email": "joao@email.com",
     │   "password": "senha123",
     │   "cpf": "12345678900",
     │   "phone": "11999999999",
     │   "profile": "STUDENT"
     │ }
     ▼
┌─────────────────────┐
│  UserController     │
│  ┌───────────────┐  │
│  │ Valida DTO    │  │
│  │ @Valid        │  │
│  └───────┬───────┘  │
└──────────┼──────────┘
           │
           ▼
┌─────────────────────┐
│   UserService       │
│  ┌───────────────┐  │
│  │1. Verifica    │  │
│  │   email único │  │
│  └───────┬───────┘  │
│          │          │
│  ┌───────▼───────┐  │
│  │2. Criptografa │  │
│  │   senha       │  │
│  │   (BCrypt)    │  │
│  └───────┬───────┘  │
│          │          │
│  ┌───────▼───────┐  │
│  │3. Cria User   │  │
│  │   Entity      │  │
│  └───────┬───────┘  │
└──────────┼──────────┘
           │
           ▼
┌─────────────────────┐
│  UserRepository     │
│  ┌───────────────┐  │
│  │ Salva no DB   │  │
│  │ PostgreSQL    │  │
│  └───────┬───────┘  │
└──────────┼──────────┘
           │
           ▼
     201 CREATED
     {
       "id": "uuid-aqui",
       "name": "João Silva",
       "email": "joao@email.com",
       "profile": "STUDENT",
       "createdAt": "2025-11-25T10:00:00"
     }
```

**Validações no Registro:**
1. ✅ Email válido e único
2. ✅ Senha com mínimo 6 caracteres
3. ✅ CPF obrigatório
4. ✅ Telefone único
5. ✅ Perfil válido (STUDENT/TEACHER/ADMIN)
6. ✅ Todos os campos obrigatórios preenchidos

**Tratamento de Erros:**
- Email já existe → `400 BAD REQUEST`
- Dados inválidos → `400 BAD REQUEST`
- Erro no servidor → `500 INTERNAL SERVER ERROR`

---

### Fluxo 2: Autenticação JWT

```
┌──────────┐
│ Cliente  │
└────┬─────┘
     │
     │ POST /api/auth/login
     │ {
     │   "email": "joao@email.com",
     │   "password": "senha123"
     │ }
     ▼
┌──────────────────────┐
│  AuthController      │
│  (não implementado)  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  AuthService         │
│  ┌────────────────┐  │
│  │1. Busca user   │  │
│  │   por email    │  │
│  └────────┬───────┘  │
│           │          │
│  ┌────────▼───────┐  │
│  │2. Valida       │  │
│  │   senha        │  │
│  │   (BCrypt)     │  │
│  └────────┬───────┘  │
└───────────┼──────────┘
            │
            ▼
┌───────────────────────┐
│      JwtUtil          │
│  ┌─────────────────┐  │
│  │ Gera Token JWT  │  │
│  │                 │  │
│  │ Claims:         │  │
│  │ - userId        │  │
│  │ - email         │  │
│  │ - profile       │  │
│  │ - name          │  │
│  │                 │  │
│  │ Expira em:      │  │
│  │ 1 hora          │  │
│  └────────┬────────┘  │
└───────────┼───────────┘
            │
            ▼
      200 OK
      {
        "token": "eyJhbGci...",
        "type": "Bearer",
        "expiresIn": 3600000,
        "user": {
          "id": "uuid",
          "name": "João Silva",
          "email": "joao@email.com",
          "profile": "STUDENT"
        }
      }
```

---

### Fluxo 3: Validação de Token JWT

```
┌──────────┐
│ Cliente  │
└────┬─────┘
     │
     │ GET /api/recurso-protegido
     │ Header: Authorization: Bearer eyJhbGci...
     ▼
┌─────────────────────────┐
│  Security Filter        │
│  ┌───────────────────┐  │
│  │1. Extrai token    │  │
│  │   do header       │  │
│  └─────────┬─────────┘  │
└────────────┼────────────┘
             │
             ▼
┌─────────────────────────┐
│       JwtUtil           │
│  ┌───────────────────┐  │
│  │ Valida Token:     │  │
│  │                   │  │
│  │ ✓ Assinatura      │  │
│  │ ✓ Expiração       │  │
│  │ ✓ Estrutura       │  │
│  └─────────┬─────────┘  │
│            │             │
│  ┌─────────▼─────────┐  │
│  │ Extrai Claims:    │  │
│  │                   │  │
│  │ - userId          │  │
│  │ - email           │  │
│  │ - profile         │  │
│  │ - name            │  │
│  └─────────┬─────────┘  │
└────────────┼────────────┘
             │
             ▼
       ┌─────────┐
       │ Válido? │
       └────┬────┘
            │
      ┌─────┴─────┐
      │           │
     SIM         NÃO
      │           │
      ▼           ▼
  Processa    401 UNAUTHORIZED
  Request     {
              "error": "Token inválido"
              }
```

---

## 🔐 Autenticação e Autorização

### Token JWT - Estrutura

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "usuario@email.com",
    "profile": "STUDENT",
    "name": "Nome do Usuário",
    "sub": "usuario@email.com",
    "iat": 1700000000,
    "exp": 1700003600
  },
  "signature": "assinatura-criptografada"
}
```

### Informações Contidas no Token

| Campo | Descrição | Uso |
|-------|-----------|-----|
| `userId` | UUID único do usuário | Identificação |
| `email` | Email do usuário | Validação |
| `profile` | Perfil do usuário | Autorização |
| `name` | Nome do usuário | Exibição |
| `sub` | Subject (email) | Padrão JWT |
| `iat` | Issued At | Auditoria |
| `exp` | Expiration | Segurança |

### Tempo de Expiração
- **Padrão**: 1 hora (3.600.000 ms)
- **Configurável**: `jwt.expiration` em application.properties
- **Refresh**: Não implementado ainda

---

## 🛡️ Segurança

### 1. Criptografia de Senha
```
┌────────────────────────────────────────┐
│     Criptografia BCrypt                │
├────────────────────────────────────────┤
│                                        │
│  Senha Original: "senha123"            │
│           │                            │
│           ▼                            │
│  BCryptPasswordEncoder                 │
│  (strength = 12)                       │
│           │                            │
│           ▼                            │
│  Hash: "$2a$12$N0IA..."                │
│                                        │
│  ✓ Salt aleatório                      │
│  ✓ 12 rounds de hash                   │
│  ✓ Irreversível                        │
│  ✓ Resistente a rainbow tables         │
└────────────────────────────────────────┘
```

**Características:**
- Algoritmo: BCrypt
- Strength: 12 rounds
- Salt: Gerado automaticamente
- Irreversível: Não pode ser descriptografado

### 2. Validação JWT

**Exceções Tratadas:**
```
┌─────────────────────────────────────────┐
│     Validações do Token JWT             │
├─────────────────────────────────────────┤
│                                         │
│  ❌ SignatureException                  │
│     → Assinatura inválida               │
│                                         │
│  ❌ MalformedJwtException               │
│     → Token malformado                  │
│                                         │
│  ❌ ExpiredJwtException                 │
│     → Token expirado                    │
│                                         │
│  ❌ UnsupportedJwtException             │
│     → Token não suportado               │
│                                         │
│  ❌ IllegalArgumentException            │
│     → Claims vazias                     │
└─────────────────────────────────────────┘
```

### 3. Constraints no Banco de Dados

```sql
-- Constraints de Unicidade
ALTER TABLE users ADD CONSTRAINT uk_email UNIQUE (email);
ALTER TABLE users ADD CONSTRAINT uk_phone UNIQUE (phone);

-- Constraints Not Null
ALTER TABLE users ALTER COLUMN name SET NOT NULL;
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN password SET NOT NULL;
ALTER TABLE users ALTER COLUMN profile SET NOT NULL;
ALTER TABLE users ALTER COLUMN cpf_string SET NOT NULL;
ALTER TABLE users ALTER COLUMN phone SET NOT NULL;
```

---

## 📜 Regras de Negócio

### RN001 - Unicidade de Email
- **Regra**: Cada email deve ser único no sistema
- **Validação**: Verificada antes de criar usuário
- **Exceção**: `EmailAlreadyExistsException`
- **Código HTTP**: 400 BAD REQUEST

### RN002 - Unicidade de Telefone
- **Regra**: Cada telefone deve ser único no sistema
- **Validação**: Constraint no banco de dados
- **Exceção**: Database constraint violation
- **Código HTTP**: 400 BAD REQUEST

### RN003 - Complexidade de Senha
- **Regra**: Senha deve ter no mínimo 6 caracteres
- **Validação**: `@Size(min = 6)` no DTO
- **Recomendação**: Aumentar para 8+ caracteres em produção

### RN004 - Formato de Email
- **Regra**: Email deve ser válido
- **Validação**: `@Email` annotation
- **Exemplo válido**: usuario@dominio.com

### RN005 - Perfil Obrigatório
- **Regra**: Todo usuário deve ter um perfil definido
- **Valores**: STUDENT, TEACHER, ADMIN
- **Validação**: `@NotNull` no DTO

### RN006 - Dados Obrigatórios
- **Regra**: Todos os campos são obrigatórios no registro
- **Campos**: name, email, password, cpf, phone, profile
- **Validação**: `@NotBlank` annotations

### RN007 - Expiração de Token
- **Regra**: Token JWT expira após 1 hora
- **Validação**: Verificada em cada requisição
- **Ação**: Usuário deve fazer login novamente

### RN008 - Auditoria de Usuário
- **Regra**: Sistema registra criação e atualização
- **Campos**: created_at, updated_at
- **Tipo**: Automático via @CreationTimestamp e @UpdateTimestamp

---

## 🔄 Diagrama de Estados do Usuário

```
┌─────────────────────────────────────────────────────────┐
│                 Ciclo de Vida do Usuário                 │
└─────────────────────────────────────────────────────────┘

                    ┌─────────────┐
                    │   INÍCIO    │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  REGISTRO   │
                    │             │
                    │ POST /users │
                    │  /register  │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   CRIADO    │
                    │             │
                    │ Usuário     │
                    │ ativo no BD │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
       ┌──────────┐ ┌──────────┐ ┌──────────┐
       │  LOGIN   │ │  LOGOUT  │ │ ATUALIZA │
       │          │ │          │ │          │
       │Gera JWT  │ │Token     │ │  Dados   │
       │          │ │Descartado│ │          │
       └─────┬────┘ └────┬─────┘ └────┬─────┘
             │           │            │
             │           │            │
             └───────────┴────────────┘
                        │
                        ▼
                  ┌──────────┐
                  │  ATIVO   │
                  │          │
                  │Operações │
                  │do Sistema│
                  └──────────┘
```

---

## 📊 Matriz de Permissões

| Funcionalidade | STUDENT | TEACHER | ADMIN |
|----------------|---------|---------|-------|
| Login/Logout | ✅ | ✅ | ✅ |
| Ver próprio perfil | ✅ | ✅ | ✅ |
| Atualizar próprio perfil | ✅ | ✅ | ✅ |
| Acessar conteúdos | ✅ | ✅ | ✅ |
| Interagir com IA | ✅ | ✅ | ✅ |
| Criar conteúdos | ❌ | ✅ | ✅ |
| Gerenciar turmas | ❌ | ✅ | ✅ |
| Ver outros usuários | ❌ | Parcial* | ✅ |
| Criar usuários | ❌ | ❌ | ✅ |
| Editar outros usuários | ❌ | ❌ | ✅ |
| Excluir usuários | ❌ | ❌ | ✅ |
| Configurações sistema | ❌ | ❌ | ✅ |
| Logs e métricas | ❌ | ❌ | ✅ |

\* Teachers podem ver apenas estudantes das suas turmas

---

## 🏗️ Arquitetura em Camadas

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│  ┌─────────────────────────────────────────────────┐    │
│  │        UserController / AuthController           │    │
│  │  - Recebe requisições HTTP                       │    │
│  │  - Valida DTOs                                   │    │
│  │  - Retorna ResponseEntity                        │    │
│  └─────────────────────────────────────────────────┘    │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                     BUSINESS LAYER                       │
│  ┌─────────────────────────────────────────────────┐    │
│  │           UserService / AuthService              │    │
│  │  - Lógica de negócio                             │    │
│  │  - Validações                                    │    │
│  │  - Orquestração                                  │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │                  JwtUtil                         │    │
│  │  - Geração de tokens                             │    │
│  │  - Validação JWT                                 │    │
│  │  - Extração de claims                            │    │
│  └─────────────────────────────────────────────────┘    │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                   PERSISTENCE LAYER                      │
│  ┌─────────────────────────────────────────────────┐    │
│  │              UserRepository                      │    │
│  │  - CRUD operations                               │    │
│  │  - Queries customizadas                          │    │
│  │  - Spring Data JPA                               │    │
│  └─────────────────────────────────────────────────┘    │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                     DATABASE LAYER                       │
│  ┌─────────────────────────────────────────────────┐    │
│  │              PostgreSQL Database                 │    │
│  │  - Tabela users                                  │    │
│  │  - Constraints                                   │    │
│  │  - Índices                                       │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 Exemplo de Uso Completo

### 1. Registro de Novo Estudante
```bash
# Request
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Santos",
    "email": "maria@email.com",
    "password": "senha123",
    "cpf": "12345678900",
    "phone": "11987654321",
    "profile": "STUDENT"
  }'

# Response (201 CREATED)
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Maria Santos",
  "email": "maria@email.com",
  "cpfString": "12345678900",
  "profile": "STUDENT",
  "phone": "11987654321",
  "createdAt": "2025-11-25T10:30:00",
  "updatedAt": "2025-11-25T10:30:00"
}
```

### 2. Login (Autenticação)
```bash
# Request
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria@email.com",
    "password": "senha123"
  }'

# Response (200 OK)
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600000,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Maria Santos",
    "email": "maria@email.com",
    "profile": "STUDENT"
  }
}
```

### 3. Acesso a Recurso Protegido
```bash
# Request
curl -X GET http://localhost:8080/api/conteudos \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Response (200 OK)
[
  {
    "id": "123",
    "titulo": "Introdução ao Java",
    "descricao": "Curso básico de Java",
    ...
  }
]
```

---

## 🎨 Considerações de Design

### Princípios Aplicados
1. **Single Responsibility**: Cada classe tem uma responsabilidade única
2. **Separation of Concerns**: Camadas bem definidas (Controller, Service, Repository)
3. **Dependency Injection**: Uso de @RequiredArgsConstructor e injeção via construtor
4. **DTO Pattern**: Separação entre entidades de domínio e objetos de transferência
5. **Exception Handling**: Tratamento centralizado de exceções

### Padrões de Código
- ✅ Lombok para redução de boilerplate
- ✅ Builder pattern para criação de objetos
- ✅ Bean Validation para validações declarativas
- ✅ Transações declarativas com @Transactional
- ✅ Logging estruturado com SLF4J

---

## 🚀 Melhorias Futuras

### Curto Prazo
- [ ] Implementar AuthController completo
- [ ] Adicionar endpoint de atualização de perfil
- [ ] Implementar refresh token
- [ ] Adicionar endpoint de recuperação de senha

### Médio Prazo
- [ ] Implementar autorização por perfil (annotations)
- [ ] Adicionar auditoria completa de ações
- [ ] Implementar soft delete de usuários
- [ ] Cache de usuários com Redis

### Longo Prazo
- [ ] Implementar 2FA (Two-Factor Authentication)
- [ ] Histórico de alterações de perfil
- [ ] Sistema de permissões granulares
- [ ] Integração com OAuth2/OpenID Connect

---

## 📚 Referências

- **Localização dos Arquivos**:
  - Model: `src/main/java/com/langia/backend/model/User.java`
  - Profile: `src/main/java/com/langia/backend/model/UserProfile.java`
  - Controller: `src/main/java/com/langia/backend/controller/UserController.java`
  - Service: `src/main/java/com/langia/backend/service/UserService.java`
  - JWT Util: `src/main/java/com/langia/backend/util/JwtUtil.java`
  - DTO: `src/main/java/com/langia/backend/dto/UserRegistrationDTO.java`

- **Tecnologias**:
  - Spring Boot 3.x
  - PostgreSQL 15
  - JWT (JJWT 0.12.3)
  - BCrypt
  - Lombok

---

**Documento gerado em**: 25/11/2025
**Versão**: 1.0
**Autor**: LangIA Development Team
