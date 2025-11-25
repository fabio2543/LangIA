# Guia de Uso - Postman Collection LangIA

Este guia explica como configurar e usar a collection do Postman para testar a API de autenticação do LangIA.

## 📦 Arquivos da Collection

- **`LangIA_Authentication.postman_collection.json`** - Collection principal com todos os endpoints
- **`LangIA_Local.postman_environment.json`** - Ambiente pré-configurado para desenvolvimento local

## 🚀 Configuração Inicial

### 1. Importar Collection no Postman

1. Abra o Postman
2. Clique em **Import** (canto superior esquerdo)
3. Selecione os arquivos:
   - `LangIA_Authentication.postman_collection.json`
   - `LangIA_Local.postman_environment.json`
4. Clique em **Import**

### 2. Selecionar Ambiente

1. No canto superior direito, clique no dropdown de ambientes
2. Selecione **"LangIA - Local Development"**
3. Certifique-se de que a aplicação está rodando em `http://localhost:8080`

### 3. Configurar Variável Base URL (se necessário)

Se sua aplicação roda em outra porta ou host:

1. Clique no ícone de olho (👁️) ao lado do ambiente selecionado
2. Clique em **Edit**
3. Altere o valor de `baseUrl` (ex: `http://localhost:8082`)
4. Clique em **Save**

## 📋 Estrutura da Collection

A collection está organizada em 6 pastas:

### 1️⃣ User Registration
Endpoints para cadastro de novos usuários.

- **Register New Student** - Cadastra usuário com perfil STUDENT
- **Register New Teacher** - Cadastra usuário com perfil TEACHER

### 2️⃣ Authentication
Endpoints de login e autenticação.

- **Login - Student** - Login com credenciais de estudante
- **Login - Teacher** - Login com credenciais de professor
- **Login - Invalid Credentials** - Teste de login com senha incorreta
- **Login - Missing Fields** - Teste de validação de campos obrigatórios

### 3️⃣ Session Management
Endpoints para gerenciamento de sessões.

- **Validate Session - Valid Token** - Valida sessão ativa
- **Validate Session - No Token** - Teste sem token
- **Validate Session - Invalid Token** - Teste com token inválido
- **Renew Session - Valid Token** - Renova TTL da sessão
- **Renew Session - Invalid Token** - Teste de renovação inválida

### 4️⃣ Logout
Endpoints para encerrar sessão.

- **Logout - Valid Session** - Logout com sucesso
- **Logout - No Token** - Teste sem token
- **Logout - Invalid Token** - Teste com token inválido

### 5️⃣ Protected Endpoints Examples
Exemplos de acesso a rotas protegidas.

- **Access Protected Endpoint - With Token** - Acesso autorizado
- **Access Protected Endpoint - Without Token** - Acesso bloqueado

### 6️⃣ Health Check
Endpoints públicos de monitoramento.

- **Health Check** - Verifica se a aplicação está no ar

## 🎯 Fluxo de Uso Recomendado

### Cenário 1: Primeiro Acesso (Novo Usuário)

```
1. User Registration → Register New Student
   ↓ (salva automaticamente o email)
2. Authentication → Login - Student
   ↓ (salva automaticamente o token e userId)
3. Session Management → Validate Session - Valid Token
   ↓ (confirma que está autenticado)
4. Protected Endpoints Examples → Access Protected Endpoint - With Token
   ↓ (testa acesso a rota protegida)
5. Logout → Logout - Valid Session
   ✓ (encerra a sessão)
```

### Cenário 2: Usuário Existente

```
1. Authentication → Login - Student
   ↓ (salva automaticamente o token)
2. Session Management → Validate Session - Valid Token
   ↓ (confirma autenticação)
3. [Use a aplicação normalmente]
4. Session Management → Renew Session - Valid Token
   ↓ (renova sessão antes de expirar)
5. Logout → Logout - Valid Session
   ✓ (encerra quando terminar)
```

### Cenário 3: Testes de Segurança

```
1. Authentication → Login - Invalid Credentials
   ✓ (deve retornar 401)
2. Authentication → Login - Missing Fields
   ✓ (deve retornar 400)
3. Session Management → Validate Session - No Token
   ✓ (deve retornar 401)
4. Protected Endpoints Examples → Access Protected Endpoint - Without Token
   ✓ (deve retornar 401 ou 403)
```

## 🔧 Recursos Automáticos da Collection

### 1. Salvamento Automático de Token

Após executar o endpoint **Login - Student** ou **Login - Teacher**, o token JWT é automaticamente salvo na variável `{{token}}` do ambiente.

Você pode ver isso nos **Tests** do request:

```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    pm.environment.set("userId", jsonData.userId);
}
```

### 2. Uso Automático de Token

Todos os endpoints que requerem autenticação já estão configurados para usar a variável `{{token}}` automaticamente através da configuração **Bearer Token** na aba **Authorization**.

### 3. Testes Automatizados

Cada request possui testes automatizados que validam:

- ✅ Status code correto
- ✅ Estrutura da resposta JSON
- ✅ Presença de campos obrigatórios
- ✅ Valores esperados

Para ver os resultados, execute um request e verifique a aba **Test Results** na parte inferior.

### 4. Limpeza Automática após Logout

O endpoint **Logout - Valid Session** remove automaticamente o token das variáveis de ambiente:

```javascript
pm.environment.unset("token");
pm.environment.unset("userId");
```

## 📊 Executando Testes em Lote

### Collection Runner

Para executar todos os testes de uma vez:

1. Clique com botão direito na collection **"LangIA - Authentication API"**
2. Selecione **Run collection**
3. Configure as opções:
   - **Iterations**: 1 (quantas vezes executar)
   - **Delay**: 100ms (delay entre requests)
4. Clique em **Run LangIA - Authentication API**

**Importante:** Alguns testes falharão se executados em sequência (ex: login com credenciais inválidas). Para testes completos, execute as pastas individualmente.

### Executar Pasta Específica

1. Clique com botão direito na pasta desejada (ex: "2. Authentication")
2. Selecione **Run folder**
3. Clique em **Run**

## 🔍 Entendendo as Variáveis

### Variáveis de Ambiente

| Variável | Descrição | Quando é Preenchida |
|----------|-----------|---------------------|
| `baseUrl` | URL base da API | Configuração manual inicial |
| `token` | Token JWT de autenticação | Automaticamente após login |
| `userId` | ID do usuário logado | Automaticamente após login |
| `userEmail` | Email do usuário cadastrado | Automaticamente após registro |

### Visualizar Variáveis

1. Clique no ícone de olho (👁️) ao lado do ambiente
2. Veja os valores atuais de cada variável
3. Para limpar manualmente: clique em **Edit** e delete os valores

## 📝 Exemplos de Uso

### Exemplo 1: Cadastro e Login

```
POST http://localhost:8080/api/users/register
{
  "name": "João Silva",
  "email": "joao.silva@example.com",
  "password": "senha123",
  "profile": "STUDENT",
  "cpf": "11144477735",
  "phone": "11987654321"
}
```

**Resposta esperada (201):**
```json
{
  "id": "uuid",
  "name": "João Silva",
  "email": "joao.silva@example.com",
  "profile": "STUDENT",
  "cpf": "111.444.777-35",
  "phone": "(11) 98765-4321"
}
```

Agora execute o login:

```
POST http://localhost:8080/api/auth/login
{
  "email": "joao.silva@example.com",
  "password": "senha123"
}
```

**Resposta esperada (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "uuid",
  "name": "João Silva",
  "email": "joao.silva@example.com",
  "profile": "STUDENT",
  "permissions": ["view_courses", "view_lessons", "submit_exercises"],
  "expiresIn": 3600000
}
```

### Exemplo 2: Validar e Renovar Sessão

Com o token obtido no login, valide a sessão:

```
GET http://localhost:8080/api/auth/validate
Authorization: Bearer {{token}}
```

**Resposta esperada (200):**
```json
{
  "valid": true,
  "sessionData": {
    "userId": "uuid",
    "name": "João Silva",
    "email": "joao.silva@example.com",
    "profile": "STUDENT",
    "permissions": ["view_courses", "view_lessons", "submit_exercises"]
  }
}
```

Para renovar a sessão (estender por mais 1 hora):

```
POST http://localhost:8080/api/auth/renew
Authorization: Bearer {{token}}
```

**Resposta esperada (200):**
```json
{
  "message": "Session renewed successfully"
}
```

### Exemplo 3: Logout

Para encerrar a sessão:

```
POST http://localhost:8080/api/auth/logout
Authorization: Bearer {{token}}
```

**Resposta esperada (204 No Content)**

Após o logout, qualquer tentativa de validar a sessão retornará 401:

```
GET http://localhost:8080/api/auth/validate
Authorization: Bearer {{token}}
```

**Resposta esperada (401):**
```json
{
  "valid": false,
  "sessionData": null
}
```

## 🔐 Perfis e Permissões

### Perfis Disponíveis

| Perfil | Descrição | Permissões Típicas |
|--------|-----------|-------------------|
| `STUDENT` | Estudante | view_courses, view_lessons, submit_exercises |
| `TEACHER` | Professor | Permissões de STUDENT + create_courses, grade_exercises |
| `ADMIN` | Administrador | Todas as permissões + manage_users, manage_settings |

### Testar Diferentes Perfis

1. Cadastre usuários com perfis diferentes usando a pasta **"1. User Registration"**
2. Faça login com cada um usando a pasta **"2. Authentication"**
3. Compare as permissões retornadas no campo `permissions` da resposta

## ⚠️ Troubleshooting

### Problema: "Could not get response"

**Causa:** Aplicação não está rodando ou porta incorreta.

**Solução:**
1. Verifique se a aplicação está rodando: `./mvnw spring-boot:run`
2. Confirme a porta no console (geralmente 8080)
3. Atualize `baseUrl` no ambiente se necessário

### Problema: "401 Unauthorized" em todos os requests

**Causa:** Token expirado ou inválido.

**Solução:**
1. Execute novamente **Login - Student** para obter novo token
2. Verifique se a variável `{{token}}` foi salva (ícone 👁️)
3. Certifique-se de que o ambiente correto está selecionado

### Problema: "Session not found" após login bem-sucedido

**Causa:** Redis não está rodando.

**Solução:**
1. Inicie o Redis: `docker-compose up -d redis`
2. Verifique conexão: `redis-cli ping` (deve retornar "PONG")
3. Faça login novamente

### Problema: Tests falhando

**Causa:** Estrutura da resposta diferente do esperado.

**Solução:**
1. Verifique a resposta real na aba **Body**
2. Compare com a validação na aba **Tests**
3. Ajuste os testes se necessário ou reporte o bug

## 📚 Documentação Relacionada

- [SECURITY_CONFIGURATION.md](./SECURITY_CONFIGURATION.md) - Documentação técnica completa
- [AUTHENTICATION_GUIDE.md](./AUTHENTICATION_GUIDE.md) - Guia de integração frontend
- [ARCHITECTURE_DIAGRAM.md](./ARCHITECTURE_DIAGRAM.md) - Diagramas de arquitetura

## 🎓 Dicas Avançadas

### 1. Variáveis Dinâmicas

Você pode usar JavaScript nos testes para criar variáveis dinâmicas:

```javascript
// Gerar email aleatório
pm.environment.set("randomEmail", `user${Date.now()}@example.com`);

// Gerar CPF válido (implementar lógica de geração)
pm.environment.set("randomCPF", "11144477735");
```

### 2. Testes Customizados

Adicione testes personalizados na aba **Tests**:

```javascript
pm.test("Token has correct format", function () {
    var jsonData = pm.response.json();
    var tokenParts = jsonData.token.split('.');
    pm.expect(tokenParts.length).to.eql(3); // JWT tem 3 partes
});

pm.test("Permissions include view_courses", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.permissions).to.include("view_courses");
});
```

### 3. Pre-request Scripts

Use scripts antes dos requests:

```javascript
// Calcular tempo de expiração
const expiresAt = Date.now() + 3600000; // 1 hora
pm.environment.set("tokenExpiresAt", expiresAt);

// Log de debug
console.log("Current token:", pm.environment.get("token"));
```

### 4. Monitor de Sessão

Crie um monitor no Postman para verificar periodicamente se a API está no ar:

1. Collection → ... → Monitor Collection
2. Configure schedule (ex: cada 5 minutos)
3. Monitore apenas o **Health Check** endpoint
4. Receba alertas se a API cair

## 🤝 Contribuindo

Ao adicionar novos endpoints na API:

1. Adicione-os na collection apropriada
2. Configure testes automatizados
3. Atualize este guia se necessário
4. Exporte e commite os arquivos JSON atualizados

---

**Desenvolvido para o projeto LangIA**
**Versão:** 1.0.0
**Última atualização:** 2025-11-25
