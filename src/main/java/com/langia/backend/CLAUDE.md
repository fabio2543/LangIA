
## Stack
- Spring Boot 3.x (Java 17+)
- PostgreSQL 15
- Redis 7
- RabbitMQ 3
- Docker
- pgvector

## Arquitetura


3 microserviços independentes:

**1. Auth Service (8082)** - Autenticação/JWT
**2. Core Service (8083)** - Lógica de negócio
**3. AI Service (8084)** - Integração IA/WhatsApp

## Estrutura

```
services/
├── auth/
│   └── src/main/java/com/langia/auth/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       ├── dto/
│       ├── config/
│       └── exception/
├── core/
└── ai/
```

## Padrões Obrigatórios

### Controllers
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
```

### Services
```java
@Service
@Slf4j
public class UserService {
    public UserDTO criarUsuario(CreateUserDTO dto) {
        log.info("Criando usuário: {}", dto.getEmail());
        // lógica
    }
}
```

### DTOs (SEMPRE usar)
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    @NotBlank(message = "Nome obrigatório")
    private String nome;

    @Email(message = "Email inválido")
    private String email;

    @Size(min = 6)
    private String senha;
}
```

### Exception Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

## application.yml
```yaml
spring:
  application:
    name: langia-auth-service
  datasource:
    url: jdbc:postgresql://postgres:5432/langia
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  redis:
    host: redis
    port: 6379
  rabbitmq:
    host: rabbitmq
    port: 5672

server:
  port: 8080
```

## Dependências
```xml
<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## ❌ NÃO FAZER

1. Expor entidades JPA diretamente
2. Lógica de negócio no Controller
3. Hardcode de configurações
4. Catch sem log

## Regras IA

Ao sugerir código:
- SEMPRE usar DTOs
- SEMPRE adicionar validações
- SEMPRE incluir logs
- SEMPRE tratar exceções
- SEMPRE usar @Value para configs
- Código e comentários em português
- SEMPRE modelar as tabelas no padrao em ingles.

## Integrações

```java
@Value("${gemini.api-key}")
private String geminiApiKey;

@Value("${openai.api-key}")
private String openaiApiKey;

@Value("${evolution.api-url}")
private String evolutionApiUrl;
```

## Segurança

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

## Estilo
- Indentação: 4 espaços
- Line length: 120 chars
- Commits: `feat: adiciona endpoint login`
