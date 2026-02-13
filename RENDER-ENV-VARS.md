# Render Environment Variables Setup

## Required Environment Variables

Go to your Render dashboard → flash-sale-engine → **Environment** tab and add these:

### Database (PostgreSQL)
If Render auto-created your PostgreSQL database, it should provide `DATABASE_URL` automatically.

**If using internal Render PostgreSQL:**
- ✅ `DATABASE_URL` - Auto-filled by Render (e.g., `postgres://user:pass@host/dbname`)

**If DATABASE_URL is not set, use these separate variables:**
- `DB_HOST` - Your PostgreSQL host
- `DB_PORT` - `5432`
- `DB_NAME` - Database name
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password

### Redis (Upstash)
- `REDIS_HOST` - `modern-tarpon-54714.upstash.io`
- `REDIS_PORT` - `6380` ⚠️ **MUST be 6380 for SSL**, not 6379
- `REDIS_PASSWORD` - Your Upstash password
- `REDIS_SSL` - `true`

### Server
- `PORT` - Auto-set by Render (usually `10000`)

## How to Check if App is Running

1. Go to **Logs** tab in Render
2. Look for: `Started FlashSaleApplication in X.XXX seconds`
3. Test endpoint: `https://flash-sale-engine-1.onrender.com/api/health`

## Common Issues

### Issue 1: Redis Connection Timeout
**Error:** `RedisConnectionException: Unable to connect`
**Fix:** 
- Make sure `REDIS_PORT=6380` (NOT 6379)
- Make sure `REDIS_SSL=true`

### Issue 2: Database Connection Failed
**Error:** `Connection refused` or `Authentication failed`
**Fix:**
- Verify `DATABASE_URL` is set correctly
- Make sure PostgreSQL add-on is linked to your web service

### Issue 3: App Not Starting
**Check:** 
- Build logs for compilation errors
- Runtime logs for startup errors
- Make sure `PORT` environment variable is not overridden

## Testing After Deployment

```bash
# Health check
curl https://flash-sale-engine-1.onrender.com/api/health

# Should return: "Flash Sale Engine is running!"
```

## Frontend Access

Once deployed, frontend is available at:
**https://flash-sale-engine-1.onrender.com/**

No `/index.html` needed - Spring Boot serves it automatically from root.
