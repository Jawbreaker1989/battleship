#!/bin/bash
# Script de deployment para Azure VM
# Configura el servidor de Batalla Naval en Ubuntu Azure VM

set -e  # Salir si hay algún error

echo "🚀 Iniciando deployment en Azure VM..."
echo "=========================================="

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuración
AZURE_PUBLIC_IP="68.211.112.149"
REGISTRY_PORT=1099
SERVICE_PORT=1100
REPO_URL="https://github.com/Jawbreaker1989/battleship.git"
PROJECT_DIR="$HOME/battleship"

echo -e "${YELLOW}📋 Configuración:${NC}"
echo "   IP Pública: $AZURE_PUBLIC_IP"
echo "   Puerto Registry: $REGISTRY_PORT"
echo "   Puerto Servicio: $SERVICE_PORT"
echo ""

# 1. Actualizar sistema
echo -e "${GREEN}[1/7] Actualizando sistema...${NC}"
sudo apt-get update -y
sudo apt-get upgrade -y

# 2. Instalar Java 11
echo -e "${GREEN}[2/7] Instalando OpenJDK 11...${NC}"
sudo apt-get install -y openjdk-11-jdk

java -version

# 3. Instalar Maven
echo -e "${GREEN}[3/7] Instalando Maven...${NC}"
sudo apt-get install -y maven

mvn -version

# 4. Instalar Git
echo -e "${GREEN}[4/7] Instalando Git...${NC}"
sudo apt-get install -y git

# 5. Clonar o actualizar repositorio
echo -e "${GREEN}[5/7] Obteniendo código fuente...${NC}"
if [ -d "$PROJECT_DIR" ]; then
    echo "   Repositorio existente, actualizando..."
    cd "$PROJECT_DIR"
    git pull
else
    echo "   Clonando repositorio..."
    git clone "$REPO_URL" "$PROJECT_DIR"
    cd "$PROJECT_DIR"
fi

# 6. Compilar proyecto
echo -e "${GREEN}[6/7] Compilando proyecto con Maven...${NC}"
mvn clean package -DskipTests

# 7. Configurar firewall (UFW)
echo -e "${GREEN}[7/7] Configurando firewall local...${NC}"
sudo ufw allow 22/tcp     # SSH
sudo ufw allow $REGISTRY_PORT/tcp  # RMI Registry
sudo ufw allow $SERVICE_PORT/tcp   # RMI Service
sudo ufw --force enable

echo ""
echo -e "${GREEN}✅ Deployment completado exitosamente!${NC}"
echo ""
echo -e "${YELLOW}📝 Próximos pasos:${NC}"
echo "   1. Verificar Azure NSG (Network Security Group) permite puertos $REGISTRY_PORT y $SERVICE_PORT"
echo "   2. Ejecutar servidor: ./run-server.sh"
echo "   3. Desde clientes remotos: java -cp shared/target/classes;client/target/classes co.edu.uptc.client.ClientMain"
echo ""
