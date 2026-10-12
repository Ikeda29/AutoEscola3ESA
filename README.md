# AutoEscola3ESA – API REST de agendamento de instruções

Projeto da disciplina **SOA e WebServices** (FIAP) – Checkpoint 5.

## Integrantes do grupo

- Isadora Meneghetti – RM556326
- Henrique Azevedo – RM556707
- Gustavo Ikeda – RM554718
- Ricardo Fernandes – RM554597
- Khadija Lima – RM558971

## Tecnologias

Java 17+ (projeto configurado com Java 25), Spring Boot 4.0.5, Spring Security + JWT (java-jwt),
Spring Data JPA, MySQL, Flyway, Bean Validation, springdoc-openapi (Swagger), Lombok.

## Como executar

1. Crie o banco MySQL `autoescola3esa` (usuário/senha em `src/main/resources/application.properties`).
2. `./mvnw spring-boot:run` – a API sobe na porta **8085** e o Flyway cria as tabelas.
3. Faça login em `POST /login` e use o token como `Bearer` nas demais rotas.

## O que foi implementado

| Requisito | Onde |
|---|---|
| CRUD de Instrutores, agendamento de instruções, login JWT | `controller/`, `service/`, `domain/` |
| **Consumo de API externa (ViaCEP)** | `service/ViaCepService`, `controller/EnderecoController` → `GET /enderecos/{cep}` |
| **Swagger / OpenAPI** | `infra/documentation/SwaggerConfig` → `http://localhost:8085/swagger-ui.html` |
| **CORS** | `infra/security/SecurityConfig` (origens, métodos e headers liberados) |
| **Testes automatizados** | `src/test/java/...` (ver abaixo) |

### API externa – ViaCEP

`GET /enderecos/{cep}` (autenticado) consulta `https://viacep.com.br/ws/{cep}/json/` e devolve o endereço no mesmo
formato do cadastro (`DadosEndereco`). Aceita `01310100` ou `01310-100`.
Respostas: `200` ok · `400` CEP inválido · `404` CEP inexistente · `502` ViaCEP indisponível.

### Testes automatizados (por entidade)

| Entidade | Testes |
|---|---|
| Instrutor | `InstrutorControllerTest` (MockMvc + segurança por perfil), `InstrutorServiceTest`, `InstrutorRepositoryTest` |
| Aluno | `AlunoRepositoryTest` |
| Usuário | `UsuarioRepositoryTest`, `AutenticacaoServiceTest` |
| Instrução | `InstrucaoRepositoryTest`, `AgendaDeInstrucoesTest`, `InstrucaoControllerTest` |
| API externa | `ViaCepServiceTest` (resposta simulada, sem internet), `EnderecoControllerTest` |

Todos os testes usam **H2 em memória** (perfil `h2`), então `./mvnw test` não precisa de MySQL.
O MySQL só é necessário para executar a API de verdade.

```
./mvnw test
```
