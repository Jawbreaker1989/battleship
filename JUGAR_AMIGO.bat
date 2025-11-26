@echo off
REM 🎮 BATALLA NAVAL - Juega con amigos en AZURE
REM Solo necesita los 2 archivos JAR en esta carpeta

echo.
echo ╔════════════════════════════════════════════╗
echo ║   🎮 BATALLA NAVAL - CLIENTE OPTIMIZADO   ║
echo ╚════════════════════════════════════════════╝
echo.

REM Cambiar a la carpeta del script
cd /d "%~dp0"

REM Variables - CAMBIAR ESTOS VALORES
set SERVIDOR_IP=68.211.112.149
set PUERTO=1100

REM Verificar que existen los JARs
if not exist "client-1.0-SNAPSHOT.jar" (
    echo ❌ ERROR: Falta client-1.0-SNAPSHOT.jar
    echo    Descargalo del proyecto
    pause
    exit /b 1
)

if not exist "shared-1.0-SNAPSHOT.jar" (
    echo ❌ ERROR: Falta shared-1.0-SNAPSHOT.jar
    echo    Descargalo del proyecto
    pause
    exit /b 1
)

echo ✅ Archivos encontrados
echo 🌐 Servidor: %SERVIDOR_IP%:%PUERTO%
echo.
echo ⏳ Conectando...
echo.

java -cp "client-1.0-SNAPSHOT.jar;shared-1.0-SNAPSHOT.jar" ^
     co.edu.uptc.client.ClientMain %SERVIDOR_IP% %PUERTO%

echo.
echo ❌ Desconectado
pause
