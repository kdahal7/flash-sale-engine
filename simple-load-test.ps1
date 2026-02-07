# 🚀 Simple Load Tester for Flash Sale Engine
# Alternative to JMeter - Uses PowerShell built-in functionality

Write-Host "`n⚡ FLASH SALE ENGINE - SIMPLE LOAD TESTER" -ForegroundColor Green -BackgroundColor Black
Write-Host "=========================================" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$concurrentUsers = 20
$requestsPerUser = 5

# Test Configuration
Write-Host "`n📊 LOAD TEST CONFIGURATION:" -ForegroundColor Cyan
Write-Host "   👥 Concurrent Users: $concurrentUsers" -ForegroundColor White
Write-Host "   🔄 Requests per User: $requestsPerUser" -ForegroundColor White
Write-Host "   🎯 Target: $baseUrl" -ForegroundColor White

# Create test product first
Write-Host "`n1️⃣ Creating Load Test Product..." -ForegroundColor Yellow
$testProduct = @{
    name = "LOAD TEST - Gaming Laptop"
    description = "High-performance laptop for load testing"
    price = "1599.99"
    stockCount = 100
} | ConvertTo-Json

try {
    $product = Invoke-RestMethod -Uri "$baseUrl/api/products" -Method POST -Body $testProduct -ContentType "application/json" -UseBasicParsing
    Write-Host "   ✅ Product Created - ID: $($product.id)" -ForegroundColor Green
    $productId = $product.id
} catch {
    Write-Host "   ❌ Failed to create product: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Function to simulate user purchases
$purchaseScript = {
    param($userId, $productId, $baseUrl, $requests)
    $results = @()
    
    for ($i = 1; $i -le $requests; $i++) {
        $startTime = Get-Date
        $purchase = @{
            userId = "LoadUser$userId"
            quantity = 1
        } | ConvertTo-Json
        
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/api/buy/$productId" -Method POST -Body $purchase -ContentType "application/json" -UseBasicParsing
            $endTime = Get-Date
            $duration = ($endTime - $startTime).TotalMilliseconds
            $results += @{
                Status = "SUCCESS"
                Duration = $duration
                User = $userId
                Request = $i
            }
        } catch {
            $endTime = Get-Date
            $duration = ($endTime - $startTime).TotalMilliseconds
            $results += @{
                Status = "FAILED"
                Duration = $duration
                User = $userId
                Request = $i
                Error = $_.Exception.Message
            }
        }
        
        Start-Sleep -Milliseconds 100
    }
    return $results
}

# Launch concurrent users
Write-Host "`n2️⃣ Launching $concurrentUsers Concurrent Users..." -ForegroundColor Yellow
$jobs = @()
$startTime = Get-Date

for ($user = 1; $user -le $concurrentUsers; $user++) {
    $job = Start-Job -ScriptBlock $purchaseScript -ArgumentList $user, $productId, $baseUrl, $requestsPerUser
    $jobs += $job
    Write-Host "   👤 Launched User $user" -ForegroundColor White
}

Write-Host "`n3️⃣ Load Test Running..." -ForegroundColor Yellow
Write-Host "   ⏱️  Please wait while $concurrentUsers users make purchases..." -ForegroundColor Cyan

# Wait for all jobs to complete
$allResults = @()
$completed = 0
foreach ($job in $jobs) {
    $results = Wait-Job $job | Receive-Job
    $allResults += $results
    $completed++
    Write-Host "   ✅ User $completed completed" -ForegroundColor Green
}

$endTime = Get-Date
$totalDuration = ($endTime - $startTime).TotalSeconds

# Clean up jobs
$jobs | Remove-Job

# Analyze results
Write-Host "`n4️⃣ LOAD TEST RESULTS:" -ForegroundColor Cyan
$successfulRequests = ($allResults | Where-Object { $_.Status -eq "SUCCESS" } | Measure-Object).Count
$failedRequests = ($allResults | Where-Object { $_.Status -eq "FAILED" } | Measure-Object).Count
$totalRequests = $allResults.Count
$averageResponseTime = ($allResults | Measure-Object -Property Duration -Average).Average
$maxResponseTime = ($allResults | Measure-Object -Property Duration -Maximum).Maximum
$minResponseTime = ($allResults | Measure-Object -Property Duration -Minimum).Minimum

Write-Host "   📊 Total Requests: $totalRequests" -ForegroundColor White
Write-Host "   ✅ Successful: $successfulRequests" -ForegroundColor Green
Write-Host "   ❌ Failed: $failedRequests" -ForegroundColor Red
Write-Host "   📈 Success Rate: $([math]::Round(($successfulRequests/$totalRequests)*100, 2))%" -ForegroundColor $(if(($successfulRequests/$totalRequests) -gt 0.95) {"Green"} else {"Yellow"})
Write-Host "   ⚡ Avg Response Time: $([math]::Round($averageResponseTime, 0))ms" -ForegroundColor White
Write-Host "   🔥 Max Response Time: $([math]::Round($maxResponseTime, 0))ms" -ForegroundColor White
Write-Host "   💚 Min Response Time: $([math]::Round($minResponseTime, 0))ms" -ForegroundColor White
Write-Host "   ⏱️  Total Test Duration: $([math]::Round($totalDuration, 2))s" -ForegroundColor White
Write-Host "   🚀 Requests/Second: $([math]::Round($totalRequests/$totalDuration, 2))" -ForegroundColor Cyan

# Check final inventory
Write-Host "`n5️⃣ Final Inventory Check:" -ForegroundColor Yellow
try {
    $finalProduct = Invoke-RestMethod -Uri "$baseUrl/api/products/$productId" -UseBasicParsing
    $unitsSold = 100 - $finalProduct.stockCount
    Write-Host "   📦 Units Sold: $unitsSold out of 100" -ForegroundColor Green
    Write-Host "   📊 Remaining Stock: $($finalProduct.stockCount)" -ForegroundColor White
} catch {
    Write-Host "   ❌ Error checking inventory" -ForegroundColor Red
}

Write-Host "`n🎉 LOAD TEST COMPLETE!" -ForegroundColor Green -BackgroundColor Black
if ($successfulRequests -gt ($totalRequests * 0.95)) {
    Write-Host "✅ EXCELLENT PERFORMANCE - Your Flash Sale Engine is ready for production!" -ForegroundColor Green
} elseif ($successfulRequests -gt ($totalRequests * 0.8)) {
    Write-Host "⚠️ GOOD PERFORMANCE - Some optimization may be beneficial" -ForegroundColor Yellow
} else {
    Write-Host "❌ PERFORMANCE ISSUES - Check system resources and configuration" -ForegroundColor Red
}