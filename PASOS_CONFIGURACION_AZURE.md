# CONFIGURACIÓN POST-CREACIÓN DE VM EN AZURE

## ✅ VM CREADA EXITOSAMENTE

```
Nombre: battleship-server
IP Pública: 68.211.112.149
Sistema: Ubuntu 24.04 LTS
Usuario: azureuser
Región: Chile Central
Estado: ACTIVO ✅
```

---

## 🔥 PASO 1: ABRIR PUERTOS RMI EN EL FIREWALL

### **¿Por qué necesitamos esto?**
Tu servidor RMI usa los puertos:
- **1099** - RMI Registry
- **1100** - RMI Server

Actualmente solo el puerto **SSH (22)** está abierto. Necesitamos agregar los puertos RMI.

---

### **Método: Agregar reglas en Network Security Group**

**Paso a Paso:**

#### **1. Ir a tu VM en Azure Portal**

```
1. En Azure Portal (https://portal.azure.com/)
2. Busca "Máquinas virtuales" o "Virtual machines"
3. Click en "battleship-server"
```

#### **2. Ir a configuración de red**

```
En el menú izquierdo de tu VM:
1. Busca y click en "Redes" o "Networking"
2. Verás la configuración de red de tu VM
```

#### **3. Agregar regla para puerto 1099 (RMI Registry)**

```
1. Click en "Agregar regla de puerto de entrada" o "Add inbound port rule"
2. En el panel que se abre, configura:

   Origen (Source): Any
   Intervalos de puertos de origen (Source port ranges): *
   Destino (Destination): Any
   Servicio (Service): Custom
   Intervalos de puertos de destino (Destination port ranges): 1099
   Protocolo (Protocol): TCP
   Acción (Action): Permitir / Allow
   Prioridad (Priority): 310
   Nombre (Name): RMI-Registry
   Descripción (Description): Puerto para RMI Registry

3. Click en "Agregar" o "Add"
```

#### **4. Agregar regla para puerto 1100 (RMI Server)**

```
1. Click de nuevo en "Agregar regla de puerto de entrada"
2. Configura:

   Origen: Any
   Intervalos de puertos de origen: *
   Destino: Any
   Servicio: Custom
   Intervalos de puertos de destino: 1100
   Protocolo: TCP
   Acción: Permitir / Allow
   Prioridad: 320
   Nombre: RMI-Server
   Descripción: Puerto para servidor RMI

3. Click en "Agregar"
```

#### **5. Verificar las reglas**

Después de agregar ambas reglas, deberías ver en la lista:

```
✅ SSH (22) - Puerto 22 - Permitir
✅ RMI-Registry (310) - Puerto 1099 - Permitir
✅ RMI-Server (320) - Puerto 1100 - Permitir
```

---

## 🔐 PASO 2: CONECTARSE POR SSH

Una vez configurados los puertos, conéctate a tu VM:

### **Desde Windows (PowerShell, Git Bash o CMD):**

```bash
ssh azureuser@68.211.112.149
```

**La primera vez te preguntará:**
```
Are you sure you want to continue connecting (yes/no)?
```
Escribe: `yes`

**Luego te pedirá contraseña:**
```
Ingresa la contraseña que configuraste al crear la VM
```

**Si todo está bien, verás:**
```
azureuser@battleship-server:~$
```

¡Estás dentro de tu servidor en la nube! 🎉

---

## ☕ PASO 3: INSTALAR JAVA Y MAVEN

Ejecuta estos comandos **UNO POR UNO** en tu VM:

```bash
# Actualizar sistema
sudo apt update
sudo apt upgrade -y

# Instalar Java 11
sudo apt install openjdk-11-jdk -y

# Instalar Maven
sudo apt install maven -y

# Instalar Git
sudo apt install git -y

# Verificar instalaciones
java -version
# Debe mostrar: openjdk version "11.x.x"

mvn -version
# Debe mostrar: Apache Maven 3.x.x

git --version
# Debe mostrar: git version 2.x.x
```

---

## 📦 PASO 4: SUBIR Y COMPILAR TU PROYECTO

### **Opción A: Desde GitHub (Recomendado)**

Si tu proyecto está en GitHub:

```bash
# Clonar repositorio
git clone https://github.com/TuUsuario/battleship-rmi.git

# Entrar al directorio
cd battleship-rmi

# Compilar
mvn clean package
```

### **Opción B: Subir desde tu laptop (SCP)**

Desde **otra terminal** en tu laptop (NO en SSH):

```bash
# Comprimir proyecto
cd d:/OneDrive/Escritorio/vscode-projects/
tar -czf battleship.tar.gz batlleship_CesarCaro_202221682/

# Copiar a Azure VM
scp battleship.tar.gz azureuser@68.211.112.149:~/
# Ingresa contraseña cuando pida

# Luego en SSH:
tar -xzf battleship.tar.gz
cd batlleship_CesarCaro_202221682/
mvn clean package
```

---

## 🚀 PASO 5: EJECUTAR SERVIDOR RMI

### **Crear script de ejecución para Azure:**

```bash
# Crear script
nano server-azure.sh
```

**Contenido del script:**
```bash
#!/bin/bash
echo "================================================"
echo "  BATTLESHIP RMI - SERVIDOR EN AZURE CLOUD"
echo "================================================"
echo ""

# IP pública de Azure
PUBLIC_IP="68.211.112.149"

echo "📡 IP Pública: $PUBLIC_IP"
echo ""

# Exportar como variable de entorno
export RMI_SERVER_HOSTNAME=$PUBLIC_IP

# Configurar Java
export JAVA_OPTS="-Djava.rmi.server.hostname=$PUBLIC_IP \
                  -Djava.rmi.server.useLocalHostname=false \
                  -Djava.security.policy=all.policy \
                  -Dsun.rmi.transport.tcp.responseTimeout=10000 \
                  -Dsun.rmi.transport.tcp.readTimeout=10000"

echo "🚀 Iniciando servidor RMI en puerto 1100..."
echo "   Hostname: $PUBLIC_IP"
echo ""

# Ejecutar servidor
java $JAVA_OPTS \
     -cp shared/target/shared-1.0-SNAPSHOT.jar:server/target/server-1.0-SNAPSHOT.jar \
     co.edu.uptc.server.ServerMain $PUBLIC_IP 1100

echo ""
echo "✅ Servidor detenido"
```

**Guardar:**
- Presiona `Ctrl+O` (guardar)
- Presiona `Enter`
- Presiona `Ctrl+X` (salir)

**Dar permisos:**
```bash
chmod +x server-azure.sh
```

**Ejecutar servidor:**
```bash
./server-azure.sh
```

Deberías ver:
```
================================================
  BATTLESHIP RMI - SERVIDOR EN AZURE CLOUD
================================================

📡 IP Pública: 68.211.112.149

🚀 Iniciando servidor RMI en puerto 1100...
   Hostname: 68.211.112.149

🚀 Iniciando Servidor de Batalla Naval Distribuido (LAN) ...
✅ Servicio de juego creado
✅ Registry creado en puerto 1100
✅ Servicio publicado como 'GameService'

╔═══════════════════════════════════════════════════════╗
║        SERVIDOR BATALLA NAVAL DISTRIBUIDO (LAN)       ║
╠═══════════════════════════════════════════════════════╣
║ 🌐 Host/IP: 68.211.112.149                           ║
║ 🔌 Puerto RMI: 1100                                   ║
║ 📡 Servicio: GameService                              ║
║ 🎮 Capacidad: 2 jugadores simultáneos                 ║
║ 📊 Estado: Esperando conexiones de clientes...        ║
╚═══════════════════════════════════════════════════════╝
```

**¡Tu servidor está corriendo en la nube!** 🎉

---

## 🎮 PASO 6: CONECTAR CLIENTES DESDE TU LAPTOP

### **En tu laptop Windows:**

```bash
# Asegúrate de tener compilado el cliente
cd d:/OneDrive/Escritorio/vscode-projects/batlleship_CesarCaro_202221682/

# Si no has compilado:
mvn clean compile -pl shared,client

# Ejecutar cliente conectando a Azure
java -Djava.security.policy=all.policy ^
     -cp shared\target\classes;client\target\classes ^
     co.edu.uptc.client.ClientMain 68.211.112.149 1100
```

O usa tu script:
```bash
client-cloud.bat
# Cuando te pida IP, ingresa: 68.211.112.149
```

---

## 🎯 VERIFICAR QUE TODO FUNCIONA

### **Checklist final:**

- [ ] Puertos 1099 y 1100 abiertos en Azure
- [ ] SSH funciona (puedes conectarte)
- [ ] Java 11 instalado
- [ ] Maven instalado
- [ ] Proyecto compilado
- [ ] Servidor corriendo mostrando "Esperando conexiones..."
- [ ] Cliente puede conectarse desde laptop
- [ ] Puedes jugar una partida completa

---

## 🛠️ TROUBLESHOOTING COMÚN

### **Problema: Cliente no puede conectar**

```bash
# En Azure VM, verificar que el servidor está corriendo:
ps aux | grep ServerMain

# Ver logs del servidor:
tail -50 server.log

# Verificar puertos abiertos:
sudo netstat -tulpn | grep 1100
```

### **Problema: "Connection refused"**

Verifica:
1. Servidor está corriendo en Azure VM
2. Puertos 1099 y 1100 están abiertos en Azure Portal
3. IP pública es correcta: 68.211.112.149

### **Problema: Servidor se detiene al cerrar SSH**

Si quieres que el servidor siga corriendo después de cerrar SSH:

```bash
# Ejecutar en background con nohup
nohup ./server-azure.sh > server.log 2>&1 &

# Ver log en tiempo real
tail -f server.log

# Para detener presiona Ctrl+C (solo sal del log, el servidor sigue)

# Para matar el servidor después
pkill -f ServerMain
```

---

## 📝 INFORMACIÓN IMPORTANTE

```
IP PÚBLICA: 68.211.112.149
Usuario SSH: azureuser
Contraseña: [la que configuraste]
Puertos RMI: 1099, 1100
Puerto SSH: 22
Región: Chile Central
```

**¡Guarda esta información!**

---

## 🎉 ¡FELICIDADES!

Ahora tienes:
- ✅ Servidor en la nube (Azure)
- ✅ Accesible desde Internet
- ✅ IP pública fija
- ✅ Puertos RMI configurados
- ✅ Java y Maven instalados
- ✅ Listo para desplegar tu proyecto

**¡Esto ES cloud computing en acción!** 🚀☁️
