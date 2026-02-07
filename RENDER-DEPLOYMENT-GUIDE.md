# 🚀 Deploy Flash Sale Engine on Render.com (FREE)

## ⚠️ CRITICAL: Render Auto-Detection Issue
**If you see "Using Node.js version 22.22.0" and "mvn: command not found":**
- Render is incorrectly detecting your Java project as Node.js
- **SOLUTION**: Manually select **"Docker"** runtime (Step 2 below)
- **DO NOT** rely on auto-detection - it fails for this project

## Why Render.com is BETTER than Railway:
- ✅ **750 hours/month FREE** (vs Railway's 500)
- ✅ **Free PostgreSQL database** included
- ✅ **No credit card required** initially
- ✅ **Automatic deployments** from GitHub
- ✅ **Better reliability** and uptime

---

## 📋 Step-by-Step Deployment Guide

### Step 1: Create Render Account
1. Go to **https://render.com**
2. Click **"Get Started for Free"**
3. Sign up with **GitHub** (easiest option)
4. Authorize Render to access your repositories

### Step 2: Create Web Service (MANUAL CONFIG REQUIRED)

🚨 **CRITICAL: Auto-detection fails, use manual setup:**

1. After login, click **"New +"** → **"Web Service"**
2. Connect your **GitHub repository**: `https://github.com/kdahal7/flash-sale-engine`
3. **IMMEDIATELY** scroll down and click **"Advanced"**
4. **Change Runtime** from "Node.js" to **"Docker"**
5. Configure these settings:

```
Name: flash-sale-engine
Runtime: Docker (MUST select this!)
Branch: main
Root Directory: (leave blank)
Dockerfile Path: ./Dockerfile
```

**⚠️ If you don't change to Docker, it will fail with "mvn: command not found"**

### Step 3: Configure Environment Variables
**BEFORE** creating the service, in the **Environment Variables** section, add:

```
Key: JAVA_OPTS
Value: -Xmx512m -Dspring.profiles.active=prod
```

### Step 4: Create PostgreSQL Database
1. Click **"New +"** → **"PostgreSQL"**
2. Configure:
```
Name: flashsale-db
Database Name: flashsale
User: flashsale_user
Region: (same as your web service)
Plan: Free
```

### Step 5: Connect Database to App
1. **Find your database URL:**
   - Go to your **PostgreSQL service** (flashsale-db)
   - Click on the database name
   - Look for **"Connections"** section
   - Copy the **"External Database URL"** (starts with `postgresql://`)
   
2. **Add to your Web Service:**
   - Go back to your **Web Service** (flash-sale-engine)
   - Click **"Environment"** tab
   - Add new environment variable:
```
Key: DATABASE_URL
Value: postgresql://flashsale_user:XXXXX@dpg-XXXXX-a.oregon-postgres.render.com/flashsale
```
   *(Use your actual URL from step 1)*

---

## 📍 **WHERE TO FIND DATABASE URL (Visual Guide)**

### In Your Render Dashboard:
1. **Left sidebar** → Click your **PostgreSQL service** name
2. **Overview tab** → Scroll to **"Connections"** 
3. **External Database URL** → Click **"Copy"** button
4. **Format looks like**: `postgresql://user:password@host.render.com/dbname`

### Then Paste It Into Web Service:
1. **Left sidebar** → Click your **Web Service** name  
2. **Environment tab** → **"Add Environment Variable"**
3. **Key**: `DATABASE_URL`
4. **Value**: *paste the copied URL*
5. **Save Changes**

---

### Step 6: Deploy! 🎉
1. Click **"Create Web Service"**
2. **Watch the logs** - should show "Building using Dockerfile"
3. Wait 5-8 minutes for Docker build
4. Your app will be live at: `https://your-service-name.onrender.com`

---

## 🔄 ALTERNATIVE: Native Java Environment

If Docker approach still fails, try Native Java:

1. **Create new Web Service**
2. **Runtime**: Native  
3. **Build Command**: `./mvnw clean package -DskipTests` 
4. **Start Command**: `java -Dserver.port=$PORT -jar target/flash-sale-engine-1.0.0.jar`
5. **Add Environment Variable**:
   - Key: `JAVA_VERSION` 
   - Value: `17`

---

## 🧪 Testing Your Deployment

### 1. Health Check
```bash
curl https://your-app-name.onrender.com/api/actuator/health
```

### 2. Create Product
```bash
curl -X POST "https://your-app-name.onrender.com/api/products" \
  -H "Content-Type: application/json" \
  -d '{"name": "iPhone 15", "price": 999.99, "initialStock": 100}'
```

### 3. Test Purchase
```bash
curl -X POST "https://your-app-name.onrender.com/api/flash-sale/purchase" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 1, "userId": "user123"}'
```

---

## 🎯 Expected Results

### ✅ Successful Health Check:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### ✅ Successful Purchase:
```json
{
  "success": true,
  "message": "Purchase successful",
  "orderId": "12345",
  "remainingStock": 99
}
```

---

## 🔧 Troubleshooting

### If Build Fails:
1. Check **"Logs"** tab in Render dashboard
2. Common issues:
   - **"mvn: command not found"** → Use Docker runtime (not Node.js)
   - **Java version mismatch** → Dockerfile uses OpenJDK 17
   - **Missing environment variables** → Add DATABASE_URL
   - **Database connection issues** → Verify database is "Available"

### If Database Connection Fails:
1. Verify `DATABASE_URL` is correctly set
2. Ensure database and web service are in same region
3. Check database is "Available" status

### Performance Issues:
- Free tier has limited resources
- App may sleep after 15 minutes of inactivity
- First request after sleep takes 30-60 seconds

---

## 🎉 Success Indicators

1. **Build logs show**: `BUILD SUCCESS`
2. **App logs show**: `Started FlashSaleApplication`
3. **Health endpoint returns**: `HTTP 200`
4. **Database connected**: No connection errors in logs

---

## 💡 Pro Tips

1. **Monitor logs** regularly in Render dashboard
2. **Database backup** is automatic on free tier
3. **Custom domain** available on paid plans
4. **Upgrade to paid** for better performance and no sleep

---

## 📞 Need Help?

- **Render Docs**: https://render.com/docs
- **Community Forum**: https://community.render.com
- **Your GitHub Repo**: https://github.com/kdahal7/flash-sale-engine

---

**🎯 Your app is now deployed and ready for global access! Share your live URL with the world! 🌍**