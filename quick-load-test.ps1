# Simple Load Test - JMeter Alternative
Write-Host "`n** SIMPLE LOAD TEST (JMeter Alternative) **" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# Create test product
Write-Host "`n1. Creating test product..." -ForegroundColor Yellow
$product = @{
    name = "Load Test Product"
    description = "Testing high concurrency" 
    price = "99.99"
    stockCount = 50
} | ConvertTo-Json

try {
    $created = Invoke-RestMethod -Uri "$baseUrl/api/products" -Method POST -Body $product -ContentType "application/json" -UseBasicParsing
    Write-Host "   SUCCESS - Product ID: $($created.id)" -ForegroundColor Green
    $productId = $created.id
} catch {
    Write-Host "   ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Run load test
Write-Host "`n2. Running 10 concurrent purchases..." -ForegroundColor Yellow
$successCount = 0
$failCount = 0
$times = @()

for($i = 1; $i -le 10; $i++) {
    $startTime = Get-Date
    $purchase = @{ userId = "LoadUser$i"; quantity = 1 } | ConvertTo-Json
    
    try {
        $result = Invoke-RestMethod -Uri "$baseUrl/api/buy/$productId" -Method POST -Body $purchase -ContentType "application/json" -UseBasicParsing
        $endTime = Get-Date
        $duration = ($endTime - $startTime).TotalMilliseconds
        $times += $duration
        Write-Host "   SUCCESS User ${i} - Response: $([math]::Round($duration))ms" -ForegroundColor Green
        $successCount++
    } catch {
        Write-Host "   FAILED User ${i}" -ForegroundColor Red
        $failCount++
    }
}

# Results
Write-Host "`n3. RESULTS:" -ForegroundColor Cyan
Write-Host "   Summary: $successCount Success | $failCount Failed" -ForegroundColor White
if($times.Count -gt 0) {
    $avgTime = ($times | Measure-Object -Average).Average
    Write-Host "   Avg Response Time: $([math]::Round($avgTime))ms" -ForegroundColor White
}

Write-Host "`nLoad test complete!" -ForegroundColor Green