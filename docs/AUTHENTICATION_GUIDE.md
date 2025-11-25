# Guia de Autenticação - LangIA

Guia rápido para desenvolvedores sobre como usar o sistema de autenticação do LangIA.

## 🚀 Início Rápido

### 1. Fazer Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "senha123"
  }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Nome do Usuário",
  "email": "usuario@example.com",
  "profile": "STUDENT",
  "permissions": ["view_courses", "view_lessons", ...],
  "expiresIn": 3600000
}
```

### 2. Usar o Token

Inclua o token no header `Authorization` de todas as requisições:

```bash
curl http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. Fazer Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 📡 Endpoints de Autenticação

### POST /api/auth/login

Autentica um usuário e retorna um token JWT.

**Request:**
```json
{
  "email": "string (required, valid email)",
  "password": "string (required)"
}
```

**Response 200:**
```json
{
  "token": "string",
  "userId": "uuid",
  "name": "string",
  "email": "string",
  "profile": "STUDENT | TEACHER | ADMIN",
  "permissions": ["string"],
  "expiresIn": "number (ms)"
}
```

**Response 401:**
```json
{
  "message": "Invalid credentials"
}
```

---

### POST /api/auth/logout

Encerra a sessão do usuário, invalidando o token.

**Headers:**
```
Authorization: Bearer <token>
```

**Response 204:** Sem corpo

**Response 401:**
```json
{
  "message": "Session not found or already expired"
}
```

---

### GET /api/auth/validate

Valida se um token ainda é válido e retorna dados da sessão.

**Headers:**
```
Authorization: Bearer <token>
```

**Response 200:**
```json
{
  "valid": true,
  "session": {
    "userId": "uuid",
    "name": "string",
    "email": "string",
    "profile": "string",
    "permissions": ["string"],
    "createdAt": "number"
  }
}
```

**Response 401:**
```json
{
  "valid": false,
  "session": null
}
```

---

### POST /api/auth/renew

Renova o tempo de expiração de uma sessão ativa.

**Headers:**
```
Authorization: Bearer <token>
```

**Response 200:**
```json
{
  "message": "Session renewed successfully"
}
```

**Response 401:**
```json
{
  "message": "Invalid token or session not found"
}
```

---

## 🔑 Formato do Token

### Header Authorization

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Importante:**
- Prefixo `Bearer` com espaço
- Case-sensitive
- Token sem espaços extras

### Estrutura do JWT

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "usuario@example.com",
  "profile": "STUDENT",
  "name": "Nome do Usuário",
  "sub": "usuario@example.com",
  "iat": 1700000000,
  "exp": 1700003600
}
```

---

## 👥 Perfis e Permissões

### STUDENT

**Permissões:**
- `view_courses` - Visualizar cursos
- `view_lessons` - Visualizar lições
- `submit_exercises` - Enviar exercícios
- `view_progress` - Visualizar progresso
- `chat_with_ai` - Conversar com IA
- `view_profile` - Visualizar perfil
- `update_profile` - Atualizar perfil

### TEACHER

**Permissões:** Todas do STUDENT, mais:
- `create_courses` - Criar cursos
- `edit_courses` - Editar cursos
- `delete_courses` - Excluir cursos
- `create_lessons` - Criar lições
- `edit_lessons` - Editar lições
- `delete_lessons` - Excluir lições
- `view_students` - Visualizar alunos
- `view_student_progress` - Ver progresso dos alunos
- `grade_exercises` - Avaliar exercícios
- `manage_class` - Gerenciar turma

### ADMIN

**Permissões:** Todas do TEACHER, mais:
- `view_teachers` - Visualizar professores
- `manage_users` - Gerenciar usuários
- `create_users` - Criar usuários
- `edit_users` - Editar usuários
- `delete_users` - Excluir usuários
- `view_system_stats` - Ver estatísticas
- `manage_settings` - Gerenciar configurações
- `manage_integrations` - Gerenciar integrações

---

## 🛠️ Integração com Frontend

### React/JavaScript

```javascript
// Login
const login = async (email, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    throw new Error('Login failed');
  }

  const data = await response.json();

  // Salvar token no localStorage
  localStorage.setItem('token', data.token);
  localStorage.setItem('user', JSON.stringify(data));

  return data;
};

// Requisição autenticada
const fetchProtectedData = async () => {
  const token = localStorage.getItem('token');

  const response = await fetch('http://localhost:8080/api/protected', {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  return response.json();
};

// Logout
const logout = async () => {
  const token = localStorage.getItem('token');

  await fetch('http://localhost:8080/api/auth/logout', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  localStorage.removeItem('token');
  localStorage.removeItem('user');
};
```

### Axios Interceptor

```javascript
import axios from 'axios';

// Configurar base URL
axios.defaults.baseURL = 'http://localhost:8080';

// Interceptor para adicionar token
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para tratar 401
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expirado ou inválido
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

## ⏱️ Gerenciamento de Expiração

### Expiração do Token

- **Duração:** 1 hora (3600000 ms)
- **Campo:** `expiresIn` na resposta de login
- **Sincronização:** JWT e Redis expiram juntos

### Estratégias de Renovação

#### 1. Renovação Manual

```javascript
const renewSession = async () => {
  const token = localStorage.getItem('token');

  const response = await fetch('http://localhost:8080/api/auth/renew', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (response.ok) {
    console.log('Sessão renovada');
  }
};

// Renovar a cada 30 minutos
setInterval(renewSession, 30 * 60 * 1000);
```

#### 2. Renovação Automática em Requisições

```javascript
axios.interceptors.response.use(
  async (response) => {
    // Renovar se faltar menos de 10 minutos
    const tokenData = JSON.parse(localStorage.getItem('user'));
    const expiresAt = tokenData.createdAt + tokenData.expiresIn;
    const timeLeft = expiresAt - Date.now();

    if (timeLeft < 10 * 60 * 1000) { // 10 minutos
      await renewSession();
    }

    return response;
  }
);
```

---

## 🔒 Boas Práticas de Segurança

### ✅ Frontend

1. **Armazenamento Seguro**
   ```javascript
   // ✅ Bom: localStorage para SPAs
   localStorage.setItem('token', token);

   // ❌ Evitar: cookies sem httpOnly em SPAs
   document.cookie = `token=${token}`;
   ```

2. **Limpeza de Token**
   ```javascript
   // Sempre limpar ao fazer logout
   const logout = () => {
     localStorage.removeItem('token');
     localStorage.removeItem('user');
     delete axios.defaults.headers.common['Authorization'];
   };
   ```

3. **Validação Antes de Enviar**
   ```javascript
   const makeRequest = async () => {
     const token = localStorage.getItem('token');
     if (!token) {
       window.location.href = '/login';
       return;
     }
     // Fazer requisição...
   };
   ```

### ✅ Geral

1. **Sempre use HTTPS em produção**
2. **Não exponha tokens em logs**
3. **Implemente timeout de inatividade**
4. **Valide entrada do usuário**
5. **Monitore tentativas de login falhas**

---

## 🐛 Troubleshooting

### Erro 401: Invalid credentials

**Causa:** Email ou senha incorretos

**Solução:** Verificar credenciais do usuário

---

### Erro 401: Missing or invalid Authorization header

**Causa:** Token ausente ou formato incorreto

**Solução:**
```javascript
// ✅ Correto
headers: {
  'Authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
}

// ❌ Incorreto
headers: {
  'Authorization': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' // Falta "Bearer "
}
```

---

### Erro 401: Session not found or already expired

**Causa:** Token expirado ou sessão removida (logout)

**Solução:** Fazer login novamente

---

### CORS Error

**Causa:** Backend não configurado para aceitar requisições do frontend

**Solução:** Adicionar configuração CORS no backend

---

## 📝 Exemplos Completos

### React Login Component

```jsx
import { useState } from 'react';
import axios from 'axios';

function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await axios.post('/api/auth/login', {
        email,
        password,
      });

      // Salvar token
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data));

      // Configurar axios
      axios.defaults.headers.common['Authorization'] =
        `Bearer ${response.data.token}`;

      // Redirecionar
      window.location.href = '/dashboard';

    } catch (err) {
      setError('Email ou senha inválidos');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Senha"
        required
      />
      {error && <p className="error">{error}</p>}
      <button type="submit">Entrar</button>
    </form>
  );
}
```

---

## 🔗 Links Úteis

- [Documentação Completa de Segurança](./SECURITY_CONFIGURATION.md)
- [API Reference](./API_REFERENCE.md)
- [Postman Collection](./postman/LangIA.postman_collection.json)

---

**Última atualização:** 25 de Novembro de 2025
