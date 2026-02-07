# JMeter Load Testing Guide

This directory contains JMeter test plans for load testing the Flash Sale Engine.

## Prerequisites

1. **Install JMeter**:
   - Download from: https://jmeter.apache.org/download_jmeter.cgi
   - Extract and add `bin/` directory to PATH

2. **Start the Application**:
   ```bash
   # Start infrastructure
   docker-compose up -d
   
   # Run the application
   mvn spring-boot:run
   ```

3. **Initialize Test Data**:
   ```bash
   # Create a product with 100 stock
   curl -X POST http://localhost:8080/api/products \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Test Product",
       "price": 99.99,
       "stockCount": 100
     }'
   
   # Sync to Redis (replace {id} with the product ID from above)
   curl -X POST http://localhost:8080/api/products/1/sync-redis
   ```

## Running Tests

### GUI Mode (Interactive)
```bash
jmeter -t flash-sale-test.jmx
```

### CLI Mode (Headless)
```bash
# Run the test
jmeter -n -t flash-sale-test.jmx -l results.jtl -e -o report/

# View the HTML report
# Open report/index.html in a browser
```

## Test Scenarios

### 1. Basic Load Test (Default)
- **Users**: 500 concurrent users
- **Stock**: 100 items
- **Ramp-up**: 5 seconds
- **Expected**: Exactly 100 successful purchases

### 2. Stress Test
Edit `flash-sale-test.jmx` or use command line:
```bash
jmeter -n -t flash-sale-test.jmx \
  -JNUM_USERS=1000 \
  -l stress-results.jtl
```

### 3. Spike Test
- **Users**: 1000 concurrent users
- **Ramp-up**: 1 second
- **Tests**: System behavior under sudden load

## Verifying Results

### Check Database Orders
```bash
# Count orders in database
curl http://localhost:8080/api/orders/all | jq 'length'

# Should equal the initial stock count (100)
```

### Check Redis Inventory
```bash
# Check remaining inventory
curl http://localhost:8080/api/products/1/inventory

# Should be 0 if all items sold
```

### Verify No Overselling
```sql
-- Connect to PostgreSQL
psql -h localhost -U postgres -d flashsale

-- Count confirmed orders for product
SELECT COUNT(*) FROM orders WHERE product_id = 1 AND status = 'CONFIRMED';

-- Should be exactly 100 (not 101 or more!)
```

## Understanding Results

### Key Metrics

1. **Response Time**:
   - Avg: < 100ms (good)
   - 95th percentile: < 200ms (good)

2. **Throughput**:
   - Should handle 100+ requests/sec

3. **Error Rate**:
   - Some 400 errors expected (out of stock)
   - No 500 errors (server errors are bad!)

4. **Success Rate**:
   - Exactly 100 successful purchases
   - Remaining 400 should get "out of stock"

### Sample Report Structure
```
report/
├── index.html          # Main dashboard
├── content/
│   ├── js/
│   └── css/
└── statistics.json     # Raw data
```

## Common Issues

### Issue: All requests fail
**Solution**: Check if the application is running and accessible at `http://localhost:8080`

### Issue: More than 100 orders created
**Solution**: This is a bug! Check the Redis atomic operation in `InventoryService.java`

### Issue: JMeter hangs
**Solution**: Use CLI mode instead of GUI for large tests

## Advanced Configuration

### Custom Variables
Edit the test plan or use command line:
```bash
jmeter -n -t flash-sale-test.jmx \
  -JBASE_URL=http://your-server:8080 \
  -JPRODUCT_ID=1 \
  -JNUM_USERS=500 \
  -l results.jtl
```

### Distributed Testing
For very high loads, use JMeter distributed mode:
```bash
# On master
jmeter -n -t flash-sale-test.jmx \
  -R server1,server2,server3 \
  -l results.jtl
```

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Run Load Test
  run: |
    jmeter -n -t src/test/jmeter/flash-sale-test.jmx \
      -l results.jtl -e -o report/
    
- name: Upload Results
  uses: actions/upload-artifact@v2
  with:
    name: jmeter-results
    path: report/
```

## Tips for Interview

When discussing this in interviews, highlight:

1. **Why 500 users for 100 stock?**
   - Simulates real flash sales (demand >> supply)
   - Tests race condition handling

2. **How to prove no overselling?**
   - Count orders in DB
   - Check Redis inventory
   - Both should match initial stock

3. **What if inventory goes negative?**
   - Rollback in `decrementInventory()`
   - This is the safety mechanism

4. **Performance bottlenecks?**
   - Database writes (solved with async)
   - Redis network latency (minimal)
   - Thread pool exhaustion (configured)
