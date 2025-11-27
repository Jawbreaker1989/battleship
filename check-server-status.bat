@echo off
REM Script para verificar estado del servidor Battleship en Azure
REM Uso: check-server-status.bat
REM Requiere: sshpass instalado

setlocal enabledelayedexpansion

echo.
echo ========================================
echo   VERIFICAR ESTADO SERVIDOR AZURE
echo ========================================
echo.

set AZURE_HOST=68.211.112.149
set AZURE_USER=azureuser
set AZURE_PASS=200710526074Acdc
set AZURE_PORT=8080

echo Conectando a Azure VM...
echo IP: %AZURE_HOST%
echo.

REM Verificar si sshpass está disponible
where sshpass >nul 2>&1
if errorlevel 1 (
    echo ⚠️  AVISO: sshpass no está instalado
    echo Intenta instalar Git Bash que incluye sshpass
    echo https://git-scm.com/download/win
    echo.
    pause
    exit /b 1
)

REM Intentar conexión SSH
echo [1] Verificando conexión SSH...
sshpass -p "%AZURE_PASS%" ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=no %AZURE_USER%@%AZURE_HOST% "echo SSH_OK" >nul 2>&1

if errorlevel 1 (
    echo ❌ No hay conexión SSH
    echo Verifica:
    echo   - Azure VM está activa
    echo   - Firewall permite SSH (puerto 22^)
    echo   - Contraseña correcta
    pause
    exit /b 1
)

echo ✓ SSH conectado
echo.

REM Verificar proceso Java
echo [2] Verificando si servidor Java está corriendo...
sshpass -p "%AZURE_PASS%" ssh -o StrictHostKeyChecking=no %AZURE_USER%@%AZURE_HOST% "pgrep -f battleship-server" >nul 2>&1

if errorlevel 1 (
    echo ❌ Servidor NO está ejecutándose
    echo.
    echo Para iniciarlo ejecuta:
    echo   start-server.bat
    pause
    exit /b 1
) else (
    echo ✓ Servidor Java está corriendo
)

echo.
echo [3] Verificando puerto %AZURE_PORT%...
sshpass -p "%AZURE_PASS%" ssh -o StrictHostKeyChecking=no %AZURE_USER%@%AZURE_HOST% "ss -tlnp 2>/dev/null | grep :%AZURE_PORT%" >nul 2>&1

if errorlevel 1 (
    echo ⚠️  Puerto %AZURE_PORT% no está escuchando (puede estar iniciando)
) else (
    echo ✓ Puerto %AZURE_PORT% escuchando
)

echo.
echo [4] Últimas líneas del log...
sshpass -p "%AZURE_PASS%" ssh -o StrictHostKeyChecking=no %AZURE_USER%@%AZURE_HOST% "tail -5 /home/azureuser/server.log"

echo.
echo ========================================
echo   ESTADO: ✓ SERVIDOR ACTIVO
echo ========================================
echo.
echo Endpoint WebSocket: ws://%AZURE_HOST%:%AZURE_PORT%/battleship
echo.

pause
