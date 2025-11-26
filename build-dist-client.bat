@echo off
REM ===============================================
REM  Construye el JAR distribuible del cliente 
REM  VERSION ACTUALIZADA con todas las mejoras:
REM  - Callbacks RMI funcionales
REM  - Polling robusto para turnos
REM  - Validacion LAN integrada
REM ===============================================

echo ============================================
echo   CONSTRUYENDO CLIENTE DISTRIBUIBLE
echo   Version: Con mejoras RMI y callbacks
echo ============================================
echo.

ECHO Compilando cliente con dependencias (version actualizada)...
ECHO Deteniendo posibles procesos Java en ejecucion...
taskkill /F /IM java.exe >nul 2>&1

REM Intentar con clean primero, si falla usar compile package
call mvn -q clean package -pl shared,client -am
if %ERRORLEVEL% neq 0 (
  ECHO Clean fallo, intentando sin clean...
  call mvn -q compile package -pl shared,client -am || goto :error
)

set SRC=client\target\battleship-client-jar-with-dependencies.jar
set DEST=dist_cliente_lan\battleship-client-jar-with-dependencies.jar

if not exist %SRC% (
  echo No se encontró %SRC%
  goto :error
)

copy /Y %SRC% %DEST% >nul || goto :error
ECHO.
ECHO ============================================
ECHO   CLIENTE DISTRIBUIBLE ACTUALIZADO
ECHO ============================================
ECHO Copiado: %SRC%
ECHO Destino: %DEST%
ECHO.
ECHO Funciones incluidas:
ECHO + Callbacks RMI sin "Connection refused"
ECHO + Polling automatico para turnos
ECHO + Ataques habilitados correctamente  
ECHO + Logs detallados [CALLBACK] [POLL]
ECHO.
ECHO ¡LISTO! Copia la carpeta dist_cliente_lan 
ECHO completa al otro computador para usar la
ECHO version mas actualizada.
ECHO ============================================

goto :eof

:error
echo.
echo ============================================
echo   ERROR DURANTE EL BUILD O COPIA
echo ============================================
echo Posibles causas:
echo - Maven no esta instalado o configurado
echo - Java no esta en el PATH
echo - Problemas de permisos en carpetas
echo.
echo Solucion:
echo 1. Verifica: mvn --version
echo 2. Verifica: java --version  
echo 3. Ejecuta como administrador si es necesario
echo ============================================
pause
exit /b 1
