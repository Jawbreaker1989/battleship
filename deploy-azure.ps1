#!/usr/bin/env powershell

# Script simple de deployment a Azure VM
param(
    [string]$VMUser = "azureuser",
    [string]$VMHost = "68.211.112.149"
)

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "DEPLOYMENT BATTLESHIP SERVER A AZURE VM" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

$JAR_PATH = ".\server\target\battleship-server-jar-with-dependencies.jar"
$REMOTE_PATH = "/home/$VMUser/battleship-server.jar"

# Verificar JAR
if (!(Test-Path $JAR_PATH)) {
    Write-Host "ERROR: No se encontro el JAR" -ForegroundColor Red
    exit 1
}

$JAR_SIZE = (Get-Item $JAR_PATH).Length
Write-Host "JAR encontrado: $JAR_SIZE bytes" -ForegroundColor Green
Write-Host ""

# Conectar y transferir
Write-Host "Transfiriendo a Azure VM..." -ForegroundColor Yellow

# Usar base64 encoding para transferencia segura
Write-Host "Leyendo archivo..."
$JAR_DATA = [Convert]::ToBase64String([IO.File]::ReadAllBytes($JAR_PATH))
$JAR_DATA_SIZE = $JAR_DATA.Length

Write-Host "Tamanio base64: $JAR_DATA_SIZE caracteres" -ForegroundColor Yellow
Write-Host ""

# Transferir y ejecutar
Write-Host "Enviando a VM..." -ForegroundColor Yellow
$SSH_CMD = @"
`$DATA = "$JAR_DATA"
[IO.File]::WriteAllBytes("$REMOTE_PATH", [Convert]::FromBase64String(`$DATA))
Write-Host "Archivo recibido" -ForegroundColor Green
"@

ssh $VMUser@$VMHost $SSH_CMD

Write-Host ""
Write-Host "Iniciando servidor..." -ForegroundColor Yellow
ssh $VMUser@$VMHost "cd /home/$VMUser && nohup java -jar battleship-server.jar > server.log 2>&1 &"

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "DEPLOYMENT COMPLETADO" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "Endpoint: ws://$VMHost`:8080/battleship" -ForegroundColor Cyan
Write-Host "Log: ssh $VMUser@$VMHost 'tail -f server.log'" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Green
