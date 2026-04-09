# PriceFlow 📊

Um sistema inteligente de monitoramento de preços que acompanha mudanças em produtos de e-commerce brasileiros e notifica usuários via email quando há alterações significativas de preço.

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-green?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue?logo=postgresql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-AMQP-orange?logo=rabbitmq)

## 📋 Descrição

O **PriceFlow** é uma aplicação backend desenvolvida em Spring Boot que realiza web scraping de preços em plataformas de e-commerce brasileiras (Amazon e Mercado Livre) e notifica os usuários através de emails quando detecta mudanças de preço nos produtos monitorados.

**Link da API:** https://priceflow-ari6.onrender.com

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 17** - Linguagem de programação
- **Spring Boot 4.0.5** - Framework principal
- **Spring Data JPA** - ORM e persistência
- **Spring AMQP** - Integração com RabbitMQ
- **Spring Mail** - Envio de emails
- **Lombok** - Redução de boilerplate
- **Flyway** - Controle de versão do banco de dados

### Infraestrutura
- **PostgreSQL** - Banco de dados relacional (Neon)
- **RabbitMQ** - Message broker (CloudAMQP)
- **Gmail SMTP** - Servidor de email

### Testes
- **JUnit 5** - Framework de testes
- **Mockito** - Mock e spy
- **TestContainers** - Testes com containers

### Build & Deployment
- **Maven** - Gerenciador de dependências

---

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.6+** - [Download](https://maven.apache.org/)
- **Git** - [Download](https://git-scm.com/)
- **Conta PostgreSQL** (Neon ou local)
- **Conta RabbitMQ** (CloudAMQP ou local)
- **Conta Gmail** com [App Password](https://myaccount.google.com/apppasswords) gerada

### Verificar Instalação

```bash
java -version
mvn -version
git --version
```

---

## 🚀 Instalação e Configuração

### 1. Clonar o Repositório

```bash
git clone https://github.com/seu-usuario/priceflow.git
cd priceflow
```

### 2. Configurar Variáveis de Ambiente

Edite o arquivo `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: priceflow

  datasource:
    url: jdbc:postgresql://seu-host:5432/priceflow
    username: seu-usuario
    password: sua-senha
    driver-class-name: org.postgresql.Driver

  rabbitmq:
    addresses: amqps://seu-usuario:sua-senha@seu-host/seu-vhost

  mail:
    host: smtp.gmail.com
    port: 587
    username: seu-email@gmail.com
    password: sua-app-password  # Gere em https://myaccount.google.com/apppasswords
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
```

### 3. Criar Banco de Dados PostgreSQL

```sql
CREATE DATABASE priceflow;
CREATE USER priceflow_user WITH PASSWORD 'sua_senha';
GRANT ALL PRIVILEGES ON DATABASE priceflow TO priceflow_user;
```

**Ou use Neon Cloud** (recomendado):
1. Acesse https://neon.tech/
2. Crie uma nova project
3. Copie a connection string

### 4. Instalar Dependências

```bash
mvn clean install
```

### 5. Executar Migrations (Flyway)

As migrations serão aplicadas automaticamente na primeira execução. Verifique os arquivos:
- `src/main/resources/db/migration/V1__create_table_product_and_history.sql` - Criar tabelas principais
- `src/main/resources/db/migration/V2__add_last_price_column.sql` - Adicionar coluna last_price e índices

---

## 🏃 Executando a Aplicação

### Via Maven

```bash
mvn spring-boot:run
```

### Via IDE (IntelliJ IDEA / VSCode)

1. Abra o projeto
2. Localize `PriceflowApplication.java`
3. Clique com botão direito → **Run**

### Via Jar Compilado

```bash
mvn clean package
java -jar target/priceflow-0.0.1-SNAPSHOT.jar
```

A aplicação estará disponível em: `http://localhost:8080`

---

## 📁 Estrutura do Projeto

```
priceflow/
├── src/
│   ├── main/
│   │   ├── java/com/example/priceflow/
│   │   │   ├── PriceflowApplication.java          # Classe principal
│   │   │   │
│   │   │   ├── controller/                        # Controladores REST
│   │   │   │   └── ProductController.java         # Endpoints da API
│   │   │   │
│   │   │   ├── domain/                            # Entidades do domínio
│   │   │   │   ├── Product.java                   # Produto
│   │   │   │   ├── ProductId.java                 # ID composto
│   │   │   │   └── PriceHistory.java              # Histórico de preços
│   │   │   │
│   │   │   ├── dto/                               # Data Transfer Objects
│   │   │   │   ├── CreateProductRequestDTO.java   # Request para criar produto
│   │   │   │   ├── ProductResponseDTO.java        # Response do produto
│   │   │   │   ├── PriceHistoryResponseDTO.java   # Response do histórico
│   │   │   │   ├── AllProductsHistoryResponseDTO.java # Response de todos
│   │   │   │   └── EmailNotificationDTO.java      # DTO de notificação
│   │   │   │
│   │   │   ├── repository/                        # Repositórios (Data Access)
│   │   │   │   ├── ProductRepository.java         # Custom queries
│   │   │   │   └── PriceHistoryRepository.java    # Custom queries
│   │   │   │
│   │   │   ├── service/                           # Lógica de negócio
│   │   │   │   ├── ScraperService.java            # Orquestração de scrapers
│   │   │   │   └── PriceCheckScheduler.java       # Scheduler automático (a cada 2h)
│   │   │   │
│   │   │   ├── infrastructure/
│   │   │   │   ├── email/
│   │   │   │   │   └── EmailService.java          # Envio de emails
│   │   │   │   │
│   │   │   │   ├── messaging/
│   │   │   │   │   ├── AlertPublisherService.java # Publicador de alertas
│   │   │   │   │   ├── EmailConsumer.java         # Consumidor de fila
│   │   │   │   │   ├── RabbitMQConfig.java        # Configuração AMQP
│   │   │   │   │   └── RabbitMQConstants.java     # Constantes de fila
│   │   │   │   │
│   │   │   │   └── scraper/
│   │   │   │       ├── PriceScraper.java          # Interface
│   │   │   │       ├── AmazonScraper.java         # Implementação Amazon
│   │   │   │       └── MercadoLivreScraper.java   # Implementação ML
│   │   │   │
│   │   │   └── utils/
│   │   │       └── MoneyUtils.java                # Utilitários de valores
│   │   │
│   │   └── resources/
│   │       ├── application.yaml                   # Configurações
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__create_table_*.sql     # Scripts Flyway - Tabelas
│   │       │       └── V2__add_last_price_*.sql   # Scripts Flyway - Coluna
│   │       ├── static/                            # Arquivos estáticos
│   │       └── templates/                         # Templates Thymeleaf
│   │
│   └── test/
│       └── java/com/example/priceflow/
│           ├── PriceflowApplicationTests.java
│           ├── controller/
│           │   └── ProductControllerTest.java     # Testes da API
│           ├── infrastructure/
│           │   ├── AmazonScraperTest.java
│           │   └── MercadoLivreScraperTest.java
│           ├── service/
│           │   └── PriceCheckSchedulerTest.java   # Testes do scheduler
│           └── utils/
│               └── MoneyUtilsTest.java
│
├── pom.xml                                        # Dependências Maven
├── mvnw / mvnw.cmd                                # Maven Wrapper
├── HELP.md                                        # Documentação da geração
└── README.md                                      # Este arquivo
```

---

## 🔄 Fluxo de Dados

### 1. Publicação de Alerta de Preço

```
ScraperService.getPriceFromUrl()
    ↓ Detecta mudança de preço
AlertPublisherService.publishPriceAlert(EmailNotificationDTO)
    ↓ Publica mensagem
RabbitMQ (email.exchange → email.queue)
    ↓ Roteia
EmailConsumer.consumeEmailNotification()
    ↓ Processa
EmailService.sendPriceAlertEmail()
    ↓ Envia via SMTP
📧 Email para usuário
```

### 2. Arquitetura de Microserviços

```
┌─────────────────────┐
│  Web Scraper        │  (Amazon, Mercado Livre)
│  (Periodicamente)   │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│  ScraperService     │  (Detecta mudanças)
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│ AlertPublisher      │  (Publica no RabbitMQ)
└──────────┬──────────┘
           │
           ↓ (AMQP)
┌─────────────────────┐
│    RabbitMQ Queue   │  (Fila de mensagens)
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│ EmailConsumer       │  (Consome fila)
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│  EmailService       │  (Formata email HTML)
└──────────┬──────────┘
           │
           ↓ (SMTP)
┌─────────────────────┐
│  Gmail SMTP Server  │  (Envia email)
└─────────────────────┘
```

---

## ⏰ Scheduler Automático (PriceCheckScheduler)

O **PriceCheckScheduler** é responsável por verificar periodicamente os preços de todos os produtos monitorados e enviar notificações quando detecta reduções de preço.

### Como Funciona

```
┌──────────────────────────────────────────┐
│   INÍCIO DO CICLO (A CADA 2 HORAS)       │
│   Cron: 0 0 */2 * * *                    │
└──────────┬───────────────────────────────┘
           │
           ↓
┌──────────────────────────────────────────┐
│  1. Buscar todos os produtos             │
│     (productRepository.findAll())         │
└──────────┬───────────────────────────────┘
           │
           ↓
    ┌──────────────────────────┐
    │ Para cada produto:       │
    └──────────┬───────────────┘
               │
               ↓
    ┌──────────────────────────────┐
    │ 2. Fazer scraping do preço   │
    │ (ScraperService.getPriceFromUrl)
    └──────────┬───────────────────┘
               │
               ↓
    ┌──────────────────────────────┐
    │ 3. Salvar no histórico       │
    │ (priceHistoryRepository.save)│
    └──────────┬───────────────────┘
               │
               ↓
    ┌──────────────────────────────────┐
    │ 4. Comparar com último preço     │
    │ (product.getLastPrice())         │
    └──────────┬──────────────────────┘
               │
        ┌──────┴──────┐
        │             │
        ↓             ↓
   Preço menor    Preço maior/igual
        │             │
        ↓             ↓
    ✉️ ENVIAR EMAIL   📝 Log apenas
    (AlertPublisher) (Sem notificação)
        │             │
        └──────┬──────┘
               │
               ↓
    ┌──────────────────────────────┐
    │ 5. Atualizar lastPrice       │
    │ (product.setLastPrice(...))  │
    └──────────┬───────────────────┘
               │
               ↓
    ┌──────────────────────────────┐
    │ 6. Publicar no RabbitMQ      │
    │ (AlertPublisherService)      │
    └──────────────────────────────┘
```

### Configuração da Tarefa Agendada

```java
@Scheduled(cron = "0 0 */2 * * *") // A cada 2 horas
@Transactional
public void checkPrices() {
    // Verifica todos os produtos
    // Envia alertas apenas para redução de preço
}
```

**Cron Expression**: `0 0 */2 * * *`
- Executa a cada **2 horas**
- Em qualquer dia da semana
- Em qualquer mês

### Ajustar Frequência de Verificação

Para alterar a frequência, edite o padrão cron:

```java
// A cada hora
@Scheduled(cron = "0 0 * * * *")

// A cada 30 minutos
@Scheduled(cron = "0 */30 * * * *")

// A cada dia (meia-noite)
@Scheduled(cron = "0 0 0 * * *")

// A cada 6 horas
@Scheduled(cron = "0 0 */6 * * *")
```

### Exemplo de Execução

**Simulação de uma execução**:

```
14:00:00 - Início da verificação
14:00:02 - Produto: "Notebook XYZ" (https://amazon.com.br/...)
           Preço anterior: R$ 3000.00
           Preço atual: R$ 2500.00 (-16.67%)
           ✉️ EMAIL ENVIADO para usuario@example.com

14:00:05 - Produto: "Mouse Gamer" (https://mercadolivre.com.br/...)
           Preço anterior: R$ 100.00
           Preço atual: R$ 120.00 (+20%)
           📝 Sem notificação (preço aumentou)

14:00:08 - Verificação concluída com sucesso
```

### Logging

O scheduler gera logs detalhados:

```log
[INFO] Iniciando verificação de preços dos produtos...
[DEBUG] Verificando preço do produto: Notebook XYZ (https://amazon.com.br/...)
[INFO] Preço diminuiu para: Notebook XYZ - De R$ 3000.00 para R$ 2500.00 (-16.67%)
[INFO] Notificação de alerta publicada para: usuario@example.com
[INFO] Verificação de preços concluída com sucesso
```

---

## 💾 Banco de Dados

### Schema Principal

**Tabela: produtos**
```sql
CREATE TABLE produtos (
    url   VARCHAR(1000) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name  VARCHAR(255) NOT NULL,
    last_price NUMERIC(19, 2) NULL,  -- Último preço verificado

    CONSTRAINT pk_produtos PRIMARY KEY (url, email)
);
```

**Tabela: historico_preco**
```sql
CREATE TABLE historico_preco (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    price NUMERIC(19, 2),
    captured_at TIMESTAMP,

    product_url VARCHAR(1000) NOT NULL,
    product_email VARCHAR(255) NOT NULL,

    CONSTRAINT fk_historico_produto
        FOREIGN KEY (product_url, product_email)
            REFERENCES produtos (url, email)
            ON DELETE CASCADE
);
```

**Índices para Performance**:
```sql
CREATE INDEX idx_produto_email ON produtos(email);
CREATE INDEX idx_preco_captured_at ON historico_preco(captured_at);
```

### Entidades JPA

**Product.java**: Representa um produto monitorado
```java
@Entity
@Table(name = "produtos")
public class Product {
    @EmbeddedId
    private ProductId id;              // URL + Email (chave composta)

    private String name;               // Nome do produto

    private BigDecimal lastPrice;      // Último preço verificado

    @OneToMany(mappedBy = "product")
    private List<PriceHistory> history; // Histórico completo de preços
}
```

**ProductId.java**: Chave composta embedável
```java
@Embeddable
public class ProductId implements Serializable {
    @Column(nullable = false)
    private String url;    // URL do produto

    @Column(nullable = false)
    private String email;  // Email do usuário monitorando
}
```

**PriceHistory.java**: Registro histórico de preços
```java
@Entity
@Table(name = "historico_preco")
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;           // Referência ao produto

    private BigDecimal price;          // Preço capturado naquele momento

    private LocalDateTime capturedAt;  // Data/hora da captura
}
```

---

## 🌐 APIs REST

### Base URL
```
http://localhost:8080/api/products
```

---

### 1. Registrar Novo Produto para Monitoramento

**Endpoint**: `POST /api/products`

Cria um novo produto para monitorar seu preço ao longo do tempo. Realiza web scraping automático para obter o preço inicial.

**Request**:
```http
POST /api/products HTTP/1.1
Content-Type: application/json

{
  "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
  "email": "usuario@example.com"
}
```

**Parâmetros**:
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `url` | string | ✅ | URL do produto no e-commerce |
| `email` | string | ✅ | Email do usuário para notificações |

**Response (201 Created)**:
```json
{
  "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
  "email": "usuario@example.com",
  "name": "dp/B07XLP9TPC",
  "lastPrice": 2999.90,
  "history": [
    {
      "id": 1,
      "price": 2999.90,
      "capturedAt": "2024-04-08T14:30:00"
    }
  ]
}
```

**Status Codes**:
- `201` - Produto criado com sucesso
- `400` - Erro na validação ou scraping falhou
- `409` - Produto já existe para este usuário

**Exemplo com cURL**:
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
    "email": "usuario@example.com"
  }'
```

---

### 2. Obter Histórico de TODOS os Produtos

**Endpoint**: `GET /api/products/history`

Retorna o histórico de preços de todos os produtos monitorados.

**Request**:
```http
GET /api/products/history HTTP/1.1
```

**Response (200 OK)**:
```json
{
  "totalProducts": 2,
  "products": [
    {
      "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
      "email": "usuario@example.com",
      "name": "Notebook ABC",
      "lastPrice": 2500.00,
      "history": [
        {
          "id": 1,
          "price": 3000.00,
          "capturedAt": "2024-04-08T10:00:00"
        },
        {
          "id": 2,
          "price": 2500.00,
          "capturedAt": "2024-04-08T12:00:00"
        }
      ]
    },
    {
      "url": "https://www.mercadolivre.com.br/item/ABC123",
      "email": "usuario2@example.com",
      "name": "Mouse Gamer",
      "lastPrice": 89.90,
      "history": [
        {
          "id": 10,
          "price": 99.90,
          "capturedAt": "2024-04-08T11:00:00"
        },
        {
          "id": 11,
          "price": 89.90,
          "capturedAt": "2024-04-08T13:00:00"
        }
      ]
    }
  ]
}
```

**Exemplo com cURL**:
```bash
curl -X GET http://localhost:8080/api/products/history
```

---

### 3. Obter Histórico de um Produto Específico (por URL)

**Endpoint**: `GET /api/products/history/details?url={url}`

Retorna o histórico de preços de um produto específico identificado pela URL.

**Request**:
```http
GET /api/products/history/details?url=https://www.amazon.com.br/dp/B07XLP9TPC HTTP/1.1
```

**Query Parameters**:
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `url` | string | ✅ | URL do produto (deve ser URL encoded) |

**Response (200 OK)**:
```json
{
  "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
  "email": "usuario@example.com",
  "name": "Notebook ABC",
  "lastPrice": 2500.00,
  "history": [
    {
      "id": 2,
      "price": 2500.00,
      "capturedAt": "2024-04-08T12:00:00"
    },
    {
      "id": 1,
      "price": 3000.00,
      "capturedAt": "2024-04-08T10:00:00"
    }
  ]
}
```

**Status Codes**:
- `200` - Sucesso
- `404` - Produto não encontrado

**Exemplo com cURL**:
```bash
curl -X GET "http://localhost:8080/api/products/history/details?url=https://www.amazon.com.br/dp/B07XLP9TPC"
```

---

### 4. Obter Histórico por URL e Email do Usuário

**Endpoint**: `GET /api/products/history/user?url={url}&email={email}`

Retorna o histórico de um produto específico for um usuário específico.

**Request**:
```http
GET /api/products/history/user?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=usuario@example.com HTTP/1.1
```

**Query Parameters**:
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `url` | string | ✅ | URL do produto |
| `email` | string | ❌ | Email do usuário (filtro opcional) |

**Response (200 OK)**:
```json
{
  "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
  "email": "usuario@example.com",
  "name": "Notebook ABC",
  "lastPrice": 2500.00,
  "history": [...]
}
```

**Exemplo com cURL**:
```bash
curl -X GET "http://localhost:8080/api/products/history/user?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=usuario@example.com"
```

---

### 5. Obter Detalhes de um Produto Específico

**Endpoint**: `GET /api/products?url={url}&email={email}`

Retorna os detalhes completos de um produto pelo seu ID (URL + Email).

**Request**:
```http
GET /api/products?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=usuario@example.com HTTP/1.1
```

**Query Parameters**:
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `url` | string | ✅ | URL do produto |
| `email` | string | ✅ | Email do usuário |

**Response (200 OK)**:
```json
{
  "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
  "email": "usuario@example.com",
  "name": "Notebook ABC",
  "lastPrice": 2500.00,
  "history": [...]
}
```

**Status Codes**:
- `200` - Sucesso
- `404` - Produto não encontrado

**Exemplo com cURL**:
```bash
curl -X GET "http://localhost:8080/api/products?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=usuario@example.com"
```

---

### 6. Remover Produto do Monitoramento

**Endpoint**: `DELETE /api/products?url={url}&email={email}`

Remove um produto do monitoramento (exclui o produto e seu histórico).

**Request**:
```http
DELETE /api/products?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=usuario@example.com HTTP/1.1
```

**Query Parameters**:
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `url` | string | ✅ | URL do produto |
| `email` | string | ✅ | Email do usuário |

**Response (204 No Content)**:
```
(sem corpo)
```

**Status Codes**:
- `204` - Produto removido com sucesso
- `404` - Produto não encontrado

**Exemplo com cURL**:
```bash
curl -X DELETE "http://localhost:8080/api/products?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=usuario@example.com"
```

---

## 💡 Lojas Suportadas

O sistema atualmente suporta scraping de:
- ✅ **Amazon** - `amazon.com.br`
- ✅ **Mercado Livre** - `mercadolivre.com.br`

Ao tentar adicionar um produto de uma loja não suportada, receberá um erro `400 Bad Request`.

---

## 📋 Fluxo Completo de Uso

```
1. POST /api/products
   ↓ Cria produto e faz scraping inicial

2. Scheduler executa a cada 2 horas (PriceCheckScheduler)
   ↓ Verifica preço
   ↓ Se diminuiu: envia email
   ↓ Atualiza histórico

3. GET /api/products/history
   ↓ Consulta histórico de todos os produtos

4. GET /api/products/history/details
   ↓ Consulta histórico específico

5. DELETE /api/products
   ↓ Remove produto quando não interessado mais
```

---

## ⚙️ Configurações Avançadas

### RabbitMQ

**Fila de Email**:
- **Nome**: `email.queue`
- **Exchange**: `email.exchange`
- **Routing Key**: `email.routingKey`
- **Durável**: Sim
- **Auto-delete**: Não

### Email

O sistema envia emails com:
- **Formato**: HTML com CSS inline
- **Dados**: Nome produto, preço anterior, novo preço, % de mudança
- **Remetente**: noreply@priceflow.com
- **Template**: Responsivo e mobile-friendly

### Logging

```yaml
logging:
  level:
    root: INFO
    com.example.priceflow: DEBUG
    org.springframework.web: DEBUG
    org.springframework.amqp: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

---

## 🧪 Testes

### Executar Todos os Testes

```bash
mvn test
```

### Testes Unitários

```bash
mvn test -Dtest=MoneyUtilsTest
```

### Testes de Integração

```bash
mvn test -Dtest=AmazonScraperTest
mvn test -Dtest=MercadoLivreScraperTest
mvn test -Dtest=PriceCheckSchedulerTest
```

### Cobertura de Código

```bash
mvn jacoco:report
# Relatório em: target/site/jacoco/index.html
```

### Exemplo de Teste - PriceCheckScheduler

```java
@ExtendWith(MockitoExtension.class)
class PriceCheckSchedulerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private ScraperService scraperService;

    @Mock
    private AlertPublisherService alertPublisherService;

    @InjectMocks
    private PriceCheckScheduler priceCheckScheduler;

    @Test
    void shouldNotifyWhenPriceLowers() throws Exception {
        // Arrange
        Product product = createTestProduct();
        BigDecimal newPrice = new BigDecimal("2500.00"); // Menor que lastPrice (3000.00)

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(scraperService.getPriceFromUrl(anyString())).thenReturn(newPrice);
        when(priceHistoryRepository.save(any())).thenReturn(new PriceHistory());

        // Act
        priceCheckScheduler.checkPrices();

        // Assert
        verify(alertPublisherService).publishPriceAlert(any(EmailNotificationDTO.class));
    }

    @Test
    void shouldNotNotifyWhenPriceIncreases() throws Exception {
        // Arrange
        Product product = createTestProduct();
        BigDecimal newPrice = new BigDecimal("3500.00"); // Maior que lastPrice (3000.00)

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(scraperService.getPriceFromUrl(anyString())).thenReturn(newPrice);
        when(priceHistoryRepository.save(any())).thenReturn(new PriceHistory());

        // Act
        priceCheckScheduler.checkPrices();

        // Assert
        verify(alertPublisherService, never()).publishPriceAlert(any());
    }
}
```

### Exemplo de Teste - EmailService

```java
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldSendEmailSuccessfully() {
        EmailNotificationDTO dto = new EmailNotificationDTO(
            "user@example.com",
            "Notebook XYZ",
            "https://example.com/product",
            new BigDecimal("3000.00"),
            new BigDecimal("2500.00"),
            new BigDecimal("-16.67")
        );

        emailService.sendPriceAlertEmail(dto);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
```

---

## 🐛 Troubleshooting

### Problema: Conexão recusada no PostgreSQL

**Solução**:
```bash
# Verificar se PostgreSQL está rodando
psql -U seu-usuario -d priceflow

# Ou verificar a connection string no application.yaml
```

### Problema: Falha ao enviar email

**Solução**:
1. Verifique App Password no Gmail
2. Confirme configuração SMTP no application.yaml
3. Verifique logs: `grep "Mail" logs/priceflow.log`

### Problema: Mensagens não sendo consumidas do RabbitMQ

**Solução**:
```bash
# Verificar Consumer listeners ativas
# Verificar logs da aplicação para erros no EmailConsumer
# Restartar container RabbitMQ
```

### Problema: Migrações Flyway não aplicadas

**Solução**:
```bash
# Limpar e recriar banco
mvn clean install

# Ou resetar Flyway manualmente
DELETE FROM flyway_schema_history WHERE version = 1;
```

---

## 📊 Monitoramento e Métricas

### Endpoints de Health Check

```http
GET /actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "rabbit": { "status": "UP" }
  }
}
```

### Métricas de RabbitMQ

```http
GET /actuator/metrics/spring.rabbitmq.connection
```

---

## 🤝 Contribuindo

1. **Faça um Fork** do repositório
2. **Crie uma Branch** para sua feature (`git checkout -b feature/minha-feature`)
3. **Commit** suas mudanças (`git commit -m 'Adiciona minha feature'`)
4. **Push** para a Branch (`git push origin feature/minha-feature`)
5. **Abra um Pull Request**

### Estilo de Código

- Usar **Java naming conventions**
- Aplicar **checkstyle** (`mvn checkstyle:check`)
- Manter cobertura de testes acima de 80%
- Documentar métodos públicos

---

## 📄 Licença

Este projeto está licenciado sob a **MIT License** - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 📞 Contato e Suporte

- **Issues**: [GitHub Issues](https://github.com/seu-usuario/priceflow/issues)
- **Email**: seu-email@example.com
- **Discord**: [Link do servidor]

---

## 🙏 Agradecimentos

Desenvolvido com ❤️ usando:
- Spring Boot
- PostgreSQL
- RabbitMQ
- Gmail SMTP

---

## 📚 Recursos Adicionais

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring AMQP Documentation](https://spring.io/projects/spring-amqp)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [Flyway Documentation](https://flywaydb.org/documentation/)

---

**Última atualização**: 08 de Abril de 2024

`PriceFlow v0.0.1-SNAPSHOT` - Em desenvolvimento ativa 🚀
