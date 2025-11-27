#!/bin/bash

# Script para ejecutar el servidor Battleship en Azure VM
# Uso: ./run-server-azure.sh <usuario> <host> <contraseña>

USUARIO=${1:-azureuser}
HOST=${2:-68.211.112.149}
PASSWORD=${3}

if [ -z "$PASSWORD" ]; then
    echo "Uso: ./run-server-azure.sh <usuario> <host> <contraseña>"
    echo "Ejemplo: ./run-server-azure.sh azureuser 68.211.112.149 micontraseña"
    exit 1
fi

echo "=============================================================="
echo "INICIANDO SERVIDOR BATTLESHIP EN AZURE VM"
echo "=============================================================="
echo "Usuario: $USUARIO"
echo "Host: $HOST"
echo ""

# Instalar sshpass si no está disponible
if ! command -v sshpass &> /dev/null; then
    echo "Instalando sshpass..."
    sudo apt-get update && sudo apt-get install -y sshpass
fi

# Ejecutar comando remoto con contraseña
echo "Iniciando servidor..."
sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no $USUARIO@$HOST \
    'cd /home/'"$USUARIO"' && nohup java -jar battleship-server-jar-with-dependencies.jar > server.log 2>&1 &'

sleep 2

echo ""
echo "Verificando servidor..."
sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no $USUARIO@$HOST \
    'ps aux | grep "battleship-server" | grep -v grep'

echo ""
echo "=============================================================="
echo "SERVIDOR INICIADO EXITOSAMENTE"
echo "=============================================================="
echo "Endpoint: ws://$HOST:8080/battleship"
echo ""
echo "Ver logs:"
echo "  sshpass -p \"$PASSWORD\" ssh $USUARIO@$HOST 'tail -f server.log'"
echo ""
echo "Detener servidor:"
echo "  sshpass -p \"$PASSWORD\" ssh $USUARIO@$HOST 'pkill -f battleship-server'"
echo "=============================================================="
