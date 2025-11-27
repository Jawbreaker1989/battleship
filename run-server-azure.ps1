#!/usr/bin/env powershell

# Script para ejecutar el servidor Battleship en Azure VM
# Uso: .\run-server-azure.ps1 -User azureuser -Host 68.211.112.149 -Password "contraseña"

param(
    [Parameter(Mandatory=$true)]
    [string]$User,
    
    [Parameter(Mandatory=$true)]
    [string]$Host,
    
    [Parameter(Mandatory=$true)]
    [string]$Password
)

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "INICIANDO SERVIDOR BATTLESHIP EN AZURE VM" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Usuario: $User" -ForegroundColor Yellow
Write-Host "Host: $Host" -ForegroundColor Yellow
Write-Host ""

# Crear archivo de script temporal para ejecutar en la VM
$RemoteScript = @"
cd /home/$User
nohup java -jar battleship-server-jar-with-dependencies.jar > server.log 2>&1 &
sleep 2
ps aux | grep battleship-server | grep -v grep
"@

# Guardar script temporalmente
$TempScript = [System.IO.Path]::GetTempFileName() | Rename-Item -NewName { $_.Name -Replace '\.tmp$', '.sh' } -PassThru

Set-Content -Path $TempScript.FullName -Value $RemoteScript -Encoding ASCII

Write-Host "Iniciando servidor..." -ForegroundColor Yellow

# Crear string de conexión SSH con contraseña usando expect o sshpass
# En Windows, usamos una secuencia de comandos PowerShell con Plink (putty)
if (Get-Command plink -ErrorAction SilentlyContinue) {
    # Usar Plink si está disponible
    Write-Host "Usando Plink..." -ForegroundColor Gray
    echo $Password | plink -l $User $Host 'cd /home/'"$User"' && nohup java -jar battleship-server-jar-with-dependencies.jar > server.log 2>&1 &'
}
elseif (Get-Command ssh -ErrorAction SilentlyContinue) {
    # Alternativa: crear un archivo con esperado
    Write-Host "Usando SSH directo (se pedira contraseña)..." -ForegroundColor Gray
    & ssh -o ConnectTimeout=10 $User@$Host 'cd /home/'"$User"' && nohup java -jar battleship-server-jar-with-dependencies.jar > server.log 2>&1 &'
}
else {
    Write-Host "ERROR: No se encontro SSH o Plink" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Verificando servidor..." -ForegroundColor Yellow
& ssh $User@$Host 'ps aux | grep battleship-server | grep -v grep'

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "SERVIDOR INICIADO EXITOSAMENTE" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host "Endpoint: ws://$Host`:8080/battleship" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ver logs:" -ForegroundColor Yellow
Write-Host "  ssh $User@$Host 'tail -f server.log'" -ForegroundColor Gray
Write-Host ""
Write-Host "Detener servidor:" -ForegroundColor Yellow
Write-Host "  ssh $User@$Host 'pkill -f battleship-server'" -ForegroundColor Gray
Write-Host "============================================================" -ForegroundColor Green

# Limpiar archivo temporal
Remove-Item $TempScript.FullName -Force -ErrorAction SilentlyContinue
