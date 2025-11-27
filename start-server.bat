@echo off
REM Script para iniciar servidor Battleship en Azure
REM Uso: start-server.bat

setlocal enabledelayedexpansion

echo.
echo ========================================
echo   INICIAR SERVIDOR AZURE
echo ========================================
echo.

set AZURE_HOST=68.211.112.149
set AZURE_USER=azureuser
set AZURE_PASS=200710526074Acdc
set LOCAL_JAR=server\target\battleship-server-jar-with-dependencies.jar
set REMOTE_JAR=/home/azureuser/battleship-server.jar

REM Verificar si JAR existe localmente
if not exist "%LOCAL_JAR%" (
    echo ❌ ERROR: JAR no encontrado en %LOCAL_JAR%
    echo.
    echo Solución: Compilar primero
    echo   mvn clean install
    echo.
    pause
    exit /b 1
)

echo [1] JAR encontrado en local
echo     Tamaño: 
for /f "tokens=*" %%A in ('dir "%LOCAL_JAR%" ^| findstr /R "battleship"') do echo     %%A
echo.

echo [2] Transfiriendo JAR a Azure...
echo     Desde: %LOCAL_JAR%
echo     Hacia:  %AZURE_USER%@%AZURE_HOST%:%REMOTE_JAR%
echo.

scp -o ConnectTimeout=10 "%LOCAL_JAR%" "%AZURE_USER%@%AZURE_HOST%:%REMOTE_JAR%"

if errorlevel 1 (
    echo ❌ Error en transferencia SCP
    echo Verifica contraseña SSH y conexión
    pause
    exit /b 1
)

echo ✓ Transferencia exitosa
echo.

echo [3] Deteniendo servidor anterior (si existe)...
ssh "%AZURE_USER%@%AZURE_HOST%" "pkill -9 -f battleship-server 2>/dev/null; sleep 1"

echo ✓ Limpio
echo.

echo [4] Iniciando servidor en Azure...
ssh "%AZURE_USER%@%AZURE_HOST%" "cd /home/%AZURE_USER% && nohup java -jar battleship-server.jar > server.log 2>&1 &"

timeout /t 3 /nobreak

echo.

echo [5] Verificando que servidor está corriendo...
ssh "%AZURE_USER%@%AZURE_HOST%" "pgrep -f battleship-server"

if errorlevel 1 (
    echo ❌ Servidor no se inició correctamente
    echo.
    echo Ver logs:
    echo   ssh %AZURE_USER%@%AZURE_HOST%
    echo   tail -50 /home/azureuser/server.log
) else (
    echo ✓ Servidor iniciado exitosamente
)

echo.
echo ========================================
echo   SERVIDOR EN EJECUCIÓN
echo ========================================
echo.
echo Endpoint: ws://68.211.112.149:8080/battleship
echo Logs:     ssh %AZURE_USER%@%AZURE_HOST% 'tail -f /home/azureuser/server.log'
echo Detener:  ssh %AZURE_USER%@%AZURE_HOST% 'pkill -f battleship-server'
echo.
pause
