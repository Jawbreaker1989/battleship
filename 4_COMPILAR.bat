@echo off
echo ============================================
echo    COMPILANDO PROYECTO BATALLA NAVAL
echo    (VERSION COMPLETA CON EMPAQUETADO)
echo ============================================
echo.

echo Limpiando y compilando todos los modulos...
mvn clean package

echo.
if %ERRORLEVEL%==0 (
  echo ============================================
  echo   COMPILACION COMPLETADA EXITOSAMENTE!
  echo   - Modulos: shared, server, client
  echo   - JARs generados en target/
  echo   - Cliente con dependencias listo
  echo ============================================
) else (
  echo ============================================
  echo   ERROR EN COMPILACION
  echo   Verifica Maven y dependencias
  echo ============================================
)
echo.
echo Presione una tecla para continuar...
pause