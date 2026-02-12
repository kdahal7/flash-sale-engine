# Flash Sale Engine API Test Script
# Run this script to test the Flash Sale Engine

$baseUrl = "http://localhost:8080"

Write-Host "`n=== FLASH SALE ENGINE TEST SUITE ===" -ForegroundColor Magenta
Write-Host "Base URL: $baseUrl`n" -ForegroundColor Yellow

# Test 1: Health Check
Write-Host "1. Health Check" -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    Write-Host "   Status: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "   Failed: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Test 2: Create Product
Write-Host "`n2. Creating Test Product" -ForegroundColor Cyan
$product = @{
    name = "Limited Edition Sneakers"
    description = "Flash Sale - Only 50 available!"
    price = 199.99
    stockCount = 50
} | ConvertTo-Json

try {
    $newProduct = Invoke-RestMethod -Uri "$baseUrl/api/products" -Method Post -Body $product -ContentType 'application/json'
    $productId = $newProduct.id
    Write-Host "   Created Product ID: $productId" -ForegroundColor Green
    Write-Host "   Name: $($newProduct.name)" -ForegroundColor White
    Write-Host "   Stock: $($newProduct.stockCount)" -ForegroundColor White
} catch {
    Write-Host "   Failed: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Test 3: Get Product Details
Write-Host "`n3. Getting Product Details" -ForegroundColor Cyan
try {
    $productDetails = Invoke-RestMethod -Uri "$baseUrl/api/products/$productId" -Method Get
    Write-Host "   DB Stock: $($productDetails.dbStock)" -ForegroundColor White
    Write-Host "   Redis Stock: $($productDetails.redisStock)" -ForegroundColor White
} catch {
    Write-Host "   Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Make Single Purchase
Write-Host "`n4. Making Single Purchase" -ForegroundColor Cyan
$purchase = @{
    userId = 1001
    quantity = 1
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri "$baseUrl/api/buy/$productId" -Method Post -Body $purchase -ContentType 'application/json'
    Write-Host "   Success: $($result.success)" -ForegroundColor Green
    Write-Host "   Order ID: $($result.orderId)" -ForegroundColor White
    Write-Host "   Message: $($result.message)" -ForegroundColor White
} catch {
    Write-Host "   Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Concurrent Purchases (Simulate Flash Sale)
Write-Host "`n5. Simulating 10 Concurrent Purchases (Flash Sale)" -ForegroundColor Cyan
$jobs = 1..10 | ForEach-Object {
    Start-Job -ScriptBlock {
        param($baseUrl, $productId, $userId)
        $body = @{userId=$userId; quantity=1} | ConvertTo-Json
        try {
            Invoke-RestMethod -Uri "$baseUrl/api/buy/$productId" -Method Post -Body $body -ContentType 'application/json'
        } catch {
            @{success=$false; message=$_.Exception.Message}
        }
    } -ArgumentList $baseUrl, $productId, (1000 + $_)
}

Wait-Job $jobs | Out-Null
$results = $jobs | Receive-Job
$successful = ($results | Where-Object {$_.success -eq $true}).Count
$failed = $results.Count - $successful

Write-Host "   Total Requests: $($results.Count)" -ForegroundColor White
Write-Host "   Successful: $successful" -ForegroundColor Green
Write-Host "   Failed: $failed" -ForegroundColor $(if($failed -gt 0){"Yellow"}else{"Green"})
$jobs | Remove-Job

# Test 6: Final Stock Check
Write-Host "`n6. Final Stock Check" -ForegroundColor Cyan
try {
    $finalStock = Invoke-RestMethod -Uri "$baseUrl/api/products/$productId" -Method Get
    Write-Host "   Initial Stock: 50" -ForegroundColor White
    Write-Host "   Current Stock: $($finalStock.redisStock)" -ForegroundColor White
    Write-Host "   Items Sold: $(50 - $finalStock.redisStock)" -ForegroundColor Green
} catch {
    Write-Host "   Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== TEST COMPLETE ===" -ForegroundColor Magenta
Write-Host "`nAPI Endpoints Available:" -ForegroundColor Yellow
Write-Host "  POST   $baseUrl/api/products           - Create product" -ForegroundColor White
Write-Host "  GET    $baseUrl/api/products/{id}      - Get product details" -ForegroundColor White
Write-Host "  POST   $baseUrl/api/buy/{id}           - Purchase product" -ForegroundColor White
Write-Host "  GET    $baseUrl/actuator/health        - Health check" -ForegroundColor White
Write-Host ""
