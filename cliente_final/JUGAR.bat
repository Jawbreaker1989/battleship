@echo off
title BATALLA NAVAL
color 0A

echo.
echo  ========================================
echo     🚢 BATALLA NAVAL - JUEGO ONLINE
echo  ========================================
echo.
echo  Conectando al servidor...
echo  IP: 68.211.112.149
echo  Puerto: 8080
echo.

java -jar battleship-client.jar 68.211.112.149 8080

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  ❌ Error al iniciar el juego
    echo.
    echo  Posibles causas:
    echo  - Java no esta instalado
    echo  - No hay conexion a Internet
    echo.
    echo  Descarga Java desde: https://www.java.com
    echo.
    pause
)
