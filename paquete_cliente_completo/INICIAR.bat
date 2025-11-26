@echo off
REM ================================================================
REM            CLIENTE BATALLA NAVAL - ARRANQUE AUTOMATICO
REM ================================================================

title Cliente Batalla Naval Distribuido

:inicio
cls
echo ================================================================
echo              CLIENTE BATALLA NAVAL DISTRIBUIDO
echo                    MENU PRINCIPAL
echo ================================================================
echo.
echo 1. Verificar sistema (Java, archivos, etc.)
echo 2. Conectar al servidor (necesitas la IP)
echo 3. Conectar a IP conocida (192.168.20.60)
echo 4. Salir
echo.
set /p opcion=Elige una opcion (1-4): 

if "%opcion%"=="1" goto verificar
if "%opcion%"=="2" goto conectar_manual
if "%opcion%"=="3" goto conectar_conocida
if "%opcion%"=="4" goto salir
echo Opcion invalida. Intenta de nuevo.
pause
goto inicio

:verificar
cls
echo Verificando sistema...
call verificar.bat
pause
goto inicio

:conectar_manual
cls
echo ================================================================
echo                    CONECTAR AL SERVIDOR
echo ================================================================
echo.
set /p ip_servidor=Ingresa la IP del servidor (ej: 192.168.1.100): 
if "%ip_servidor%"=="" (
    echo Error: Debes ingresar una IP
    pause
    goto inicio
)
echo.
echo Conectando a %ip_servidor%...
call run-client-lan-simple.bat %ip_servidor%
pause
goto inicio

:conectar_conocida
cls
echo Conectando a 192.168.20.60...
call run-client-lan-simple.bat 192.168.20.60
pause
goto inicio

:salir
echo.
echo ¡Gracias por usar el Cliente Batalla Naval!
echo ¡Que tengas un buen dia!
pause
exit