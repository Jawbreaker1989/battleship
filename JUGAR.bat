@echo off
echo ========================================
echo    BATALLA NAVAL - CLIENTE
echo ========================================
echo.
echo Conectando al servidor...
echo.

cd client\target
java -jar battleship-client-jar-with-dependencies.jar 68.211.112.149 8080

pause
