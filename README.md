# PetCare360

Aplicacao web Java Spring Boot para monitoramento de pets com coleiras inteligentes.

> Esta pasta contem uma copia funcional do projeto `petcare360_java`. A regra de negocio, banco de dados, seguranca, endpoints, Flyway e servicos sao os mesmos da versao com HTML, mas esta versao foi separada sem os arquivos de tela `.html`.

## Sobre O Projeto

O **PetCare360** e uma aplicacao web desenvolvida para receber, processar e consultar dados simulados de coleiras inteligentes. A aplicacao monitora sinais vitais e comportamentais dos pets, como temperatura, frequencia cardiaca, nivel de atividade, localizacao e bateria.

O projeto vai alem de um CRUD simples: ele processa telemetria IoT, calcula alertas automaticos e permite fluxos de atendimento entre tutor e veterinario.

## Integrantes

| Integrante | RM |
|---|---:|
| Leonardo Zerbinatti de Sales | 562992 |
| Luis Guilherme Borges Silva | 566548 |
| Rafael de Freitas Moraes | 563210 |
| Rafael Pascotte Mercadante | 564928 |

## Tecnologias

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Flyway
- Bean Validation
- Oracle SQL
- Swagger / OpenAPI
- Maven

## Principais Funcionalidades

- Cadastro e login de usuarios.
- Cadastro de conta como tutor ou veterinario pela tela web.
- Autenticacao com Spring Security por formulario.
- Perfis `ROLE_ADMIN`, `ROLE_CLIENTE` e `ROLE_VETERINARIO`.
- Protecao de rotas com base no perfil do usuario.
- CRUD de pets para tutor, veterinario e administrador.
- CRUD de leituras IoT para veterinario e administrador.
- Criacao de pet ja vinculando coleira e primeira leitura.
- Recebimento de dados IoT da coleira inteligente.
- Persistencia historica das leituras em `SJ_SENSOR_DATA`.
- Calculo automatico de status: `NORMAL`, `WARNING` e `CRITICAL`.
- Geracao automatica de alertas.
- Mensagens entre tutor e veterinario.
- Agendamento de consultas.
- Recomendacoes de cuidados feitas pelo veterinario.
- Consulta de monitoramento, atividade, localizacao e alertas.
- Listagem paginada e listagem sem paginacao.
- Cache para otimizar consultas.
- Tratamento global de erros.
- Documentacao com Swagger.

## Funcionalidades Fora De CRUD

O requisito pede fluxos completos alem de CRUD. No projeto foram implementados:

- **Vinculo tutor e veterinario:** o tutor escolhe um veterinario para acompanhar um pet.
- **Mensagens:** tutor e veterinario trocam mensagens vinculadas ao pet.
- **Consultas:** tutor solicita consulta e o veterinario/admin consegue acompanhar o pedido.
- **Recomendacoes:** veterinario registra orientacoes de cuidado para o tutor.
- **Monitoramento IoT:** o sistema recebe leituras da coleira, classifica o status do pet e gera alertas automaticos.

## Controle De Acesso

| Perfil | Permissoes principais |
|---|---|
| `ROLE_CLIENTE` | Acessa area do tutor, cadastra seus pets, vincula veterinario, envia mensagens, solicita consultas e visualiza recomendacoes. |
| `ROLE_VETERINARIO` | Acessa area do veterinario, consulta pacientes vinculados, responde mensagens, acompanha consultas, cria recomendacoes e gerencia leituras IoT. |
| `ROLE_ADMIN` | Acessa area administrativa, visualiza usuarios, pets, vinculos, mensagens, consultas, recomendacoes e tem visao geral do sistema. |

## Acessos De Teste

As credenciais abaixo sao criadas pela carga inicial do banco:

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | `admin@petcare360.com` | `admin000` |
| Tutor | `tutor@petcare360.com` | `senha1234` |
| Veterinario | `veterinario@petcare360.com` | `senha1234` |

## Arquitetura

O projeto segue arquitetura em camadas:

```text
controller -> service -> repository -> database
```

Pacotes principais:

```text
src/main/java/br/com/fiap/petcare360_java
├── config
├── controller
├── dto
├── exception
├── model
├── repository
├── security
└── service
```

## Entidades Principais

- `AppUser`
- `Role`
- `Pet`
- `Device`
- `SensorData`
- `Alert`
- `PetMessage`
- `Appointment`
- `CareRecommendation`

Relacionamentos:

- Usuario 1:N Pet
- Usuario N:N Role
- Pet 1:1 Device
- Device 1:N SensorData
- Pet 1:N Alert
- Pet N:N Veterinario
- Pet 1:N Mensagem
- Pet 1:N Consulta
- Pet 1:N Recomendacao

## Regras De Negocio IoT

Endpoint principal:

```http
POST /api/iot/data
```

Payload exemplo:

```json
{
  "deviceId": "COLLAR_001",
  "timestamp": "2026-04-27T10:30:00Z",
  "temperature": 38.5,
  "heartRate": 110,
  "activityLevel": 72,
  "latitude": -23.6815,
  "longitude": -46.8755,
  "battery": 85
}
```

Ao receber uma leitura, o backend:

1. Valida o payload.
2. Verifica se o device existe.
3. Verifica se o device esta ativo.
4. Verifica se o device esta vinculado a um pet.
5. Calcula o status da leitura.
6. Salva a leitura como historico.
7. Atualiza bateria e ultimo contato do device.
8. Gera alertas quando necessario.

## Endpoints Principais

### Auth

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | `/auth/register` | Registra usuario |
| POST | `/auth/login` | Realiza login |
| GET | `/auth/me` | Retorna o usuario autenticado pela sessao atual |
| POST | `/auth/logout` | Encerra a sessao e invalida o cookie JSESSIONID |

### Pets

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/pets?page=0&size=10` | Lista pets com paginacao |
| GET | `/pets/all` | Lista pets sem paginacao |
| GET | `/pets/patients` | Lista pacientes vinculados ao veterinario autenticado |
| POST | `/pets` | Cadastra pet, coleira e primeira leitura |
| GET | `/pets/{id}` | Consulta pet por ID |
| PUT | `/pets/{id}` | Atualiza pet e registra nova leitura |
| DELETE | `/pets/{id}` | Remove pet e dados vinculados |
| GET | `/pets/{id}/health-status` | Consulta status consolidado |
| GET | `/pets/quick-alerts` | Lista pets em alerta ou critico |
| GET | `/pets/{id}/activity-summary` | Resume atividade das ultimas 24h |

### Veterinarios

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/veterinarians` | Lista usuarios ativos com perfil de veterinario |
| POST | `/veterinarians/link` | Vincula um veterinario ao pet do tutor autenticado |

### IoT

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/iot/data` | Lista leituras IoT |
| POST | `/api/iot/data` | Recebe telemetria da coleira |
| GET | `/api/iot/data/{id}` | Busca leitura IoT por ID |
| PUT | `/api/iot/data/{id}` | Atualiza leitura IoT |
| DELETE | `/api/iot/data/{id}` | Remove leitura IoT |

### Fluxos Nao CRUD

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/messages` | Lista mensagens do usuario autenticado |
| POST | `/messages` | Envia mensagem entre tutor e veterinario |
| GET | `/appointments` | Lista consultas do usuario autenticado |
| POST | `/appointments` | Solicita consulta para um pet |
| PUT | `/appointments/{id}` | Edita pet, veterinario, data e motivo da consulta |
| PUT | `/appointments/{id}/finish` | Veterinario ou admin finaliza consulta |
| DELETE | `/appointments/{id}` | Veterinario ou admin remove consulta |
| GET | `/recommendations` | Lista recomendacoes de cuidado |
| POST | `/recommendations` | Veterinario cria recomendacao para o tutor |

### Monitoramento

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/pets/{id}/summary` | Resumo do pet |
| GET | `/pets/{id}/monitoring` | Historico de leituras |
| GET | `/pets/{id}/activity` | Historico de atividade |
| GET | `/pets/{id}/location` | Ultima localizacao |
| GET | `/pets/{id}/alerts` | Alertas do pet |

## Configuracao Do Banco Oracle

O projeto usa variaveis de ambiente para evitar senha no codigo.

O script e a base do banco de dados utilizada no projeto estao neste repositorio:

```text
https://github.com/pascotterafaaa/petcare360_DB
```

Configure antes de executar:

```bash
export ORACLE_DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
export ORACLE_DB_USERNAME=rm564928
export ORACLE_DB_PASSWORD=SUA_SENHA_AQUI
```

Opcionalmente, use o arquivo `.env.example` como modelo para criar um `.env` local. Nao suba o `.env` para o GitHub.

## Flyway

O projeto usa Flyway para versionar o banco de dados. A migration principal fica em:

```text
src/main/resources/db/migration/V1__create_petcare360_schema.sql
```

Ao iniciar a aplicacao, o Flyway cria/valida as tabelas `SJ_` e mantem o historico das migrations aplicadas.

## Como Executar

Na raiz do projeto:

```bash
./mvnw spring-boot:run
```

A API ficara disponivel em:

```text
http://localhost:8080
```

Tela principal:

```text
http://localhost:8080/login
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## Docker

O projeto possui `Dockerfile` para publicacao em ambientes como Render e Azure.

Exemplo local:

```bash
docker build -t petcare360 .
docker run --rm -p 8080:8080 \
  -e ORACLE_DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL \
  -e ORACLE_DB_USERNAME=rm564928 \
  -e ORACLE_DB_PASSWORD=SUA_SENHA_AQUI \
  petcare360
```

## Como Testar Rapidamente

1. Entrar em `http://localhost:8080/login`.
2. Usar um dos acessos de teste ou criar conta em `/register`.
3. Como tutor, cadastrar pet, vincular veterinario, enviar mensagem e solicitar consulta.
4. Como veterinario, responder mensagens, acompanhar consultas, criar recomendacoes e testar IoT.
5. Como admin, consultar usuarios, pets vinculados, mensagens, consultas e recomendacoes.
6. Abrir o Swagger em `/swagger-ui/index.html` para testar os endpoints REST.

## Exemplo De Cadastro De Pet

```json
{
  "name": "Thor",
  "age": 4,
  "weight": 12.5,
  "breed": "Golden Retriever",
  "species": "DOG",
  "deviceId": "COLLAR_001",
  "initialSensorData": {
    "timestamp": "2026-04-27T10:30:00Z",
    "temperature": 38.5,
    "heartRate": 110,
    "activityLevel": 72,
    "latitude": -23.6815,
    "longitude": -46.8755,
    "battery": 85
  }
}
```

## Testes

Para executar os testes automatizados:

```bash
./mvnw test
```
