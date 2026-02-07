# 🚀 Deployment Guide

This guide covers multiple deployment options for the Flash Sale Engine.

---

## Table of Contents
1. [Local Development](#local-development)
2. [Docker Deployment](#docker-deployment)
3. [Kubernetes/AKS](#kubernetes-deployment)
4. [Cloud Platforms](#cloud-platforms)
5. [Production Checklist](#production-checklist)

---

## Local Development

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Steps
```bash
# 1. Clone the repository
git clone <your-repo-url>
cd flash-sale-engine

# 2. Start infrastructure
docker-compose up -d

# 3. Build and run
mvn clean install
mvn spring-boot:run

# 4. Verify
curl http://localhost:8080/actuator/health
```

---

## Docker Deployment

### Build and Run

```bash
# 1. Build the JAR
mvn clean package -DskipTests

# 2. Build Docker image
docker build -t flash-sale-engine:latest .

# 3. Run with Docker Compose
docker-compose -f docker-compose.prod.yml up -d

# 4. View logs
docker-compose logs -f app

# 5. Scale the application
docker-compose up -d --scale app=3
```

### Environment Configuration

Create `.env` file:
```env
DATABASE_URL=jdbc:postgresql://postgres:5432/flashsale
DATABASE_USER=postgres
DATABASE_PASSWORD=your-secure-password
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

---

## Kubernetes Deployment

### Prerequisites
- kubectl configured
- Kubernetes cluster (AKS, EKS, GKE, or local)

### Deploy

```bash
# 1. Create namespace
kubectl create namespace flash-sale

# 2. Create secrets
kubectl create secret generic flash-sale-secrets \
  --from-literal=database-url='jdbc:postgresql://...' \
  --from-literal=database-user='your-user' \
  --from-literal=database-password='your-password' \
  --from-literal=redis-password='your-redis-password' \
  -n flash-sale

# 3. Apply configurations
kubectl apply -f k8s/config.yml -n flash-sale
kubectl apply -f k8s/deployment.yml -n flash-sale

# 4. Check status
kubectl get pods -n flash-sale
kubectl get svc -n flash-sale

# 5. Get external IP
kubectl get svc flash-sale-service -n flash-sale
```

### Scaling

```bash
# Manual scaling
kubectl scale deployment flash-sale-app --replicas=5 -n flash-sale

# Auto-scaling is configured via HPA in deployment.yml
kubectl get hpa -n flash-sale
```

---

## Cloud Platforms

### Azure (AKS + Neon.tech + Upstash)

#### 1. Create Resources

```bash
# Create resource group
az group create --name flash-sale-rg --location eastus

# Create AKS cluster
az aks create \
  --resource-group flash-sale-rg \
  --name flash-sale-aks \
  --node-count 3 \
  --enable-addons monitoring \
  --generate-ssh-keys

# Get credentials
az aks get-credentials --resource-group flash-sale-rg --name flash-sale-aks
```

#### 2. Setup External Services

**Neon.tech (PostgreSQL)**:
1. Sign up at https://neon.tech
2. Create a new project
3. Copy connection string

**Upstash (Redis)**:
1. Sign up at https://upstash.com
2. Create a Redis database
3. Copy host, port, and password

#### 3. Configure Application

Update `k8s/config.yml` with your values:
```yaml
data:
  redis-host: "your-redis.upstash.io"
  redis-port: "6379"

stringData:
  database-url: "jdbc:postgresql://your-project.neon.tech/flashsale?sslmode=require"
  redis-password: "your-upstash-password"
```

#### 4. Deploy

```bash
kubectl apply -f k8s/ -n flash-sale
```

---

### AWS (ECS + RDS + ElastiCache)

#### 1. Create Infrastructure

```bash
# Create VPC and subnets (use CloudFormation or Terraform)

# Create RDS PostgreSQL
aws rds create-db-instance \
  --db-instance-identifier flash-sale-db \
  --db-instance-class db.t3.medium \
  --engine postgres \
  --master-username postgres \
  --master-user-password YourPassword \
  --allocated-storage 20

# Create ElastiCache Redis
aws elasticache create-cache-cluster \
  --cache-cluster-id flash-sale-redis \
  --cache-node-type cache.t3.medium \
  --engine redis \
  --num-cache-nodes 1
```

#### 2. Build and Push Image

```bash
# Push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

docker tag flash-sale-engine:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/flash-sale-engine:latest

docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/flash-sale-engine:latest
```

#### 3. Create ECS Task Definition

Use AWS Console or CLI to create task with:
- Image: Your ECR image
- Environment variables from RDS and ElastiCache endpoints
- Port mapping: 8080

---

### Heroku (Quick Deploy)

```bash
# 1. Create app
heroku create flash-sale-engine

# 2. Add addons
heroku addons:create heroku-postgresql:standard-0
heroku addons:create heroku-redis:premium-0

# 3. Set buildpack
heroku buildpacks:set heroku/java

# 4. Deploy
git push heroku main

# 5. Scale
heroku ps:scale web=3

# 6. Open app
heroku open
```

---

## Production Checklist

### Security
- [ ] Use strong passwords for DB and Redis
- [ ] Enable SSL/TLS for all connections
- [ ] Set up firewall rules
- [ ] Use secrets management (Azure Key Vault, AWS Secrets Manager)
- [ ] Enable HTTPS/TLS for API endpoints
- [ ] Implement API authentication (OAuth2/JWT)

### Performance
- [ ] Configure connection pooling
- [ ] Set appropriate JVM memory settings
- [ ] Enable Redis persistence (AOF or RDB)
- [ ] Configure auto-scaling rules
- [ ] Set up CDN for static content (if any)

### Monitoring
- [ ] Set up Prometheus + Grafana
- [ ] Configure alerts for errors and latency
- [ ] Enable application logs aggregation
- [ ] Monitor Redis and PostgreSQL metrics
- [ ] Set up uptime monitoring

### Reliability
- [ ] Configure health checks
- [ ] Set up database backups
- [ ] Implement circuit breakers
- [ ] Configure retry policies
- [ ] Test disaster recovery procedures

### Testing
- [ ] Run load tests with JMeter
- [ ] Verify no overselling under load
- [ ] Test auto-scaling behavior
- [ ] Validate rate limiting
- [ ] Test database failover

---

## Environment Variables Reference

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | Yes | - |
| `DATABASE_USER` | Database username | Yes | - |
| `DATABASE_PASSWORD` | Database password | Yes | - |
| `REDIS_HOST` | Redis host | Yes | - |
| `REDIS_PORT` | Redis port | Yes | 6379 |
| `REDIS_PASSWORD` | Redis password | No | - |
| `REDIS_SSL` | Enable Redis SSL | No | false |
| `PORT` | Application port | No | 8080 |
| `JAVA_OPTS` | JVM options | No | -Xms512m -Xmx1024m |
| `RATE_LIMIT_ENABLED` | Enable rate limiting | No | true |
| `RATE_LIMIT_MAX_REQUESTS` | Max requests per window | No | 5 |
| `RATE_LIMIT_WINDOW_SECONDS` | Rate limit window | No | 1 |

---

## Troubleshooting

### Application won't start
```bash
# Check logs
docker-compose logs app
kubectl logs <pod-name> -n flash-sale

# Verify environment variables
docker exec flash-sale-app env
```

### Database connection issues
```bash
# Test connection
psql -h <host> -U <user> -d flashsale

# Check firewall rules
telnet <db-host> 5432
```

### Redis connection issues
```bash
# Test Redis connection
redis-cli -h <host> -p 6379 -a <password> ping

# Check Redis info
redis-cli -h <host> -p 6379 -a <password> info
```

### Performance issues
```bash
# Check metrics
curl http://localhost:8080/actuator/prometheus

# Monitor JVM
jcmd <pid> VM.info

# Check database slow queries
SELECT * FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;
```

---

## Support

For issues and questions:
1. Check application logs
2. Review [README.md](../README.md)
3. Open an issue on GitHub

---

**Built with ❤️ for high-concurrency scenarios**
