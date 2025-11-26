@echo off
REM Script para ejecutar el servidor de Batalla Naval en Azure
REM Uso: run-server-azure.bat <IP_AZURE> [PUERTO]

setlocal enabledelayedexpansion

if "%1"=="" (
    echo.
    echo Uso: run-server-azure.bat ^<IP_AZURE^> [PUERTO]
    echo.
    echo Ejemplo:
    echo   run-server-azure.bat 68.211.112.149 1100
    echo.
    exit /b 1
)

set AZURE_IP=%1
set PUERTO=%2
if "%PUERTO%"=="" set PUERTO=1100

cd /d "%~dp0"

echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║   SERVIDOR BATALLA NAVAL - AZURE DEPLOYMENT            ║
echo ╚══════════════════════════════════════════════════════════╝
echo.
echo 🌐 IP Azure: %AZURE_IP%
echo 🔌 Puerto RMI: %PUERTO%
echo 📁 Ubicación: %CD%
echo.

REM Verificar que existen los JAR
if not exist "server\target\server-1.0-SNAPSHOT.jar" (
    echo ❌ Error: No se encuentra server-1.0-SNAPSHOT.jar
    echo    Ejecuta primero: mvn clean package -DskipTests
    exit /b 1
)

if not exist "shared\target\shared-1.0-SNAPSHOT.jar" (
    echo ❌ Error: No se encuentra shared-1.0-SNAPSHOT.jar
    echo    Ejecuta primero: mvn clean package -DskipTests
    exit /b 1
)

if not exist "all.policy" (
    echo ❌ Error: No se encuentra all.policy
    exit /b 1
)

REM Ejecutar servidor
echo ▶️  Iniciando servidor...
echo.

java -Djava.security.policy=all.policy ^
     -Djava.rmi.server.hostname=%AZURE_IP% ^
     -cp "server\target\server-1.0-SNAPSHOT.jar;shared\target\shared-1.0-SNAPSHOT.jar" ^
     co.edu.uptc.server.ServerMain %AZURE_IP% %PUERTO%

echo.
echo ❌ Servidor detenido
pause
