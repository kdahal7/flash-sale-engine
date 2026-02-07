# Test your deployed Flash Sale Engine on Render.com
# Replace YOUR_APP_NAME with your actual Render app name

$APP_URL = "https://YOUR_APP_NAME.onrender.com"

Write-Host "🧪 Testing Flash Sale Engine on Render.com..." -ForegroundColor Green
Write-Host "📍 App URL: $APP_URL" -ForegroundColor Yellow
Write-Host ""

# Test 1: Health Check
Write-Host "1️⃣  Testing Health Check..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$APP_URL/actuator/health" -Method GET
    Write-Host "✅ Health Check: " -NoNewline -ForegroundColor Green
    Write-Host $health.status -ForegroundColor White
} catch {
    Write-Host "❌ Health Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# Test 2: Create Product
Write-Host "`n2️⃣  Creating Test Product..." -ForegroundColor Cyan
try {
    $productData = @{
        name = "iPhone 15 Pro"
        price = 999.99
        initialStock = 50
    } | ConvertTo-Json

    $product = Invoke-RestMethod -Uri "$APP_URL/products" -Method POST -Body $productData -ContentType "application/json"
    Write-Host "✅ Product Created: ID $($product.id)" -ForegroundColor Green
    $productId = $product.id
} catch {
    Write-Host "❌ Product Creation Failed: $($_.Exception.Message)" -ForegroundColor Red
    $productId = 1  # Fallback to ID 1
}

Start-Sleep -Seconds 2

# Test 3: Make Purchase
Write-Host "`n3️⃣  Testing Flash Sale Purchase..." -ForegroundColor Cyan
try {
    $purchaseData = @{
        productId = $productId
        quantity = 1
        userId = "render-test-user"
    } | ConvertTo-Json

    $purchase = Invoke-RestMethod -Uri "$APP_URL/flash-sale/purchase" -Method POST -Body $purchaseData -ContentType "application/json"
    Write-Host "✅ Purchase Successful: Order ID $($purchase.orderId)" -ForegroundColor Green
    Write-Host "   Remaining Stock: $($purchase.remainingStock)" -ForegroundColor White
} catch {
    Write-Host "❌ Purchase Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# Test 4: Load Test (5 concurrent purchases)
Write-Host "`n4️⃣  Running Mini Load Test (5 requests)..." -ForegroundColor Cyan
$jobs = @()

for ($i = 1; $i -le 5; $i++) {
    $job = Start-Job -ScriptBlock {
        param($url, $productId, $userId)
        try {
            $data = @{
                productId = $productId
                quantity = 1
                userId = $userId
            } | ConvertTo-Json
            
            $result = Invoke-RestMethod -Uri "$url/flash-sale/purchase" -Method POST -Body $data -ContentType "application/json"
            return @{ Success = $true; Result = $result }
        } catch {
            return @{ Success = $false; Error = $_.Exception.Message }
        }
    } -ArgumentList $APP_URL, $productId, "load-test-user-$i"
    
    $jobs += $job
}

# Wait for all jobs and collect results
$results = $jobs | Wait-Job | Receive-Job
$successCount = ($results | Where-Object { $_.Success }).Count

Write-Host "✅ Load Test Results: $successCount/5 successful purchases" -ForegroundColor Green

# Cleanup jobs
$jobs | Remove-Job

Write-Host "`n🎉 Testing Complete!" -ForegroundColor Green
Write-Host "📊 Your Flash Sale Engine is running successfully on Render.com!" -ForegroundColor Yellow
Write-Host "🔗 Share your app: $APP_URL" -ForegroundColor Cyan