@echo off
REM Cliente simplificado distribuible (dist version)
if "%1"=="" (
  echo Uso: run-client-lan-simple.bat IP_SERVIDOR
  exit /b 1
)
set HOST=%1
set PORT=1100

echo === CLIENTE SIMPLE (DIST) ===
echo Servidor: %HOST%:%PORT%
if not exist "battleship-client-jar-with-dependencies.jar" (
  echo ERROR: Falta battleship-client-jar-with-dependencies.jar
  exit /b 1
)
java -Djava.security.policy=all.policy -jar battleship-client-jar-with-dependencies.jar %HOST% %PORT%
pause
