#!/bin/bash
# Script para ejecutar el cliente de Batalla Naval en Linux/Mac
# Conecta al servidor en Azure VM

set -e

AZURE_SERVER_IP="68.211.112.149"
REGISTRY_PORT=1099

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "========================================"
echo "   Cliente Batalla Naval - Azure"
echo "========================================"
echo "   Servidor: $AZURE_SERVER_IP:$REGISTRY_PORT"
echo "========================================"
echo ""

# Verificar que el proyecto esté compilado
if [ ! -d "client/target/classes" ]; then
    echo -e "${YELLOW}Compilando proyecto...${NC}"
    mvn clean package -DskipTests
fi

# Ejecutar cliente
echo -e "${GREEN}Conectando al servidor...${NC}"
echo ""

java -cp "shared/target/classes:client/target/classes" \
     co.edu.uptc.client.ClientMain $AZURE_SERVER_IP $REGISTRY_PORT

if [ $? -ne 0 ]; then
    echo ""
    echo -e "${RED}ERROR: No se pudo conectar al servidor${NC}"
    echo ""
    echo "Verifica:"
    echo "  1. El servidor está ejecutándose en Azure VM"
    echo "  2. Los puertos 1099 y 1100 están abiertos en Azure NSG"
    echo "  3. Tu conexión a internet está funcionando"
fi
