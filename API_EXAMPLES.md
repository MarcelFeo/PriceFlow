# 📋 Exemplos de Uso da API - PriceFlow

Guia prático com exemplos de requisições para testar todas as endpoints da API.

## 🛠️ Pré-requisitos

- Aplicação rodando em `http://localhost:8080`
- `curl` instalado (ou Postman/Insomnia)
- As lojas suportadas: Amazon e Mercado Livre

---

## 1. Registrar Novo Produto

### Exemplo 1: Amazon

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
    "email": "joao@example.com"
  }'
```

**Response (200)**:
```json
{
  "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
  "email": "joao@example.com",
  "name": "Notebook ABC",
  "lastPrice": 3499.90,
  "history": [
    {
      "id": 1,
      "price": 3499.90,
      "capturedAt": "2024-04-08T14:30:00"
    }
  ]
}
```

### Exemplo 2: Mercado Livre

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.mercadolivre.com.br/item/ABC123456",
    "email": "maria@example.com"
  }'
```

### Exemplo 3: Validação - Email Inválido

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
    "email": "email-invalido"
  }'
```

**Response (400 Bad Request)** - Email inválido

### Exemplo 4: Erro - Loja Não Suportada

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.aliexpress.com/item/123456",
    "email": "usuario@example.com"
  }'
```

**Response (400 Bad Request)** - Loja não suportada

---

## 2. Obter Histórico de TODOS os Produtos

```bash
curl -X GET http://localhost:8080/api/products/history
```

**Response (200)**:
```json
{
  "totalProducts": 2,
  "products": [
    {
      "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
      "email": "joao@example.com",
      "name": "Notebook ABC",
      "lastPrice": 3299.90,
      "history": [
        {
          "id": 3,
          "price": 3299.90,
          "capturedAt": "2024-04-08T16:00:00"
        },
        {
          "id": 2,
          "price": 3399.90,
          "capturedAt": "2024-04-08T14:00:00"
        },
        {
          "id": 1,
          "price": 3499.90,
          "capturedAt": "2024-04-08T12:00:00"
        }
      ]
    },
    {
      "url": "https://www.mercadolivre.com.br/item/ABC123456",
      "email": "maria@example.com",
      "name": "Mouse Gamer RGB",
      "lastPrice": 89.90,
      "history": [
        {
          "id": 5,
          "price": 89.90,
          "capturedAt": "2024-04-08T13:00:00"
        },
        {
          "id": 4,
          "price": 99.90,
          "capturedAt": "2024-04-08T11:00:00"
        }
      ]
    }
  ]
}
```

---

## 3. Obter Histórico de um Produto Específico (por URL)

```bash
curl -X GET "http://localhost:8080/api/products/history/details?url=https://www.amazon.com.br/dp/B07XLP9TPC"
```

**Parâmetro está url-encoded?** Se a URL tiver caracteres especiais:

```bash
# URL encoded
curl -X GET "http://localhost:8080/api/products/history/details?url=https%3A%2F%2Fwww.amazon.com.br%2Fdp%2FB07XLP9TPC"
```

**Response (200)**:
```json
{
  "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
  "email": "joao@example.com",
  "name": "Notebook ABC",
  "lastPrice": 3299.90,
  "history": [
    {
      "id": 3,
      "price": 3299.90,
      "capturedAt": "2024-04-08T16:00:00"
    },
    {
      "id": 2,
      "price": 3399.90,
      "capturedAt": "2024-04-08T14:00:00"
    }
  ]
}
```

---

## 4. Obter Histórico por URL e Email

```bash
curl -X GET "http://localhost:8080/api/products/history/user?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=joao@example.com"
```

**Sem especificar email** (traz o primeiro encontrado):

```bash
curl -X GET "http://localhost:8080/api/products/history/user?url=https://www.amazon.com.br/dp/B07XLP9TPC"
```

**Response (200)**: Igual ao exemplo anterior

---

## 5. Obter Detalhes de um Produto

```bash
curl -X GET "http://localhost:8080/api/products?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=joao@example.com"
```

**Response (200)**:
```json
{
  "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
  "email": "joao@example.com",
  "name": "Notebook ABC",
  "lastPrice": 3299.90,
  "history": [...]
}
```

**Response (404)** - Produto não encontrado:
```json
```

---

## 6. Remover Produto do Monitoramento

```bash
curl -X DELETE "http://localhost:8080/api/products?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=joao@example.com"
```

**Response (204 No Content)** - Sem corpo na resposta

**Response (404)** - Se produto não existe:
```json
```

---

## 📊 Fluxo Completo de Teste

### Passo 1: Registrar Produto

```bash
# Amazon
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
    "email": "teste@example.com"
  }'
```

```bash
# Mercado Livre
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.mercadolivre.com.br/item/ABC123",
    "email": "teste@example.com"
  }'
```

### Passo 2: Listar Todos os Produtos

```bash
curl -X GET http://localhost:8080/api/products/history
```

### Passo 3: Ver Histórico de Um Produto

```bash
curl -X GET "http://localhost:8080/api/products/history/details?url=https://www.amazon.com.br/dp/B07XLP9TPC"
```

### Passo 4: Remover Produto

```bash
curl -X DELETE "http://localhost:8080/api/products?url=https://www.amazon.com.br/dp/B07XLP9TPC&email=teste@example.com"
```

### Passo 5: Verificar Remoção

```bash
# Este deve retornar 404
curl -X GET "http://localhost:8080/api/products/history/details?url=https://www.amazon.com.br/dp/B07XLP9TPC"
```

---

## 🧪 Testando com Postman/Insomnia

### 1. Criar Collection

- Arquivo → Nova Collection → "PriceFlow API"

### 2. Criar Requests

**POST - Create Product**
- URL: `http://localhost:8080/api/products`
- Method: `POST`
- Body (JSON):
```json
{
  "url": "https://www.amazon.com.br/dp/B07XLP9TPC",
  "email": "teste@example.com"
}
```

**GET - All Products History**
- URL: `http://localhost:8080/api/products/history`
- Method: `GET`

**GET - Product History by URL**
- URL: `http://localhost:8080/api/products/history/details`
- Params: `url` = `https://www.amazon.com.br/dp/B07XLP9TPC`

**DELETE - Remove Product**
- URL: `http://localhost:8080/api/products`
- Method: `DELETE`
- Params:
  - `url` = `https://www.amazon.com.br/dp/B07XLP9TPC`
  - `email` = `teste@example.com`

---

## 🔔 O Que Esperar

### Após Registrar um Produto

1. **Imediatamente**: ✅ Produto criado com primeiro preço capturado
2. **A cada 2 horas**: ⏰ Scheduler verifica o preço
3. **Se preço diminuir**: 📧 Email enviado para o usuário

### Exemplo de Sequência

```
12:00 - POST /api/products (criar produto)
        ↓ Preço inicial: R$ 3000.00

14:00 - PriceCheckScheduler executa
        Novo preço: R$ 2899.00 (diminuiu)
        ↓ Email enviado! ✉️

16:00 - PriceCheckScheduler executa
        Novo preço: R$ 2899.00 (mantém igual)
        ↓ Sem email

18:00 - PriceCheckScheduler executa
        Novo preço: R$ 3100.00 (aumentou)
        ↓ Sem email (apenas log)
```

---

## 🐛 Troubleshooting

### Erro: "Método HTTP não permitido"

Verifique se está usando o método correto (POST, GET, DELETE)

### Erro: "URL inválida"

- URL precisa estar completa com `https://`
- Se usar curl, a URL pode precisar estar entre aspas ou URL-encoded

### Erro: "Loja não suportada"

Verifique se está usando:
- ✅ `amazon.com.br`
- ✅ `mercadolivre.com.br`

### Email não recebido

- Verifique configuração do Gmail no `application.yaml`
- Use App Password, não senha normal
- Confirme que RabbitMQ está rodando
- Verifique logs da aplicação

---

## 📝 Notas Importantes

1. **URL deve ser completa**: `https://www.amazon.com.br/dp/...`
2. **Email deve ser válido**: Será usado para enviar notificações
3. **Cada produto é único por URL + EMAIL**: Mesma URL com emails diferentes = produtos diferentes
4. **Histórico é ordenado**: Mais recentes primeiro (DESC)
5. **Delete é permanente**: Deleta produto e todo seu histórico

---

**Última atualização**: 08 de Abril de 2024
