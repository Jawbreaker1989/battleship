@echo off
REM Script para ejecutar el cliente de Batalla Naval conectando a Azure
REM Uso: run-client-azure.bat <IP_AZURE> [PUERTO]

setlocal enabledelayedexpansion

if "%1"=="" (
    echo.
    echo Uso: run-client-azure.bat ^<IP_AZURE^> [PUERTO]
    echo.
    echo Ejemplo:
    echo   run-client-azure.bat 68.211.112.149 1100
    echo.
    exit /b 1
)

set AZURE_IP=%1
set PUERTO=%2
if "%PUERTO%"=="" set PUERTO=1100

cd /d "%~dp0"

echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║   CLIENTE BATALLA NAVAL - CONEXION A AZURE              ║
echo ╚══════════════════════════════════════════════════════════╝
echo.
echo 🌐 Servidor Azure: %AZURE_IP%
echo 🔌 Puerto RMI: %PUERTO%
echo 📁 Ubicación: %CD%
echo.

REM Verificar que existen los JAR
if not exist "client\target\client-1.0-SNAPSHOT.jar" (
    echo ❌ Error: No se encuentra client-1.0-SNAPSHOT.jar
    echo    Ejecuta primero: mvn clean package -DskipTests
    exit /b 1
)

if not exist "shared\target\shared-1.0-SNAPSHOT.jar" (
    echo ❌ Error: No se encuentra shared-1.0-SNAPSHOT.jar
    echo    Ejecuta primero: mvn clean package -DskipTests
    exit /b 1
)

REM Ejecutar cliente
echo ▶️  Iniciando cliente...
echo 💻 Detectando IP local para callbacks RMI...
echo.

java -cp "client\target\client-1.0-SNAPSHOT.jar;shared\target\shared-1.0-SNAPSHOT.jar" ^
     co.edu.uptc.client.ClientMain %AZURE_IP% %PUERTO%

echo.
echo ❌ Cliente desconectado
pause
