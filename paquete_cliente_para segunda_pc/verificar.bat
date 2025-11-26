@echo off
REM ================================================================
REM     VERIFICADOR E INSTALADOR - CLIENTE BATALLA NAVAL LAN
REM ================================================================

echo ================================================================
echo              CLIENTE BATALLA NAVAL DISTRIBUIDO
echo                 VERIFICACION DEL SISTEMA
echo ================================================================
echo.

REM Verificar Java
echo [1/4] Verificando Java...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ ERROR: Java NO esta instalado
    echo.
    echo Necesitas instalar Java 8 o superior:
    echo 1. Ve a https://www.java.com/download/
    echo 2. Descarga e instala Java
    echo 3. Reinicia esta aplicacion
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Java instalado correctamente
)

REM Verificar archivos necesarios
echo [2/4] Verificando archivos...
if not exist "battleship-client-jar-with-dependencies.jar" (
    echo ❌ ERROR: Falta battleship-client-jar-with-dependencies.jar
    goto :error_files
)
if not exist "all.policy" (
    echo ❌ ERROR: Falta all.policy
    goto :error_files
)
echo ✅ Todos los archivos necesarios encontrados

REM Verificar conectividad (opcional)
echo [3/4] Verificando red...
if "%1"=="" (
    echo ⚠️  No se proporciono IP del servidor
    echo Uso: verificar.bat [IP_SERVIDOR]
    echo Ejemplo: verificar.bat 192.168.20.60
    goto :skip_ping
)

echo Probando conectividad con %1...
ping -n 1 %1 >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo ✅ Conectividad OK con %1
) else (
    echo ⚠️  No se puede hacer ping a %1 (normal si hay firewall)
)

:skip_ping
echo [4/4] Sistema listo
echo.
echo ================================================================
echo                         SISTEMA LISTO
echo ================================================================
echo.
echo Para conectarte al servidor, usa:
echo   run-client-lan-simple.bat [IP_SERVIDOR]
echo.
echo Ejemplo:
echo   run-client-lan-simple.bat 192.168.20.60
echo.
if "%1" neq "" (
    echo ¿Quieres conectarte ahora a %1? (S/N)
    set /p respuesta=
    if /i "%respuesta%"=="s" (
        call run-client-lan-simple.bat %1
    )
)
pause
exit /b 0

:error_files
echo.
echo Asegurate de tener todos estos archivos en la misma carpeta:
echo - battleship-client-jar-with-dependencies.jar
echo - all.policy
echo - run-client-lan-simple.bat
echo - run-client-lan.bat
echo - verificar.bat (este archivo)
echo.
pause
exit /b 1