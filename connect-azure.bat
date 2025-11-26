@echo off
echo ================================================
echo   BATTLESHIP CLIENT - CONECTANDO A AZURE
echo ================================================
echo.
echo Servidor Azure: 68.211.112.149:1100
echo.

java -Djava.security.policy=all.policy ^
     -cp shared\target\classes;client\target\classes ^
     co.edu.uptc.client.ClientMain 68.211.112.149 1100

pause
