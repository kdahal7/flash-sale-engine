# 📡 API Documentation - Flash Sale Engine

Complete API reference for the Flash Sale Engine.

---

## Base URL

```
Local Development: http://localhost:8080
Production: https://your-domain.com
```

---

## Authentication

Currently, the API does not require authentication (for demo purposes).

**For Production**: Add JWT-based authentication:
- Header: `Authorization: Bearer <token>`
- User ID will be extracted from token

---

## API Endpoints

### 🛒 Purchase API

#### Make a Purchase

Purchase a product in the flash sale.

**Endpoint**: `POST /api/buy/{productId}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| productId | Long | Yes | ID of the product to purchase |

**Request Body**:
```json
{
  "userId": "user123",
  "quantity": 1
}
```

**Request Headers**:
```
Content-Type: application/json
```

**Success Response (200 OK)**:
```json
{
  "success": true,
  "message": "Purchase successful! Your order has been placed.",
  "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "productId": 1,
  "userId": "user123"
}
```

**Error Responses**:

**Out of Stock (400 Bad Request)**:
```json
{
  "success": false,
  "message": "Sorry, this product is out of stock.",
  "productId": 1
}
```

**Rate Limited (400 Bad Request)**:
```json
{
  "success": false,
  "message": "Rate limit exceeded. Please try again later.",
  "userId": "user123"
}
```

**Product Not Found (400 Bad Request)**:
```json
{
  "success": false,
  "message": "Product not found"
}
```

**Example cURL**:
```bash
curl -X POST http://localhost:8080/api/buy/1 \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "quantity": 1
  }'
```

**Example JavaScript**:
```javascript
fetch('http://localhost:8080/api/buy/1', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    userId: 'user123',
    quantity: 1
  })
})
.then(response => response.json())
.then(data => console.log(data));
```

**Rate Limit**: 5 requests per second per user

---

### 📦 Product Management API

#### Create Product

Create a new product for flash sale.

**Endpoint**: `POST /api/products`

**Request Body**:
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 Pro chip",
  "price": 999.99,
  "stockCount": 100
}
```

**Success Response (201 Created)**:
```json
{
  "id": 1,
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 Pro chip",
  "price": 999.99,
  "stockCount": 100,
  "createdAt": "2026-02-07T10:00:00",
  "updatedAt": "2026-02-07T10:00:00"
}
```

**Example cURL**:
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone",
    "price": 999.99,
    "stockCount": 100
  }'
```

---

#### Get Product

Get product details including Redis inventory.

**Endpoint**: `GET /api/products/{id}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Product ID |

**Success Response (200 OK)**:
```json
{
  "product": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone",
    "price": 999.99,
    "stockCount": 100,
    "createdAt": "2026-02-07T10:00:00",
    "updatedAt": "2026-02-07T10:00:00"
  },
  "redisStock": 95,
  "dbStock": 100
}
```

**Example cURL**:
```bash
curl http://localhost:8080/api/products/1
```

---

#### Get All Products

Get list of all products.

**Endpoint**: `GET /api/products`

**Success Response (200 OK)**:
```json
[
  {
    "id": 1,
    "name": "iPhone 15 Pro",
    "price": 999.99,
    "stockCount": 100,
    "createdAt": "2026-02-07T10:00:00"
  },
  {
    "id": 2,
    "name": "Samsung Galaxy S24",
    "price": 899.99,
    "stockCount": 150,
    "createdAt": "2026-02-07T11:00:00"
  }
]
```

**Example cURL**:
```bash
curl http://localhost:8080/api/products
```

---

#### Sync Inventory to Redis

Sync product inventory from database to Redis.

**Endpoint**: `POST /api/products/{id}/sync-redis`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Product ID |

**Success Response (200 OK)**:
```json
{
  "message": "Inventory synced to Redis",
  "productId": 1,
  "redisStock": 100
}
```

**Example cURL**:
```bash
curl -X POST http://localhost:8080/api/products/1/sync-redis
```

**When to use**: 
- After creating a product
- To reset inventory for a new flash sale
- After fixing inventory discrepancies

---

#### Get Product Inventory

Get current Redis inventory without product details.

**Endpoint**: `GET /api/products/{id}/inventory`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Product ID |

**Success Response (200 OK)**:
```json
{
  "productId": 1,
  "stock": 95,
  "available": true
}
```

**Example cURL**:
```bash
curl http://localhost:8080/api/products/1/inventory
```

---

### 📋 Order Management API

#### Get User Orders

Get all orders for a specific user.

**Endpoint**: `GET /api/orders/user/{userId}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | User ID |

**Success Response (200 OK)**:
```json
[
  {
    "id": 1,
    "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "productId": 1,
    "userId": "user123",
    "quantity": 1,
    "price": 999.99,
    "status": "CONFIRMED",
    "createdAt": "2026-02-07T10:30:00"
  },
  {
    "id": 5,
    "orderId": "b2c3d4e5-f6g7-8901-bcde-fg2345678901",
    "productId": 2,
    "userId": "user123",
    "quantity": 1,
    "price": 899.99,
    "status": "CONFIRMED",
    "createdAt": "2026-02-07T11:00:00"
  }
]
```

**Example cURL**:
```bash
curl http://localhost:8080/api/orders/user/user123
```

---

#### Get All Orders

Get all orders in the system (admin endpoint).

**Endpoint**: `GET /api/orders/all`

**Success Response (200 OK)**:
```json
[
  {
    "id": 1,
    "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "productId": 1,
    "userId": "user123",
    "quantity": 1,
    "price": 999.99,
    "status": "CONFIRMED",
    "createdAt": "2026-02-07T10:30:00"
  }
]
```

**Example cURL**:
```bash
curl http://localhost:8080/api/orders/all
```

---

#### Get Single Order

Get details of a specific order by order ID.

**Endpoint**: `GET /api/orders/{orderId}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| orderId | String | Yes | UUID of the order |

**Success Response (200 OK)**:
```json
{
  "id": 1,
  "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "productId": 1,
  "userId": "user123",
  "quantity": 1,
  "price": 999.99,
  "status": "CONFIRMED",
  "createdAt": "2026-02-07T10:30:00"
}
```

**Example cURL**:
```bash
curl http://localhost:8080/api/orders/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

---

### 🏥 Health & Monitoring API

#### Health Check

Check application health status.

**Endpoint**: `GET /actuator/health`

**Success Response (200 OK)**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    }
  }
}
```

**Example cURL**:
```bash
curl http://localhost:8080/actuator/health
```

---

#### Prometheus Metrics

Get Prometheus-formatted metrics for monitoring.

**Endpoint**: `GET /actuator/prometheus`

**Success Response (200 OK)**:
```
# HELP http_server_requests_seconds  
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{exception="None",method="POST",outcome="SUCCESS",status="200",uri="/api/buy/{productId}",} 1523.0
http_server_requests_seconds_sum{exception="None",method="POST",outcome="SUCCESS",status="200",uri="/api/buy/{productId}",} 76.234

# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space",} 5.24288E7

# ... more metrics
```

**Example cURL**:
```bash
curl http://localhost:8080/actuator/prometheus
```

---

#### Application Info

Get application information.

**Endpoint**: `GET /actuator/info`

**Success Response (200 OK)**:
```json
{
  "app": {
    "name": "flash-sale-engine",
    "version": "1.0.0",
    "description": "High-Concurrency Flash Sale Engine"
  }
}
```

---

## Error Responses

### Standard Error Format

All error responses follow this format:

```json
{
  "status": 400,
  "message": "Error description",
  "timestamp": "2026-02-07T10:30:00"
}
```

### HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET/POST request |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid input, out of stock, rate limited |
| 404 | Not Found | Product/Order not found |
| 500 | Internal Server Error | Unexpected server error |

---

## Rate Limiting

### Configuration

- **Max Requests**: 5 per user per second
- **Window**: 1 second (sliding window)
- **Scope**: Per user (based on userId)

### Headers (Future Enhancement)

```
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 2
X-RateLimit-Reset: 1675771800
```

### Rate Limit Response

When rate limited:
```json
{
  "success": false,
  "message": "Rate limit exceeded. Please try again later.",
  "userId": "user123"
}
```

---

## Testing the API

### Postman Collection

Import this JSON into Postman:

```json
{
  "info": {
    "name": "Flash Sale Engine",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Purchase Product",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"userId\": \"user123\", \"quantity\": 1}"
        },
        "url": "{{baseUrl}}/api/buy/1"
      }
    },
    {
      "name": "Create Product",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"name\": \"iPhone 15\", \"price\": 999.99, \"stockCount\": 100}"
        },
        "url": "{{baseUrl}}/api/products"
      }
    }
  ],
  "variable": [
    {"key": "baseUrl", "value": "http://localhost:8080"}
  ]
}
```

### JavaScript/Node.js Example

```javascript
const axios = require('axios');

const BASE_URL = 'http://localhost:8080';

// Create a product
async function createProduct() {
  const response = await axios.post(`${BASE_URL}/api/products`, {
    name: 'iPhone 15 Pro',
    price: 999.99,
    stockCount: 100
  });
  return response.data.id;
}

// Sync to Redis
async function syncToRedis(productId) {
  await axios.post(`${BASE_URL}/api/products/${productId}/sync-redis`);
}

// Make a purchase
async function purchase(productId, userId) {
  const response = await axios.post(`${BASE_URL}/api/buy/${productId}`, {
    userId: userId
  });
  return response.data;
}

// Main flow
(async () => {
  const productId = await createProduct();
  await syncToRedis(productId);
  const result = await purchase(productId, 'user123');
  console.log('Purchase result:', result);
})();
```

### Python Example

```python
import requests

BASE_URL = 'http://localhost:8080'

# Create a product
def create_product():
    response = requests.post(f'{BASE_URL}/api/products', json={
        'name': 'iPhone 15 Pro',
        'price': 999.99,
        'stockCount': 100
    })
    return response.json()['id']

# Sync to Redis
def sync_to_redis(product_id):
    requests.post(f'{BASE_URL}/api/products/{product_id}/sync-redis')

# Make a purchase
def purchase(product_id, user_id):
    response = requests.post(f'{BASE_URL}/api/buy/{product_id}', json={
        'userId': user_id
    })
    return response.json()

# Main flow
if __name__ == '__main__':
    product_id = create_product()
    sync_to_redis(product_id)
    result = purchase(product_id, 'user123')
    print('Purchase result:', result)
```

---

## WebSocket Support (Future Enhancement)

Real-time inventory updates:

```javascript
const socket = new WebSocket('ws://localhost:8080/inventory');

socket.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log(`Product ${data.productId}: ${data.stock} remaining`);
};
```

---

## Webhooks (Future Enhancement)

Get notified when orders are created:

```json
POST https://your-server.com/webhook
{
  "event": "order.created",
  "orderId": "uuid",
  "productId": 1,
  "userId": "user123",
  "timestamp": "2026-02-07T10:30:00"
}
```

---

## API Versioning (Future)

Support multiple API versions:

```
v1: /api/v1/buy/{productId}
v2: /api/v2/buy/{productId}
```

---

## Best Practices

### For Clients

1. **Retry Logic**: Implement exponential backoff for failed requests
2. **Rate Limiting**: Respect rate limits, don't spam
3. **Error Handling**: Handle all error status codes
4. **Idempotency**: Store orderId to prevent duplicate orders
5. **Timeouts**: Set reasonable timeouts (5-10 seconds)

### For Load Testing

1. Use unique userIds for each virtual user
2. Ramp up gradually (5-10 seconds)
3. Monitor response times and error rates
4. Verify final inventory matches expectations

---

## Support

For API issues:
1. Check application logs
2. Verify Redis and PostgreSQL connections
3. Review [QUICKSTART.md](QUICKSTART.md) for setup
4. Check [DEPLOYMENT.md](DEPLOYMENT.md) for configuration

---

**Happy Testing! 🚀**
