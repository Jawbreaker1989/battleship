# 🎮 Batalla Naval RMI - Deployment Azure VM

## 📋 Configuración Actual

- **IP Pública Azure**: `68.211.112.149`
- **Puerto RMI Registry**: `1099`
- **Puerto RMI Service**: `1100`
- **Usuario VM**: `azureadmin`
- **Contraseña VM**: `200710526074Acdc`

## 🚀 Deployment en Azure VM (Servidor)

### Paso 1: Conectar a la VM

```bash
ssh azureadmin@68.211.112.149
```

Contraseña: `200710526074Acdc`

### Paso 2: Subir archivos a la VM

Desde tu máquina local (Windows), sube los archivos:

```powershell
# Usando SCP (requiere OpenSSH instalado en Windows)
scp -r * azureadmin@68.211.112.149:~/battleship/
```

O sube el repositorio directamente desde GitHub (recomendado):

```bash
# En la VM
git clone https://github.com/Jawbreaker1989/battleship.git ~/battleship
cd ~/battleship
```

### Paso 3: Ejecutar deployment

```bash
cd ~/battleship
chmod +x deploy-azure.sh
./deploy-azure.sh
```

Este script:
- ✅ Actualiza el sistema
- ✅ Instala Java 11
- ✅ Instala Maven
- ✅ Compila el proyecto
- ✅ Configura el firewall local (UFW)

### Paso 4: Configurar Azure Network Security Group (NSG)

En Azure Portal:

1. Ve a tu VM → **Networking** → **Network Security Group**
2. Agrega reglas de entrada (Inbound rules):

**Regla 1: RMI Registry**
- Priority: 310
- Name: AllowRMIRegistry
- Port: 1099
- Protocol: TCP
- Source: Any
- Action: Allow

**Regla 2: RMI Service**
- Priority: 320
- Name: AllowRMIService
- Port: 1100
- Protocol: TCP
- Source: Any
- Action: Allow

### Paso 5: Ejecutar el servidor

```bash
cd ~/battleship
chmod +x run-server.sh
./run-server.sh
```

El servidor quedará escuchando en `68.211.112.149:1099`

## 💻 Clientes Remotos

### Windows

1. Compila el proyecto (solo la primera vez):
```cmd
mvn clean package
```

2. Ejecuta el cliente:
```cmd
run-client.bat
```

O manualmente:
```cmd
java -cp "shared\target\classes;client\target\classes" co.edu.uptc.client.ClientMain 68.211.112.149 1099
```

### Linux/Mac

1. Compila el proyecto (solo la primera vez):
```bash
mvn clean package
```

2. Ejecuta el cliente:
```bash
chmod +x run-client.sh
./run-client.sh
```

O manualmente:
```bash
java -cp "shared/target/classes:client/target/classes" co.edu.uptc.client.ClientMain 68.211.112.149 1099
```

## 🔧 Troubleshooting

### El cliente no puede conectarse

1. **Verificar que el servidor esté corriendo**:
```bash
ssh azureadmin@68.211.112.149
ps aux | grep ServerMain
```

2. **Verificar que los puertos estén abiertos**:
```bash
# En la VM
sudo ufw status
sudo netstat -tulpn | grep -E '1099|1100'
```

3. **Verificar NSG en Azure Portal**:
   - Asegúrate de que las reglas para puertos 1099 y 1100 estén activas

4. **Probar conectividad desde cliente**:
```bash
# En Windows (PowerShell)
Test-NetConnection -ComputerName 68.211.112.149 -Port 1099

# En Linux/Mac
telnet 68.211.112.149 1099
# o
nc -zv 68.211.112.149 1099
```

### El servidor se detiene al cerrar SSH

Ejecuta el servidor con `nohup` para que siga corriendo:

```bash
nohup ./run-server.sh > server.log 2>&1 &
```

O usa `screen` o `tmux`:

```bash
# Instalar screen
sudo apt-get install screen

# Iniciar sesión screen
screen -S battleship

# Ejecutar servidor
./run-server.sh

# Desconectar (Ctrl+A, luego D)
# Reconectar: screen -r battleship
```

### Reiniciar servidor

```bash
# Encontrar el proceso
ps aux | grep ServerMain

# Matar el proceso (reemplaza PID con el número del proceso)
kill <PID>

# Ejecutar nuevamente
./run-server.sh
```

## 📊 Monitoreo

### Ver logs del servidor

```bash
# Si usaste nohup
tail -f ~/battleship/server.log

# Ver procesos Java
jps -l
```

### Ver conexiones activas

```bash
sudo netstat -tulpn | grep -E '1099|1100'
```

## 🔐 Seguridad

**IMPORTANTE**: La contraseña actual es temporal. Cámbiala por seguridad:

```bash
ssh azureadmin@68.211.112.149
passwd
```

## 🎯 Resumen de Comandos Rápidos

**En VM (Servidor)**:
```bash
ssh azureadmin@68.211.112.149
cd ~/battleship
git pull  # Actualizar código
mvn clean package  # Recompilar
./run-server.sh  # Ejecutar
```

**En Cliente (Windows)**:
```cmd
run-client.bat
```

**En Cliente (Linux/Mac)**:
```bash
./run-client.sh
```

---

¿Problemas? Verifica:
1. ✅ Servidor corriendo en VM
2. ✅ NSG permite puertos 1099 y 1100
3. ✅ Firewall local (UFW) permite puertos
4. ✅ Cliente compilado correctamente
