#!/bin/bash
# Script para ejecutar el servidor de Batalla Naval en Azure VM

set -e

# Configuración
AZURE_PUBLIC_IP="68.211.112.149"
PROJECT_DIR="$HOME/battleship"

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}🎮 Iniciando Servidor de Batalla Naval${NC}"
echo "=========================================="
echo "   IP Pública: $AZURE_PUBLIC_IP"
echo "   Puerto Registry: 1099"
echo "   Puerto Servicio: 1100"
echo ""

cd "$PROJECT_DIR"

# Verificar que el proyecto esté compilado
if [ ! -d "server/target/classes" ]; then
    echo -e "${YELLOW}⚠️  Proyecto no compilado, compilando...${NC}"
    mvn clean package -DskipTests
fi

# Ejecutar servidor
echo -e "${GREEN}🚀 Ejecutando servidor...${NC}"
echo ""

# Configurar variable de entorno (opcional, el código ya tiene la IP hardcodeada)
export BATTLESHIP_PUBLIC_IP="$AZURE_PUBLIC_IP"

# Ejecutar con política de seguridad
java -cp server/target/classes:shared/target/classes \
     -Djava.security.policy=all.policy \
     -Djava.rmi.server.hostname=$AZURE_PUBLIC_IP \
     co.edu.uptc.server.ServerMain $AZURE_PUBLIC_IP
