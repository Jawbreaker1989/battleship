@echo off
REM Script para ejecutar el cliente de Batalla Naval en Windows
REM Conecta al servidor en Azure VM

setlocal

set AZURE_SERVER_IP=68.211.112.149
set REGISTRY_PORT=1099

echo ========================================
echo    Cliente Batalla Naval - Azure
echo ========================================
echo    Servidor: %AZURE_SERVER_IP%:%REGISTRY_PORT%
echo ========================================
echo.

REM Verificar que el proyecto esté compilado
if not exist "client\target\classes" (
    echo Compilando proyecto...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo ERROR: La compilacion fallo
        pause
        exit /b 1
    )
)

REM Ejecutar cliente
echo Conectando al servidor...
echo.

java -cp "shared\target\classes;client\target\classes" ^
     co.edu.uptc.client.ClientMain %AZURE_SERVER_IP% %REGISTRY_PORT%

if errorlevel 1 (
    echo.
    echo ERROR: No se pudo conectar al servidor
    echo.
    echo Verifica:
    echo   1. El servidor esta ejecutandose en Azure VM
    echo   2. Los puertos 1099 y 1100 estan abiertos en Azure NSG
    echo   3. Tu conexion a internet esta funcionando
    pause
)

endlocal
