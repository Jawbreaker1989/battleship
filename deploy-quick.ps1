# Script rápido de deployment a Azure
# Uso: .\deploy-quick.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "DEPLOYMENT SERVIDOR A AZURE" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$AZURE_USER = "azureuser"
$AZURE_HOST = "68.211.112.149"
$LOCAL_JAR = ".\server\target\battleship-server-jar-with-dependencies.jar"
$REMOTE_JAR = "/home/$AZURE_USER/battleship-server.jar"

# Verificar que el JAR existe
if (!(Test-Path $LOCAL_JAR)) {
    Write-Host "❌ ERROR: No se encontró el JAR compilado" -ForegroundColor Red
    Write-Host "   Ejecuta primero: mvn clean package" -ForegroundColor Yellow
    exit 1
}

Write-Host "📦 JAR encontrado: $LOCAL_JAR" -ForegroundColor Green
Write-Host "📤 Transfiriendo a Azure VM..." -ForegroundColor Yellow
Write-Host ""

# Transferir JAR
scp $LOCAL_JAR "${AZURE_USER}@${AZURE_HOST}:${REMOTE_JAR}"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Transferencia exitosa" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "🔄 Deteniendo servidor anterior (si existe)..." -ForegroundColor Yellow
    ssh "${AZURE_USER}@${AZURE_HOST}" "pkill -f battleship-server || true"
    Start-Sleep -Seconds 2
    
    Write-Host "🚀 Iniciando servidor optimizado..." -ForegroundColor Yellow
    ssh "${AZURE_USER}@${AZURE_HOST}" "cd /home/${AZURE_USER} && nohup java -jar battleship-server.jar > server.log 2>&1 &"
    Start-Sleep -Seconds 3
    
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host "✅ SERVIDOR DESPLEGADO EN AZURE" -ForegroundColor Green
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "🌐 Endpoint: ws://${AZURE_HOST}:8080/battleship" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📋 Comandos para clientes:" -ForegroundColor Yellow
    Write-Host "   java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar ${AZURE_HOST} 8080" -ForegroundColor White
    Write-Host ""
    Write-Host "📊 Ver logs:" -ForegroundColor Yellow
    Write-Host "   ssh ${AZURE_USER}@${AZURE_HOST} 'tail -f server.log'" -ForegroundColor White
    Write-Host ""
    
} else {
    Write-Host "❌ Error en la transferencia" -ForegroundColor Red
    exit 1
}
