param(
    [string]$Password = "200710526074Acdc",
    [string]$Host = "68.211.112.149",
    [string]$User = "azureuser"
)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICAR ESTADO SERVIDOR AZURE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$AZURE_HOST = $Host
$AZURE_USER = $User
$AZURE_PORT = 8080

Write-Host "Host: $AZURE_HOST"
Write-Host "Usuario: $AZURE_USER"
Write-Host "Puerto: $AZURE_PORT"
Write-Host ""

# Verificar si sshpass está instalado
$sshpass = Get-Command sshpass -ErrorAction SilentlyContinue

if (-not $sshpass) {
    Write-Host "⚠️  sshpass no está instalado" -ForegroundColor Yellow
    Write-Host "Intenta con SSH tradicional (se pedirá contraseña):" -ForegroundColor Yellow
    Write-Host ""
    & ssh -o ConnectTimeout=5 "$AZURE_USER@$AZURE_HOST" "echo 'SSH OK'"
    exit
}

Write-Host "[1] Verificando conexión SSH..." -ForegroundColor Yellow

$connection = & sshpass -p "$Password" ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=no "$AZURE_USER@$AZURE_HOST" "echo 'SSH_OK'" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ No hay conexión SSH" -ForegroundColor Red
    Write-Host "Verifica:"
    Write-Host "  - Azure VM está activa"
    Write-Host "  - Firewall permite SSH (puerto 22)"
    Write-Host "  - Contraseña correcta"
    exit 1
}

Write-Host "✓ SSH conectado" -ForegroundColor Green
Write-Host ""

# Verificar proceso Java
Write-Host "[2] Verificando si servidor Java está corriendo..." -ForegroundColor Yellow

$process = & sshpass -p "$Password" ssh -o StrictHostKeyChecking=no "$AZURE_USER@$AZURE_HOST" 'pgrep -f battleship-server' 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Servidor NO está ejecutándose" -ForegroundColor Red
    Write-Host ""
    Write-Host "Para iniciarlo ejecuta:" -ForegroundColor Yellow
    Write-Host "  start-server.ps1"
    exit 1
} else {
    Write-Host "✓ Servidor Java está corriendo" -ForegroundColor Green
}

Write-Host ""
Write-Host "[3] Verificando puerto $AZURE_PORT..." -ForegroundColor Yellow

$port = & sshpass -p "$Password" ssh -o StrictHostKeyChecking=no "$AZURE_USER@$AZURE_HOST" "ss -tlnp 2>/dev/null | grep :$AZURE_PORT" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Puerto $AZURE_PORT no está escuchando (puede estar iniciando)" -ForegroundColor Yellow
} else {
    Write-Host "✓ Puerto $AZURE_PORT escuchando" -ForegroundColor Green
}

Write-Host ""
Write-Host "[4] Últimas líneas del log..." -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray

& sshpass -p "$Password" ssh -o StrictHostKeyChecking=no "$AZURE_USER@$AZURE_HOST" "tail -5 /home/azureuser/server.log" 2>&1

Write-Host "---" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ESTADO: ✓ SERVIDOR ACTIVO" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Endpoint WebSocket: ws://$AZURE_HOST:$AZURE_PORT/battleship" -ForegroundColor Cyan
Write-Host ""
