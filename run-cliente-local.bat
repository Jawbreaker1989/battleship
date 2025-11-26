@echo off
title Cliente local - Batalla Naval

echo ============================================
echo       CLIENTE LOCAL - BATALLA NAVAL
echo           (Conexion rapida)
echo ============================================

REM Compilar si es necesario
if not exist "client\target\classes" (
    echo Compilando...
    call mvn -q compile -pl shared,client -am
)

REM Ejecutar cliente
java -Djava.security.policy=all.policy -cp shared\target\classes;client\target\classes co.edu.uptc.client.ClientMain localhost 1100

pause