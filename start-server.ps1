param(
    [string]$Password = "200710526074Acdc",
    [string]$Host = "68.211.112.149",
    [string]$User = "azureuser"
)

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INICIAR SERVIDOR AZURE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$AZURE_HOST = $Host
$AZURE_USER = $User
$LOCAL_JAR = ".\server\target\battleship-server-jar-with-dependencies.jar"
$REMOTE_JAR = "/home/$AZURE_USER/battleship-server.jar"

# Verificar si JAR existe
if (-not (Test-Path $LOCAL_JAR)) {
    Write-Host "❌ ERROR: JAR no encontrado" -ForegroundColor Red
    Write-Host "Ruta esperada: $LOCAL_JAR" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Solución: Compilar primero" -ForegroundColor Yellow
    Write-Host "  mvn clean install" -ForegroundColor White
    Write-Host ""
    exit 1
}

$jarSize = (Get-Item $LOCAL_JAR).Length / 1MB
Write-Host "[1] JAR encontrado en local" -ForegroundColor Green
Write-Host "    Tamaño: $([Math]::Round($jarSize, 2)) MB" -ForegroundColor Gray
Write-Host ""

Write-Host "[2] Transfiriendo JAR a Azure..." -ForegroundColor Yellow
Write-Host "    Desde: $LOCAL_JAR" -ForegroundColor Gray
Write-Host "    Hacia: $AZURE_USER@$AZURE_HOST`:$REMOTE_JAR" -ForegroundColor Gray
Write-Host ""

# Usar scp con sshpass si está disponible
$sshpass = Get-Command sshpass -ErrorAction SilentlyContinue

if ($sshpass) {
    & sshpass -p "$Password" scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no "$LOCAL_JAR" "${AZURE_USER}@${AZURE_HOST}:${REMOTE_JAR}" 2>&1
} else {
    & scp -o ConnectTimeout=10 "$LOCAL_JAR" "${AZURE_USER}@${AZURE_HOST}:${REMOTE_JAR}" 2>&1
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error en transferencia SCP" -ForegroundColor Red
    Write-Host "Verifica contraseña SSH y conexión" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Transferencia exitosa" -ForegroundColor Green
Write-Host ""

Write-Host "[3] Deteniendo servidor anterior (si existe)..." -ForegroundColor Yellow

if ($sshpass) {
    & sshpass -p "$Password" ssh -o StrictHostKeyChecking=no "$AZURE_USER@$AZURE_HOST" "pkill -f battleship-server || true" 2>&1
} else {
    & ssh "$AZURE_USER@$AZURE_HOST" "pkill -f battleship-server || true" 2>&1
}

Start-Sleep -Seconds 2

Write-Host "✓ Limpio" -ForegroundColor Green
Write-Host ""

Write-Host "[4] Iniciando servidor en Azure..." -ForegroundColor Yellow

if ($sshpass) {
    & sshpass -p "$Password" ssh -o StrictHostKeyChecking=no "$AZURE_USER@$AZURE_HOST" "cd /home/$AZURE_USER && nohup java -jar battleship-server.jar > server.log 2>&1 &" 2>&1
} else {
    & ssh "$AZURE_USER@$AZURE_HOST" "cd /home/$AZURE_USER && nohup java -jar battleship-server.jar > server.log 2>&1 &" 2>&1
}

Start-Sleep -Seconds 3

Write-Host ""
Write-Host "[5] Verificando que servidor está corriendo..." -ForegroundColor Yellow

if ($sshpass) {
    $process = & sshpass -p "$Password" ssh -o StrictHostKeyChecking=no "$AZURE_USER@$AZURE_HOST" "ps aux | grep -i battleship-server | grep -v grep" 2>&1
} else {
    $process = & ssh "$AZURE_USER@$AZURE_HOST" "ps aux | grep -i battleship-server | grep -v grep" 2>&1
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Servidor no se inició correctamente" -ForegroundColor Red
    Write-Host ""
    Write-Host "Ver logs:" -ForegroundColor Yellow
    Write-Host "  ssh $AZURE_USER@$AZURE_HOST" -ForegroundColor White
    Write-Host "  tail -50 /home/azureuser/server.log" -ForegroundColor White
    exit 1
} else {
    Write-Host "✓ Servidor iniciado exitosamente" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  SERVIDOR EN EJECUCIÓN" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Endpoint WebSocket:" -ForegroundColor Cyan
Write-Host "  ws://68.211.112.149:8080/battleship" -ForegroundColor White
Write-Host ""
Write-Host "Ver logs en tiempo real:" -ForegroundColor Cyan
Write-Host "  ssh $AZURE_USER@$AZURE_HOST 'tail -f /home/azureuser/server.log'" -ForegroundColor White
Write-Host ""
Write-Host "Detener servidor:" -ForegroundColor Cyan
Write-Host "  ssh $AZURE_USER@$AZURE_HOST 'pkill -f battleship-server'" -ForegroundColor White
Write-Host ""
