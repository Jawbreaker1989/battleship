@echo off
REM ===============================================
REM  SERVIDOR SIMPLE - BATALLA NAVAL
REM  Inicia servidor en esta computadora
REM ===============================================

echo ============================================
echo   SERVIDOR BATALLA NAVAL - INICIANDO
echo ============================================
echo.

REM Compilar si es necesario
echo Compilando proyecto...
call mvn clean package -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)

echo Servidor iniciando en puerto 1100...
echo Esperando clientes...
echo.
echo IMPORTANTE: Tu IP es:
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4"') do echo%%a

echo.
echo Ejecuta el servidor:
java -Djava.security.policy=all.policy -cp shared\target\shared-1.0-SNAPSHOT.jar;server\target\server-1.0-SNAPSHOT.jar co.edu.uptc.server.ServerMain

pause