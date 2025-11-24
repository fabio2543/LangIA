# LangIA API - Postman Collection

Esta collection do Postman contém todos os endpoints da API LangIA para testar o sistema de autenticação JWT com Redis.

## Configuração

### Variáveis de Ambiente

A collection utiliza as seguintes variáveis:

- `baseUrl`: URL base da API (padrão: `http://localhost:8080`)
- `authToken`: Token JWT obtido após login (preenchido automaticamente)
- `userId`: ID do usuário autenticado (preenchido automaticamente)
- `userProfile`: Perfil do usuário autenticado (preenchido automaticamente)
- `testEmail`: Email para testes (padrão: `joao.silva@example.com`)
- `testPassword`: Senha para testes (padrão: `senha123456`)

### Como Usar

1. Importe a collection `LangIA_API_Collection.json` no Postman
2. Configure a variável `baseUrl` se necessário (padrão: `http://localhost:8080`)
3. Execute os requests na ordem:
   - Primeiro registre um usuário (Register User)
   - Depois faça login para obter o token
   - Use o token para acessar endpoints protegidos

## Endpoints Incluídos

### Health Check
- **GET** `/actuator/health` - Verifica o status da aplicação

### Authentication
- **POST** `/api/auth/login` - Autenticação de usuário
- **POST** `/api/auth/login` (Invalid Credentials) - Teste com credenciais inválidas
- **POST** `/api/auth/login` (Validation Error) - Teste de validação

### Users
- **POST** `/api/users/register` - Registro de usuário (STUDENT, TEACHER, ADMIN)
- **POST** `/api/users/register` (Duplicate Email) - Teste de email duplicado
- **POST** `/api/users/register` (Duplicate CPF) - Teste de CPF duplicado
- **POST** `/api/users/register` (Duplicate Phone) - Teste de telefone duplicado
- **POST** `/api/users/register` (Validation Error) - Teste de validação

### Protected Endpoints
- **GET** `/api/protected` (Without Token) - Teste sem autenticação
- **GET** `/api/protected` (With Token) - Teste com token válido
- **GET** `/api/protected` (Invalid Token) - Teste com token inválido
- **GET** `/api/protected` (Expired Token) - Teste com token expirado

## Fluxo de Teste Recomendado

1. **Registrar Usuário**: Execute "Register User - Student" para criar um novo usuário
2. **Login**: Execute "Login" para obter o token JWT (o token será salvo automaticamente na variável `authToken`)
3. **Acessar Endpoint Protegido**: Execute "Access Protected Endpoint - With Token" para testar autenticação
4. **Testar Erros**: Execute os testes de erro para validar tratamento de exceções

## Observações

- O token JWT é automaticamente salvo na variável `authToken` após login bem-sucedido
- Todos os endpoints protegidos requerem o header `Authorization: Bearer {token}`
- Os endpoints públicos são: `/api/users/register`, `/api/auth/login`, `/actuator/health`

