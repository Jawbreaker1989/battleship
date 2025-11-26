#!/usr/bin/env pwsh
<#
.SYNOPSIS
🎮 Batalla Naval - Cliente Optimizado para Jugar con Amigos en Azure
#>

$SERVIDOR_IP = "68.211.112.149"
$PUERTO = 1100

Write-Host "`n╔════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   🎮 BATALLA NAVAL - CLIENTE OPTIMIZADO   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# Verificar archivos
if (-not (Test-Path "client-1.0-SNAPSHOT.jar")) {
    Write-Host "❌ ERROR: Falta client-1.0-SNAPSHOT.jar" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path "shared-1.0-SNAPSHOT.jar")) {
    Write-Host "❌ ERROR: Falta shared-1.0-SNAPSHOT.jar" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Archivos encontrados" -ForegroundColor Green
Write-Host "🌐 Servidor: $SERVIDOR_IP`:$PUERTO" -ForegroundColor Yellow
Write-Host "⏳ Conectando...`n" -ForegroundColor Yellow

java -cp "client-1.0-SNAPSHOT.jar;shared-1.0-SNAPSHOT.jar" `
     co.edu.uptc.client.ClientMain $SERVIDOR_IP $PUERTO

Write-Host "`n❌ Desconectado" -ForegroundColor Red
