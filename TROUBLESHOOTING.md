# 🔧 Troubleshooting Guide - Flash Sale Engine

Common issues and their solutions.

---

## 🚀 Application Won't Start

### Issue: Port 8080 already in use

**Symptoms**:
```
Web server failed to start. Port 8080 was already in use.
```

**Solution 1 - Kill the process**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <pid> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

**Solution 2 - Change port**:

Edit `application.yml`:
```yaml
server:
  port: 8081  # Changed from 8080
```

---

### Issue: Cannot connect to PostgreSQL

**Symptoms**:
```
Unable to acquire JDBC Connection
Connection refused: localhost:5432
```

**Solutions**:

**Check if PostgreSQL is running**:
```bash
docker ps | grep postgres
```

**Start PostgreSQL**:
```bash
docker-compose up -d postgres
```

**Check PostgreSQL logs**:
```bash
docker-compose logs postgres
```

**Test connection manually**:
```bash
psql -h localhost -U postgres -d flashsale -W
# Password: postgres
```

**Verify credentials in application.yml**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/flashsale
    username: postgres
    password: postgres  # Check this matches
```

---

### Issue: Cannot connect to Redis

**Symptoms**:
```
Unable to connect to Redis
io.lettuce.core.RedisConnectionException
```

**Solutions**:

**Check if Redis is running**:
```bash
docker ps | grep redis
```

**Start Redis**:
```bash
docker-compose up -d redis
```

**Test Redis connection**:
```bash
redis-cli ping
# Should return: PONG
```

**Check Redis configuration**:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:  # Leave empty for local
```

---

### Issue: Maven build fails

**Symptoms**:
```
[ERROR] Failed to execute goal
```

**Solutions**:

**Clean build**:
```bash
mvn clean install
```

**Skip tests temporarily**:
```bash
mvn clean install -DskipTests
```

**Check Java version**:
```bash
java -version
# Must be 17 or higher
```

**Update Maven**:
```bash
mvn --version
# Should be 3.8+
```

---

## 🐳 Docker Issues

### Issue: Docker containers won't start

**Symptoms**:
```
ERROR: Service 'postgres' failed to build
```

**Solutions**:

**Remove old containers**:
```bash
docker-compose down
docker-compose up -d
```

**Remove volumes (WARNING: deletes data)**:
```bash
docker-compose down -v
docker-compose up -d
```

**Check Docker is running**:
```bash
docker ps
```

**Check Docker Compose version**:
```bash
docker-compose --version
# Should be 1.29+
```

---

### Issue: Out of disk space

**Symptoms**:
```
no space left on device
```

**Solutions**:

**Clean Docker**:
```bash
docker system prune -a
docker volume prune
```

**Check disk space**:
```bash
df -h  # Linux/Mac
```

---

## 🔥 Runtime Errors

### Issue: All purchases return "Out of Stock"

**Symptoms**:
- First purchase fails
- Redis shows stock = 0 or negative

**Solutions**:

**Check if inventory synced to Redis**:
```bash
redis-cli GET product:1:stock
```

**Sync inventory manually**:
```bash
curl -X POST http://localhost:8080/api/products/1/sync-redis
```

**Check product stock in database**:
```sql
SELECT id, name, stock_count FROM products WHERE id = 1;
```

**Recreate product with stock**:
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","price":99.99,"stockCount":100}'
```

---

### Issue: Purchases succeed but orders not in database

**Symptoms**:
- API returns success
- Orders table is empty

**Solutions**:

**Check async thread pool**:
```bash
# Check application logs for errors
tail -f logs/application.log | grep "Failed to save order"
```

**Check database connection during save**:
```sql
SELECT COUNT(*) FROM orders;
```

**Try synchronous save (for debugging)**:

Comment out `@Async` in `OrderService.java`:
```java
// @Async("orderProcessingExecutor")  // Comment this
public CompletableFuture<Order> saveOrderAsync(Order order) {
```

**Check application logs**:
```bash
docker-compose logs app | grep ERROR
```

---

### Issue: Rate limiting not working

**Symptoms**:
- Can make unlimited requests
- No rate limit errors

**Solutions**:

**Check configuration**:
```yaml
flashsale:
  rate-limit:
    enabled: true  # Must be true
    max-requests: 5
    window-seconds: 1
```

**Check Redis keys**:
```bash
redis-cli KEYS "rate_limit:*"
```

**Test rate limit**:
```bash
# Send 10 requests quickly
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/buy/1 \
    -H "Content-Type: application/json" \
    -d '{"userId":"testuser"}' &
done
wait
# Some should return rate limit error
```

---

### Issue: Inventory mismatch (Redis vs Database)

**Symptoms**:
- Redis shows different stock than database
- Orders count doesn't match stock decrease

**Solutions**:

**Check both sources**:
```bash
# Redis
redis-cli GET product:1:stock

# Database
psql -h localhost -U postgres -d flashsale -c "SELECT stock_count FROM products WHERE id = 1"

# Orders count
psql -h localhost -U postgres -d flashsale -c "SELECT COUNT(*) FROM orders WHERE product_id = 1"
```

**Reconcile**:
```bash
# Option 1: Sync DB to Redis
curl -X POST http://localhost:8080/api/products/1/sync-redis

# Option 2: Update DB to match Redis
# Manual SQL update
```

---

## 🧪 Load Testing Issues

### Issue: JMeter not found

**Symptoms**:
```
jmeter: command not found
```

**Solutions**:

**Install JMeter**:
```bash
# Download from https://jmeter.apache.org/download_jmeter.cgi
# Extract and add to PATH

# Or use package manager
# macOS
brew install jmeter

# Windows (Chocolatey)
choco install jmeter
```

**Run with full path**:
```bash
/path/to/jmeter/bin/jmeter -n -t flash-sale-test.jmx -l results.jtl
```

---

### Issue: Load test shows more than expected orders

**Symptoms**:
- Started with 100 stock
- Got 105 orders (OVERSELLING!)

**This is a BUG! Solutions**:

**Verify Redis DECR is being used**:
Check `InventoryService.java`:
```java
// MUST use decrement, not get-then-subtract
Long remaining = redisTemplate.opsForValue().decrement(key);
```

**Check for race conditions in code**:
Look for any code that does:
```java
// BAD - has race condition
int stock = getStock();
if (stock > 0) {
    setStock(stock - 1);  // RACE CONDITION!
}
```

**Verify rollback works**:
```java
if (remaining < 0) {
    redisTemplate.opsForValue().increment(key);  // Must rollback
    return -1L;
}
```

---

### Issue: JMeter test hangs

**Symptoms**:
- Test starts but never completes
- Application stops responding

**Solutions**:

**Check application logs**:
```bash
docker-compose logs app
```

**Reduce concurrent users**:
Edit JMX file: Change `NUM_USERS` from 500 to 50

**Increase timeout**:
```yaml
spring:
  redis:
    timeout: 5000  # Increase from 2000
```

**Check thread pool exhaustion**:
```yaml
server:
  tomcat:
    threads:
      max: 200  # Increase if needed
```

---

## ☸️ Kubernetes Issues

### Issue: Pods not starting

**Symptoms**:
```
kubectl get pods shows CrashLoopBackOff
```

**Solutions**:

**Check pod logs**:
```bash
kubectl logs <pod-name> -n flash-sale
```

**Describe pod for events**:
```bash
kubectl describe pod <pod-name> -n flash-sale
```

**Check secrets**:
```bash
kubectl get secrets -n flash-sale
```

**Verify environment variables**:
```bash
kubectl exec <pod-name> -n flash-sale -- env | grep DATABASE
```

---

### Issue: Service has no external IP

**Symptoms**:
```
kubectl get svc shows <pending> for EXTERNAL-IP
```

**Solutions**:

**Wait (can take 5-10 minutes)**:
```bash
kubectl get svc -n flash-sale --watch
```

**Check LoadBalancer support**:
```bash
# On local K8s (minikube), use NodePort instead
kubectl edit svc flash-sale-service -n flash-sale
# Change type: LoadBalancer to type: NodePort
```

**Use port-forward for testing**:
```bash
kubectl port-forward svc/flash-sale-service 8080:80 -n flash-sale
```

---

## 🔒 Security Issues

### Issue: Cannot connect with SSL/TLS

**Symptoms**:
```
SSL handshake failed
```

**Solutions**:

**For PostgreSQL (Neon.tech)**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://host/db?sslmode=require
```

**For Redis (Upstash)**:
```yaml
spring:
  redis:
    ssl: true
```

**Disable SSL for testing** (NOT for production):
```yaml
spring:
  redis:
    ssl: false
```

---

## 📊 Performance Issues

### Issue: Slow response times (>500ms)

**Solutions**:

**Check Redis latency**:
```bash
redis-cli --latency
```

**Check database connection pool**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Increase if needed
```

**Check async thread pool**:
```java
executor.setCorePoolSize(20);  // Increase from 10
executor.setMaxPoolSize(40);   // Increase from 20
```

**Add indexes** (if missing):
```sql
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_product_id ON orders(product_id);
```

---

### Issue: High memory usage

**Solutions**:

**Increase JVM memory**:
```bash
JAVA_OPTS="-Xms512m -Xmx2048m"
mvn spring-boot:run
```

**Check for memory leaks**:
```bash
jcmd <pid> GC.heap_info
```

**Enable G1GC**:
```bash
JAVA_OPTS="-XX:+UseG1GC"
```

---

## 🐛 Common Mistakes

### Mistake 1: Forgot to sync to Redis

**Symptom**: All purchases return "Product not found" or "Out of stock"

**Solution**: Always run after creating product:
```bash
curl -X POST http://localhost:8080/api/products/1/sync-redis
```

---

### Mistake 2: Using wrong product ID

**Symptom**: 404 Product not found

**Solution**: Check created product ID:
```bash
curl http://localhost:8080/api/products
# Find the ID from response
```

---

### Mistake 3: Docker containers stopped

**Symptom**: Connection refused errors

**Solution**: Restart containers:
```bash
docker-compose up -d
```

---

### Mistake 4: Wrong database credentials

**Symptom**: Authentication failed

**Solution**: Check `.env` or `application.yml`:
```yaml
spring:
  datasource:
    username: postgres  # Default
    password: postgres  # Default for local
```

---

## 📞 Getting Help

### Debug Checklist

Before asking for help, check:

1. [ ] Application logs: `tail -f logs/application.log`
2. [ ] Docker logs: `docker-compose logs`
3. [ ] Redis accessible: `redis-cli ping`
4. [ ] PostgreSQL accessible: `psql -h localhost -U postgres`
5. [ ] Ports available: `netstat -an | grep 8080`
6. [ ] Correct Java version: `java -version`
7. [ ] Recent changes: `git diff`

### Collect Debug Info

```bash
# System info
java -version
mvn -version
docker --version

# Application status
curl http://localhost:8080/actuator/health

# Redis status
redis-cli INFO

# Database status
psql -h localhost -U postgres -c "SELECT version();"

# Recent logs
tail -n 50 logs/application.log
```

---

## 🎯 Prevention Tips

1. **Always check health before testing**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Keep backups**:
   ```bash
   docker-compose exec postgres pg_dump -U postgres flashsale > backup.sql
   ```

3. **Monitor resources**:
   ```bash
   docker stats
   ```

4. **Test locally first** before deploying

5. **Read logs regularly** to catch issues early

---

## 🚨 Emergency Reset

If everything is broken, nuclear option:

```bash
# Stop everything
docker-compose down -v
pkill -f "spring-boot:run"

# Clean all
mvn clean
rm -rf target/

# Rebuild from scratch
mvn clean install
docker-compose up -d
mvn spring-boot:run

# Recreate data
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","price":99.99,"stockCount":100}'
  
curl -X POST http://localhost:8080/api/products/1/sync-redis
```

---

**Still stuck? Check:**
- [README.md](README.md) for project overview
- [QUICKSTART.md](QUICKSTART.md) for setup steps
- [DEPLOYMENT.md](DEPLOYMENT.md) for deployment issues
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for API help

---

**Good luck! 🚀**
