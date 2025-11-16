# Script de Pruebas de Seguridad - Windows PowerShell
# Uso: .\test_seguridad.ps1

$baseUrl = "http://localhost:5000"
$testResults = @()

function Write-TestResult {
    param($testName, $passed, $message = "")
    $result = @{
        Test = $testName
        Passed = $passed
        Message = $message
    }
    $script:testResults += $result
    
    if ($passed) {
        Write-Host "✅ $testName" -ForegroundColor Green
    } else {
        Write-Host "❌ $testName" -ForegroundColor Red
        if ($message) {
            Write-Host "   $message" -ForegroundColor Yellow
        }
    }
}

Write-Host "🧪 Iniciando Pruebas de Seguridad" -ForegroundColor Cyan
Write-Host "Servidor: $baseUrl" -ForegroundColor Gray
Write-Host ""

# Test 1: Verificar que el servidor está corriendo
Write-Host "1️⃣ Verificando conexión con el servidor..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/usuarios/login" -Method HEAD -UseBasicParsing -TimeoutSec 5
    Write-TestResult "Servidor accesible" $true
} catch {
    Write-TestResult "Servidor accesible" $false "El servidor no está corriendo o no es accesible en $baseUrl"
    Write-Host ""
    Write-Host "⚠️  Por favor inicia el servidor con: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Test 2: Verificar headers de seguridad
Write-Host "2️⃣ Verificando headers de seguridad HTTP..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/usuarios/login" -Method HEAD -UseBasicParsing
    
    $headers = @{
        "X-Frame-Options" = $false
        "X-Content-Type-Options" = $false
        "Strict-Transport-Security" = $false
        "Content-Security-Policy" = $false
    }
    
    foreach ($header in $headers.Keys) {
        if ($response.Headers[$header]) {
            $headers[$header] = $true
            Write-Host "   ✅ $header presente" -ForegroundColor Green
        } else {
            Write-Host "   ❌ $header falta" -ForegroundColor Red
        }
    }
    
    $allPresent = $headers.Values | Where-Object { $_ -eq $false } | Measure-Object | Select-Object -ExpandProperty Count
    Write-TestResult "Headers de seguridad" ($allPresent -eq 0) "$($headers.Values | Where-Object { $_ -eq $true } | Measure-Object | Select-Object -ExpandProperty Count) de $($headers.Count) headers presentes"
} catch {
    Write-TestResult "Headers de seguridad" $false $_.Exception.Message
}
Write-Host ""

# Test 3: Probar acceso sin token
Write-Host "3️⃣ Probando protección de endpoints (sin token)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/usuarios/listarUsuarios" -Method GET -UseBasicParsing -ErrorAction Stop
    Write-TestResult "Protección de endpoints" $false "El endpoint debería requerir autenticación pero devolvió: $($response.StatusCode)"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-TestResult "Protección de endpoints" $true "401 Unauthorized (correcto)"
    } else {
        Write-TestResult "Protección de endpoints" $false "Error inesperado: $($_.Exception.Response.StatusCode.value__)"
    }
}
Write-Host ""

# Test 4: Probar login
Write-Host "4️⃣ Probando autenticación (login)..." -ForegroundColor Yellow
$token = $null
$loginEmail = "test@test.com"
$loginPassword = "test"

Write-Host "   Intentando login con: $loginEmail" -ForegroundColor Gray
Write-Host "   ⚠️  Nota: Asegúrate de tener un usuario válido en la base de datos" -ForegroundColor Yellow

$body = @{
    correo = $loginEmail
    password = $loginPassword
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/usuarios/login" -Method POST -Body $body -ContentType "application/json"
    if ($response.token) {
        $token = $response.token
        Write-TestResult "Login exitoso" $true "Token obtenido: $($token.Substring(0, [Math]::Min(30, $token.Length)))..."
    } else {
        Write-TestResult "Login exitoso" $false "No se recibió token en la respuesta"
    }
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-TestResult "Login exitoso" $false "Credenciales incorrectas o usuario no existe. Verifica tus credenciales en la base de datos."
    } else {
        Write-TestResult "Login exitoso" $false $_.Exception.Message
    }
}
Write-Host ""

# Test 5: Probar acceso con token válido
if ($token) {
    Write-Host "5️⃣ Probando acceso con token válido..." -ForegroundColor Yellow
    $headers = @{
        Authorization = "Bearer $token"
        ContentType = "application/json"
    }
    
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/api/usuarios/listarUsuarios" -Method GET -Headers $headers -UseBasicParsing
        Write-TestResult "Acceso con token válido" $true "Código: $($response.StatusCode)"
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 403) {
            Write-TestResult "Acceso con token válido" $true "403 Forbidden (token válido pero sin permisos - correcto)"
        } elseif ($statusCode -eq 401) {
            Write-TestResult "Acceso con token válido" $false "401 Unauthorized (token no válido o expirado)"
        } else {
            Write-TestResult "Acceso con token válido" $false "Error: $statusCode"
        }
    }
    Write-Host ""
} else {
    Write-Host "5️⃣ ⏭️  Omitido (no se obtuvo token)" -ForegroundColor Gray
    Write-Host ""
}

# Test 6: Probar token inválido
Write-Host "6️⃣ Probando token inválido..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer token_invalido_aqui"
    ContentType = "application/json"
}

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/usuarios/listarUsuarios" -Method GET -Headers $headers -UseBasicParsing -ErrorAction Stop
    Write-TestResult "Rechazo de token inválido" $false "El endpoint debería rechazar tokens inválidos"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-TestResult "Rechazo de token inválido" $true "401 Unauthorized (correcto)"
    } else {
        Write-TestResult "Rechazo de token inválido" $false "Error: $($_.Exception.Response.StatusCode.value__)"
    }
}
Write-Host ""

# Resumen
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "📊 Resumen de Pruebas" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$passed = ($testResults | Where-Object { $_.Passed -eq $true } | Measure-Object).Count
$total = $testResults.Count

foreach ($result in $testResults) {
    $icon = if ($result.Passed) { "✅" } else { "❌" }
    $color = if ($result.Passed) { "Green" } else { "Red" }
    Write-Host "$icon $($result.Test)" -ForegroundColor $color
    if ($result.Message) {
        Write-Host "   $($result.Message)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "Resultado: $passed/$total pruebas pasadas" -ForegroundColor $(if ($passed -eq $total) { "Green" } else { "Yellow" })

if ($passed -eq $total) {
    Write-Host ""
    Write-Host "🎉 ¡Todas las pruebas pasaron!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "⚠️  Algunas pruebas fallaron. Revisa los detalles arriba." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "📝 Nota: Para ver los logs de auditoría, revisa la consola del servidor o el archivo de logs." -ForegroundColor Gray

