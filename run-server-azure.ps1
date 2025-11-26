#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Script para ejecutar el servidor de Batalla Naval en Azure
.DESCRIPTION
    Inicia el servidor RMI con configuración específica para Azure
.PARAMETER AzureIP
    Dirección IP pública del servidor Azure
.PARAMETER Port
    Puerto RMI (por defecto 1100)
.EXAMPLE
    ./run-server-azure.ps1 -AzureIP 68.211.112.149 -Port 1100
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$AzureIP,
    
    [Parameter(Mandatory=$false)]
    [int]$Port = 1100
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   SERVIDOR BATALLA NAVAL - AZURE DEPLOYMENT            ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "🌐 IP Azure: $AzureIP" -ForegroundColor Green
Write-Host "🔌 Puerto RMI: $Port" -ForegroundColor Green
Write-Host "📁 Ubicación: $(Get-Location)" -ForegroundColor Yellow
Write-Host ""

# Verificar JAR del servidor
if (-not (Test-Path "server\target\server-1.0-SNAPSHOT.jar")) {
    Write-Host "❌ Error: No se encuentra server-1.0-SNAPSHOT.jar" -ForegroundColor Red
    Write-Host "   Ejecuta primero: mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

# Verificar JAR compartido
if (-not (Test-Path "shared\target\shared-1.0-SNAPSHOT.jar")) {
    Write-Host "❌ Error: No se encuentra shared-1.0-SNAPSHOT.jar" -ForegroundColor Red
    Write-Host "   Ejecuta primero: mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

# Verificar policy
if (-not (Test-Path "all.policy")) {
    Write-Host "❌ Error: No se encuentra all.policy" -ForegroundColor Red
    exit 1
}

Write-Host "▶️  Iniciando servidor..." -ForegroundColor Green
Write-Host ""

$policyPath = (Get-Item "all.policy").FullName

java "-Djava.security.policy=$policyPath" `
     "-Djava.rmi.server.hostname=$AzureIP" `
     -cp "server\target\server-1.0-SNAPSHOT.jar;shared\target\shared-1.0-SNAPSHOT.jar" `
     co.edu.uptc.server.ServerMain $AzureIP $Port

Write-Host ""
Write-Host "❌ Servidor detenido" -ForegroundColor Red
