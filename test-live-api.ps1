# Test Live Flash Sale API on Render.com
# URL: https://flash-sale-engine-1.onrender.com

$baseUrl = "https://flash-sale-engine-1.onrender.com"

Write-Host "`n=== Testing Live Flash Sale API ===" -ForegroundColor Cyan
Write-Host "Base URL: $baseUrl`n" -ForegroundColor Yellow

# 1. Health Check
Write-Host "1. Health Check..." -ForegroundColor Green
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    Write-Host "   Status: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. Get All Products
Write-Host "`n2. Getting all products..." -ForegroundColor Green
try {
    $products = Invoke-RestMethod -Uri "$baseUrl/api/products" -Method Get
    Write-Host "   Found $($products.Count) products"
    $products | ForEach-Object {
        Write-Host "   - $($_.name): $($_.stock) units @ `$$($_.price)"
    }
} catch {
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Create a Product
Write-Host "`n3. Creating a new product..." -ForegroundColor Green
$newProduct = @{
    name = "iPhone 15 Pro"
    description = "Latest iPhone with A17 Pro chip"
    price = 999.99
    stock = 50
} | ConvertTo-Json

try {
    $created = Invoke-RestMethod -Uri "$baseUrl/api/products" -Method Post -Body $newProduct -ContentType "application/json"
    Write-Host "   Created product ID: $($created.id)" -ForegroundColor Green
    Write-Host "   Name: $($created.name), Stock: $($created.stock)"
    $productId = $created.id
} catch {
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    $productId = 1  # fallback
}

# 4. Make a Purchase
Write-Host "`n4. Making a purchase..." -ForegroundColor Green
$purchase = @{
    productId = $productId
    email = "test@example.com"
    quantity = 1
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri "$baseUrl/api/flash-sales/purchase" -Method Post -Body $purchase -ContentType "application/json"
    Write-Host "   Success: $($result.success)" -ForegroundColor Green
    Write-Host "   Order ID: $($result.orderId)"
    Write-Host "   Message: $($result.message)"
} catch {
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Check Stock
Write-Host "`n5. Checking current stock..." -ForegroundColor Green
try {
    $product = Invoke-RestMethod -Uri "$baseUrl/api/products/$productId" -Method Get
    Write-Host "   Current stock: $($product.stock) units"
} catch {
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete! ===" -ForegroundColor Cyan
Write-Host "`nYou can also test this API using:" -ForegroundColor Yellow
Write-Host "  - Postman: Import the URL and test endpoints"
Write-Host "  - curl: curl $baseUrl/api/products"
Write-Host "  - Your browser: Visit $baseUrl/api/products"
