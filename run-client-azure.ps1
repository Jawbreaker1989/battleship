#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Script para ejecutar el cliente de Batalla Naval conectando a Azure
.DESCRIPTION
    Inicia el cliente RMI conectando a un servidor en Azure
.PARAMETER AzureIP
    Dirección IP pública del servidor Azure
.PARAMETER Port
    Puerto RMI (por defecto 1100)
.EXAMPLE
    ./run-client-azure.ps1 -AzureIP 68.211.112.149 -Port 1100
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
Write-Host "║   CLIENTE BATALLA NAVAL - CONEXION A AZURE              ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "🌐 Servidor Azure: $AzureIP" -ForegroundColor Green
Write-Host "🔌 Puerto RMI: $Port" -ForegroundColor Green
Write-Host "📁 Ubicación: $(Get-Location)" -ForegroundColor Yellow
Write-Host ""

# Verificar JAR del cliente
if (-not (Test-Path "client\target\client-1.0-SNAPSHOT.jar")) {
    Write-Host "❌ Error: No se encuentra client-1.0-SNAPSHOT.jar" -ForegroundColor Red
    Write-Host "   Ejecuta primero: mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

# Verificar JAR compartido
if (-not (Test-Path "shared\target\shared-1.0-SNAPSHOT.jar")) {
    Write-Host "❌ Error: No se encuentra shared-1.0-SNAPSHOT.jar" -ForegroundColor Red
    Write-Host "   Ejecuta primero: mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

Write-Host "▶️  Iniciando cliente..." -ForegroundColor Green
Write-Host "💻 Detectando IP local para callbacks RMI..." -ForegroundColor Yellow
Write-Host ""

java -cp "client\target\client-1.0-SNAPSHOT.jar;shared\target\shared-1.0-SNAPSHOT.jar" `
     co.edu.uptc.client.ClientMain $AzureIP $Port

Write-Host ""
Write-Host "❌ Cliente desconectado" -ForegroundColor Red
