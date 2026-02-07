# Flash Sale Engine - Test Scripts

This directory contains test scripts for various scenarios.

## Available Scripts

### 1. Basic Purchase Test
```bash
./basic-purchase.sh
```
Tests a single purchase flow.

### 2. Concurrent Purchase Test
```bash
./concurrent-test.sh
```
Simulates 100 concurrent users making purchases.

### 3. Rate Limit Test
```bash
./rate-limit-test.sh
```
Tests rate limiting by sending rapid requests.

### 4. Inventory Verification
```bash
./verify-inventory.sh
```
Validates that inventory counts match between Redis and database.

## Manual Testing

### Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "price": 99.99,
    "stockCount": 100
  }'
```

### Sync to Redis
```bash
curl -X POST http://localhost:8080/api/products/1/sync-redis
```

### Make a Purchase
```bash
curl -X POST http://localhost:8080/api/buy/1 \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123"}'
```

### Check Inventory
```bash
curl http://localhost:8080/api/products/1/inventory
```

### View Orders
```bash
curl http://localhost:8080/api/orders/all
```

## Load Testing with JMeter

See `../jmeter/README.md` for detailed JMeter instructions.

Quick run:
```bash
jmeter -n -t ../jmeter/flash-sale-test.jmx -l results.jtl -e -o report/
```
