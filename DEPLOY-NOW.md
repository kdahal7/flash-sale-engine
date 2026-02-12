# 🚀 DEPLOY YOUR FLASH SALE ENGINE FOR FREE (10 MINUTES)

## ✅ Your Code is Ready!
Your GitHub repo: https://github.com/kdahal7/flash-sale-engine

---

## 🎯 EASIEST OPTION: Render.com + Upstash Redis

### Why This Combo?
- ✅ **100% FREE Forever**
- ✅ PostgreSQL Database (FREE)
- ✅ Redis Cache (FREE 75MB from Upstash)
- ✅ No Credit Card Required
- ✅ Auto-deploys from GitHub

---

## 📝 STEP-BY-STEP GUIDE

### **STEP 1: Deploy on Render.com** (5 minutes)

1. **Go to Render.com**
   - Visit: https://render.com
   - Click **"Get Started for Free"**
   - Sign in with **GitHub**

2. **Create PostgreSQL Database**
   - Click **"New +"** → **"PostgreSQL"**
   - Name: `flashsale-db`
   - Select: **Free** plan
   - Click **"Create Database"**
   - ⏳ Wait 1-2 minutes for creation
   - **SAVE** the connection details (you'll need these!)

3. **Create Web Service**
   - Click **"New +"** → **"Web Service"**
   - Connect your repository: `kdahal7/flash-sale-engine`
   - Click **"Connect"**
   
4. **Configure Service** ⚠️ IMPORTANT!
   ```
   Name: flash-sale-engine
   Runtime: Docker
   Branch: main
   Docker Command: (leave blank)
   ```

5. **Add Environment Variables**
   Click **"Advanced"** → **"Add Environment Variable"**
   
   Add these for PostgreSQL:
   ```
   DB_HOST = [from your database connection info]
   DB_PORT = 5432
   DB_NAME = [from your database]
   DB_USER = [from your database]
   DB_PASSWORD = [from your database]
   ```
   
   **How to get these values:**
   - Go to your PostgreSQL database on Render
   - Click "Info" tab
   - Copy the values from "Internal Database URL"
   - Format: `postgresql://USER:PASSWORD@HOST/DATABASE`

6. **Click "Create Web Service"**
   - ⏳ Wait 5-8 minutes for deployment
   - ✅ Your app will be at: `https://flash-sale-engine-xxxx.onrender.com`

---

### **STEP 2: Add FREE Redis (Optional but Recommended)** (3 minutes)

1. **Go to Upstash**
   - Visit: https://upstash.com
   - Click **"Login"** → Sign in with **GitHub**

2. **Create Redis Database**
   - Click **"Create Database"**
   - Name: `flash-sale-redis`
   - Type: **Regional**
   - Region: Choose **same as your Render service** (e.g., Oregon USA)
   - Click **"Create"**

3. **Get Connection Details**
   - Click on your database name
   - Scroll to **"REST API"** section
   - You'll need:
     - **Endpoint** (looks like: `https://xxx.upstash.io`)
     - **REST Token**

4. **Update Render Environment Variables**
   - Go back to **Render dashboard**
   - Click your **Web Service**
   - Click **"Environment"** tab
   - Click **"Add Environment Variable"**
   
   Add these:
   ```
   REDIS_HOST = [your-redis-endpoint from Upstash]
   REDIS_PORT = 6379
   UPSTASH_REDIS_REST_URL = [REST API URL from Upstash]
   UPSTASH_REDIS_REST_TOKEN = [REST Token from Upstash]
   ```

5. **Redeploy**
   - Scroll down and click **"Manual Deploy"** → **"Deploy latest commit"**
   - ⏳ Wait 3-5 minutes

---

## 🎉 YOUR APP IS LIVE!

### Your Live URLs:
```
🌐 App URL: https://flash-sale-engine-xxxx.onrender.com
💚 Health: https://flash-sale-engine-xxxx.onrender.com/actuator/health
```

### Test It:
```bash
# Health Check
curl https://flash-sale-engine-xxxx.onrender.com/actuator/health

# Create Product (using PowerShell)
$product = @{name='iPhone 15'; description='Flash Sale'; price=999.99; stockCount=100} | ConvertTo-Json
Invoke-RestMethod -Uri https://flash-sale-engine-xxxx.onrender.com/api/products -Method Post -Body $product -ContentType 'application/json'

# Make Purchase
$buy = @{userId=123; quantity=1} | ConvertTo-Json
Invoke-RestMethod -Uri https://flash-sale-engine-xxxx.onrender.com/api/buy/1 -Method Post -Body $buy -ContentType 'application/json'
```

---

## 📌 ADD TO YOUR GITHUB README

Add this badge and link to your repository:

```markdown
## 🚀 Live Demo
[![Live Demo](https://img.shields.io/badge/Live%20Demo-Render-46E3B7?style=for-the-badge)](https://flash-sale-engine-xxxx.onrender.com)

Try it now: [https://flash-sale-engine-xxxx.onrender.com](https://flash-sale-engine-xxxx.onrender.com)

### Quick Test:
\`\`\`bash
# Check health
curl https://flash-sale-engine-xxxx.onrender.com/actuator/health

# API Documentation
curl https://flash-sale-engine-xxxx.onrender.com/api/health
\`\`\`
```

---

## ⚡ EVEN SIMPLER: Railway.app (One-Click)

If you want EVEN EASIER (but $5/month limit):

1. **Go to**: https://railway.app
2. **Login** with GitHub
3. **Click**: "New Project" → "Deploy from GitHub repo"
4. **Select**: `kdahal7/flash-sale-engine`
5. **Click**: "Deploy"
6. Railway will **auto-detect** and add PostgreSQL + Redis!
7. ✅ **Done in 2 minutes!**

---

## 📊 Free Tier Limits

### Render.com:
- ✅ 750 hours/month (always on!)
- ✅ Free PostgreSQL (1GB)
- ⚠️ Spins down after 15 min inactivity (30s wake up)

### Upstash Redis:
- ✅ 10,000 commands/day
- ✅ 75MB storage
- ✅ No sleep/spin-down

### Railway.app:
- ✅ $5 free credits/month
- ✅ Always on (no spin-down)
- ✅ Includes PostgreSQL + Redis
- ⚠️ Charges after $5 used

---

## 🎯 RECOMMENDATION

**For Public Demo**: Use **Render + Upstash** (100% free, just spins down)
**For Portfolio**: Use **Railway** for first month (no spin-down, better first impression)

---

## 💡 TIPS

1. **Keep it awake**: Use UptimeRobot (free) to ping your app every 5 minutes
2. **Custom domain**: Render allows free custom domains (makes it look professional)
3. **Auto-deploy**: Both platforms auto-deploy when you push to GitHub
4. **Logs**: Check deployment logs if something goes wrong

---

## 🆘 TROUBLESHOOTING

### App not starting?
- Check **Logs** in Render dashboard
- Verify all environment variables are set correctly
- Make sure Docker is selected as runtime

### Database connection error?
- Double-check DB_HOST, DB_PASSWORD, etc.
- Make sure PostgreSQL database is created first
- Try using the full DATABASE_URL instead of separate variables

### Redis error?
- If Redis fails, app will still work (just slower)
- You can deploy WITHOUT Redis initially
- Add Redis later for better performance

---

## 📞 NEED HELP?

Add this to your README so people know it's deployed:
- "🚀 **Live Demo**: [link-here]"
- "Built and deployed on Render.com"
- "Uses PostgreSQL + Redis for high-performance"

**Your deployment is ready to go! Follow the steps above and you'll be live in 10 minutes!** 🚀
