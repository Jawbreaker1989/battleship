# GUÍA PASO A PASO: DESPLEGAR BATALLA NAVAL EN AZURE
**Azure for Students - $100 USD Créditos**

---

## 🎯 OBJETIVO

Desplegar tu servidor de Batalla Naval RMI en Azure Virtual Machine para que sea accesible desde Internet.

---

## ✅ LO QUE YA TIENES

- ✅ Azure for Students activado
- ✅ $100 USD en créditos (dura ~8-12 meses)
- ✅ Sin tarjeta de crédito requerida
- ✅ Acceso al Azure Portal

---

## 📋 PASO 1: CREAR MÁQUINA VIRTUAL

### **1.1 Ir al Azure Portal**

1. Ve a: https://portal.azure.com/
2. Inicia sesión con tu cuenta de estudiante
3. Verás el Dashboard de Azure

### **1.2 Crear Virtual Machine**

1. En el menú izquierdo o buscador superior, busca: **"Virtual machines"**
2. Click en **"Create"** → **"Azure virtual machine"**

### **1.3 Configuración Básica (Basics)**

**Project details:**
```
Subscription: Azure for Students
Resource group: Click "Create new"
  - Name: battleship-rg
  - Click OK
```

**Instance details:**
```
Virtual machine name: battleship-server
Region: (US) East US  (o el más cercano a ti)
Availability options: No infrastructure redundancy required
Security type: Standard
Image: Ubuntu Server 22.04 LTS - x64 Gen2
VM architecture: x64
Size: Click "See all sizes"
  → Busca: B1s (1 vCPU, 1 GB RAM) ← MÁS BARATO
  → O: B2s (2 vCPU, 4 GB RAM) ← MÁS CÓMODO
  → Select: B1s para empezar
```

**Administrator account:**
```
Authentication type: Password (más simple)
  
Username: azureuser
Password: [Crea contraseña fuerte, ANÓTALA]
  Ejemplo: BattleShip2024!@
Confirm password: [Repite la contraseña]
```

**Inbound port rules:**
```
Public inbound ports: Allow selected ports
Select inbound ports: 
  ✅ SSH (22)
  ✅ HTTP (80)  [opcional]
```

⚠️ **IMPORTANTE:** Abriremos más puertos después (1099, 1100 para RMI).

Click **"Next: Disks >"**

### **1.4 Configuración de Discos (Disks)**

```
OS disk type: Standard SSD (locally-redundant storage)
  ← Barato y suficiente

Delete with VM: ✅ (Check)
```

Click **"Next: Networking >"**

### **1.5 Configuración de Red (Networking)**

**Network interface:**
```
Virtual network: (default) - battleship-rg-vnet
Subnet: (default)
Public IP: (new) battleship-server-ip
NIC network security group: Basic
Public inbound ports: Allow selected ports
Select inbound ports:
  ✅ SSH (22)
```

**⚠️ Importante:** Configuraremos puertos RMI después.

Click **"Review + create"**

### **1.6 Revisar y Crear**

1. Azure validará tu configuración
2. Verifica:
   - Size: B1s (~$10/mes, tienes $100)
   - OS: Ubuntu 22.04
   - Authentication: Password
3. Click **"Create"**

**⏱️ Espera 2-3 minutos** mientras Azure crea tu VM.

### **1.7 Obtener IP Pública**

Cuando termine:
1. Click **"Go to resource"**
2. En Overview, verás:
   - **Public IP address:** `20.xxx.xxx.xxx`
3. **📝 ANOTA ESTA IP** - la necesitarás

---

## 📋 PASO 2: CONFIGURAR FIREWALL (PUERTOS RMI)

### **2.1 Abrir Puertos RMI**

Tu VM ya está corriendo, pero necesita puertos abiertos para RMI.

1. En tu VM, menú izquierdo → **"Networking"** → **"Network settings"**
2. Click en el nombre del **"Network security group"**: `battleship-server-nsg`
3. En el menú izquierdo → **"Inbound security rules"**
4. Click **"+ Add"**

**Regla 1: RMI Registry (puerto 1099)**
```
Source: Any
Source port ranges: *
Destination: Any
Service: Custom
Destination port ranges: 1099
Protocol: TCP
Action: Allow
Priority: 310
Name: Allow-RMI-Registry
Description: Puerto para RMI Registry
```
Click **"Add"**

**Regla 2: RMI Server (puerto 1100)**
```
Source: Any
Source port ranges: *
Destination: Any
Service: Custom
Destination port ranges: 1100
Protocol: TCP
Action: Allow
Priority: 320
Name: Allow-RMI-Server
Description: Puerto para servidor RMI
```
Click **"Add"**

Espera 30 segundos a que las reglas se apliquen.

---

## 📋 PASO 3: CONECTARSE A LA VM POR SSH

### **3.1 Desde Windows (Git Bash o PowerShell)**

**Opción A: PowerShell / CMD**
```powershell
ssh azureuser@20.xxx.xxx.xxx
# Reemplaza con TU IP pública

# Te preguntará:
# Are you sure you want to continue connecting? 
# Escribe: yes

# Luego pide password:
# Ingresa la contraseña que creaste
```

**Opción B: Git Bash**
```bash
ssh azureuser@20.xxx.xxx.xxx
```

**Opción C: PuTTY (Windows)**
1. Descarga PuTTY: https://www.putty.org/
2. Host Name: `azureuser@20.xxx.xxx.xxx`
3. Port: 22
4. Click "Open"
5. Ingresa password

### **3.2 Verificar Conexión**

Si todo está bien, verás:
```bash
azureuser@battleship-server:~$
```

¡Estás dentro de tu VM en la nube! 🎉

---

## 📋 PASO 4: INSTALAR JAVA Y MAVEN

Ejecuta estos comandos **UNO POR UNO** en la VM:

```bash
# Actualizar el sistema
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

**Si todo muestra versiones correctas → ¡Perfecto!** ✅

---

## 📋 PASO 5: SUBIR TU PROYECTO A LA VM

### **Opción A: Desde GitHub (RECOMENDADO)**

Si tu proyecto está en GitHub:

```bash
# Clonar repositorio
git clone https://github.com/TuUsuario/battleship-rmi.git

# Entrar al directorio
cd battleship-rmi
```

### **Opción B: Subir desde tu Laptop (SCP)**

Si NO está en GitHub, desde **otra terminal** en tu laptop:

```bash
# Comprimir proyecto (en tu laptop)
cd d:/OneDrive/Escritorio/vscode-projects/
tar -czf battleship.tar.gz batlleship_CesarCaro_202221682/

# Copiar a Azure VM
scp battleship.tar.gz azureuser@20.xxx.xxx.xxx:~/
# Ingresa password cuando pida

# Luego en la VM (SSH):
tar -xzf battleship.tar.gz
cd batlleship_CesarCaro_202221682/
```

### **Opción C: Crear repositorio GitHub ahora**

En tu laptop:

```bash
# Si no has subido a GitHub
cd d:/OneDrive/Escritorio/vscode-projects/batlleship_CesarCaro_202221682/

# Inicializar git (si no está)
git init
git add .
git commit -m "Proyecto Batalla Naval RMI"

# Crear repo en GitHub.com (desde web)
# Luego:
git remote add origin https://github.com/TuUsuario/battleship-rmi.git
git branch -M main
git push -u origin main

# Ahora puedes clonar en Azure (Opción A)
```

---

## 📋 PASO 6: COMPILAR PROYECTO EN AZURE

Conectado por SSH a tu VM:

```bash
# Entrar al directorio del proyecto
cd battleship-rmi  # o batlleship_CesarCaro_202221682

# Compilar con Maven
mvn clean package

# Esto tardará 1-2 minutos la primera vez
# Descarga dependencias de Maven

# Verificar que compiló correctamente
ls -lh server/target/
ls -lh client/target/
ls -lh shared/target/

# Deberías ver archivos .jar
```

**Si ves errores de compilación:**
- Verifica que `pom.xml` esté correcto
- Asegúrate de tener Java 11

---

## 📋 PASO 7: CREAR SCRIPT PARA SERVIDOR AZURE

### **7.1 Crear script de ejecución**

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

# Obtener IP pública de Azure
# Opción 1: Desde metadata de Azure
PUBLIC_IP=$(curl -s -H Metadata:true "http://169.254.169.254/metadata/instance/network/interface/0/ipv4/ipAddress/0/publicIpAddress?api-version=2021-02-01&format=text")

# Si no funciona, usar la que anotaste
if [ -z "$PUBLIC_IP" ]; then
    echo "No se pudo detectar IP automáticamente."
    echo "Ingresa la IP pública de tu VM Azure:"
    read PUBLIC_IP
fi

echo "📡 IP Pública detectada: $PUBLIC_IP"
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

### **7.2 Dar permisos de ejecución**

```bash
chmod +x server-azure.sh
```

---

## 📋 PASO 8: EJECUTAR SERVIDOR

### **8.1 Ejecución Simple (Para Probar)**

```bash
./server-azure.sh
```

Deberías ver:
```
================================================
  BATTLESHIP RMI - SERVIDOR EN AZURE CLOUD
================================================

📡 IP Pública detectada: 20.xxx.xxx.xxx

🚀 Iniciando servidor RMI en puerto 1100...
   Hostname: 20.xxx.xxx.xxx

🚀 Iniciando Servidor de Batalla Naval Distribuido (LAN) ...

✅ Servicio de juego creado
✅ Registry creado en puerto 1100
✅ Servicio publicado como 'GameService'

╔═══════════════════════════════════════════════════════╗
║        SERVIDOR BATALLA NAVAL DISTRIBUIDO (LAN)       ║
╠═══════════════════════════════════════════════════════╣
║ 🌐 Host/IP: 20.xxx.xxx.xxx                           ║
║ 🔌 Puerto RMI: 1100                                   ║
║ 📡 Servicio: GameService                              ║
║ 🎮 Capacidad: 2 jugadores simultáneos                 ║
║ 📊 Estado: Esperando conexiones de clientes...        ║
╚═══════════════════════════════════════════════════════╝
```

**¡Servidor corriendo en la nube!** 🎉

Para detener: `Ctrl+C`

### **8.2 Ejecución en Background (24/7)**

Para que siga corriendo después de cerrar SSH:

```bash
# Ejecutar en background con nohup
nohup ./server-azure.sh > server.log 2>&1 &

# Ver el log en tiempo real
tail -f server.log

# Presiona Ctrl+C para salir del log (servidor sigue corriendo)

# Ver procesos Java corriendo
ps aux | grep ServerMain

# Para detener servidor
pkill -f ServerMain
```

---

## 📋 PASO 9: CONECTAR CLIENTES DESDE TU LAPTOP

### **9.1 En tu Laptop Windows**

```bash
# Asegúrate de tener compilado el cliente
cd d:/OneDrive/Escritorio/vscode-projects/batlleship_CesarCaro_202221682/

# Si no has compilado:
mvn clean compile -pl shared,client

# Ejecutar cliente conectando a Azure
java -Djava.security.policy=all.policy ^
     -cp shared\target\classes;client\target\classes ^
     co.edu.uptc.client.ClientMain 20.xxx.xxx.xxx 1100

# Reemplaza 20.xxx.xxx.xxx con TU IP pública de Azure
```

O usa el script que creamos antes:

```bash
# Ejecutar client-cloud.bat
client-cloud.bat

# Cuando te pida IP, ingresa:
20.xxx.xxx.xxx
```

### **9.2 Desde Otra Computadora (Compañero)**

Tu compañero solo necesita:

1. **Opción A: Clonar tu repositorio**
```bash
git clone https://github.com/TuUsuario/battleship-rmi.git
cd battleship-rmi
mvn clean compile -pl shared,client
java -Djava.security.policy=all.policy \
     -cp shared/target/classes:client/target/classes \
     co.edu.uptc.client.ClientMain 20.xxx.xxx.xxx 1100
```

2. **Opción B: Ejecutable JAR** (si lo empaquetaste)
```bash
java -jar battleship-client-jar-with-dependencies.jar 20.xxx.xxx.xxx 1100
```

**¡Ahora pueden jugar desde cualquier lugar del mundo!** 🌍

---

## 📋 PASO 10: VERIFICAR QUE TODO FUNCIONA

### **10.1 Checklist de Verificación**

En Azure SSH:
```bash
# Ver que el servidor está corriendo
ps aux | grep ServerMain

# Ver log del servidor
tail -50 server.log

# Ver conexiones en puerto 1100
sudo netstat -tulpn | grep 1100
```

En tu laptop:
```bash
# Probar conectividad al puerto RMI
telnet 20.xxx.xxx.xxx 1100
# Si conecta, está funcionando

# O con PowerShell:
Test-NetConnection -ComputerName 20.xxx.xxx.xxx -Port 1100
```

### **10.2 Troubleshooting Común**

**Problema: Cliente no puede conectar**
```bash
# Verificar firewall Azure
# Portal Azure → VM → Networking → verificar reglas 1099, 1100

# Verificar servidor corriendo
ps aux | grep ServerMain

# Ver logs de error
tail -100 server.log
```

**Problema: "Connection refused"**
```bash
# Verificar que el servidor usa IP pública correcta
# En server.log debe mostrar: 20.xxx.xxx.xxx (no 10.x.x.x)

# Reiniciar servidor con IP correcta
pkill -f ServerMain
./server-azure.sh
```

---

## 📋 PASO 11: MONITOREAR USO DE CRÉDITOS

### **11.1 Ver Costos en Azure Portal**

1. Azure Portal → **"Cost Management + Billing"**
2. **"Cost analysis"**
3. Verás:
   - Gasto actual: ~$10-15/mes
   - Créditos restantes: $90-85
   - Tiempo restante: ~8-10 meses

### **11.2 Configurar Alertas**

1. Cost Management → **"Budgets"**
2. **"+ Add"**
3. Configurar:
```
Budget name: Alert-$50
Amount: $50
Alert conditions:
  - Al 50% ($50): Email alerta
  - Al 80% ($80): Email alerta
  - Al 100% ($100): Email alerta
```

---

## 📋 PASO 12: DETENER/ELIMINAR RECURSOS (DESPUÉS DEL PROYECTO)

### **12.1 Detener VM (Conserva datos, no gasta créditos)**

```bash
# Desde Azure Portal:
VM → Stop

# O desde Azure CLI:
az vm stop --resource-group battleship-rg --name battleship-server
az vm deallocate --resource-group battleship-rg --name battleship-server
```

**Mientras esté detenida: NO GASTA CRÉDITOS** ✅

### **12.2 Eliminar Todo (Cuando termines el curso)**

```bash
# Azure Portal:
Resource groups → battleship-rg → Delete resource group

# Escribe el nombre para confirmar: battleship-rg
# Delete
```

**Esto libera todos los recursos y detiene cobros permanentemente.**

---

## 🎓 PARA LA DEMOSTRACIÓN AL PROFESOR

### **Qué Mostrar (15-20 min)**

**1. Azure Portal (5 min)**
```
- Iniciar sesión en portal.azure.com
- Mostrar:
  * Virtual Machine corriendo
  * IP pública
  * Configuración de firewall (puertos abiertos)
  * Cost Management (uso de créditos)
```

**2. SSH en Vivo (3 min)**
```
- Conectarte por SSH a la VM
- Mostrar:
  * java -version (servidor tiene Java)
  * ps aux | grep ServerMain (proceso corriendo)
  * tail server.log (logs del servidor)
```

**3. Jugar Partida (10 min)**
```
- Laptop 1 (tuyo): Ejecutar cliente
- Laptop 2 (compañero): Ejecutar cliente
- Jugar partida completa:
  * Colocar barcos
  * Turnos
  * Ataques
  * Victoria
```

**4. Explicar Conceptos (5 min)**
```
"Migré mi servidor RMI de red local a Microsoft Azure.

Apliqué cloud computing usando:
- IaaS (Infrastructure as a Service)
- Virtual Machine en la nube
- IP pública accesible globalmente
- Firewall configurado (Network Security Groups)
- Sin costo ($100 créditos Azure for Students)

Mi servidor está en un datacenter de Microsoft,
accesible desde cualquier lugar con Internet.

Esto demuestra sistemas distribuidos en producción."
```

---

## 📊 COSTOS ESTIMADOS

### **VM B1s (1 vCPU, 1 GB RAM)**
```
Costo: ~$10-12 USD/mes
Tienes: $100 USD
Duración: ~8-10 meses

Para tu proyecto (1-2 meses): ~$20-24 USD
Sobran: ~$76-80 USD
```

### **VM B2s (2 vCPU, 4 GB RAM)** - Si necesitas más potencia
```
Costo: ~$30 USD/mes
Duración: ~3 meses
Suficiente para proyecto + prácticas
```

**Recomendación:** Empieza con B1s. Si es lento, cambia a B2s.

---

## ✅ CHECKLIST COMPLETO

### **Configuración Inicial**
- ☐ Azure for Students activado
- ☐ $100 créditos verificados
- ☐ VM creada (battleship-server)
- ☐ IP pública anotada
- ☐ Firewall configurado (puertos 22, 1099, 1100)

### **Instalación**
- ☐ Conectado por SSH
- ☐ Java 11 instalado
- ☐ Maven instalado
- ☐ Git instalado
- ☐ Proyecto subido/clonado

### **Deployment**
- ☐ Proyecto compilado (`mvn clean package`)
- ☐ Script `server-azure.sh` creado
- ☐ Servidor ejecutándose
- ☐ Logs verificados

### **Testing**
- ☐ Cliente conecta desde laptop
- ☐ Segundo cliente conecta (compañero)
- ☐ Partida completa funciona
- ☐ Turnos alternan correctamente
- ☐ Victoria se detecta

### **Documentación**
- ☐ Capturas de Azure Portal
- ☐ Capturas de SSH + logs
- ☐ Capturas de juego funcionando
- ☐ Documento técnico preparado

---

## 🚀 PRÓXIMOS PASOS

**Hoy:**
1. ✅ Crear VM Azure (15 min)
2. ✅ Configurar firewall (5 min)
3. ✅ Conectar SSH (2 min)
4. ✅ Instalar Java/Maven (5 min)

**Mañana:**
5. ✅ Subir proyecto (10 min)
6. ✅ Compilar (5 min)
7. ✅ Crear script y ejecutar servidor (10 min)
8. ✅ Probar con clientes (30 min)

**Pasado mañana:**
9. ✅ Capturas de pantalla
10. ✅ Preparar documento
11. ✅ Practicar demostración

---

## 📞 RECURSOS ADICIONALES

**Documentación Azure:**
- [Azure VMs](https://docs.microsoft.com/azure/virtual-machines/)
- [Azure for Students](https://azure.microsoft.com/free/students/)
- [SSH to Linux VM](https://docs.microsoft.com/azure/virtual-machines/linux/ssh-from-windows)

**Troubleshooting:**
- [Network Security Groups](https://docs.microsoft.com/azure/virtual-network/network-security-groups-overview)
- [Azure Cost Management](https://docs.microsoft.com/azure/cost-management-billing/)

**Comunidad:**
- Stack Overflow: [azure] tag
- Microsoft Q&A: [Azure Virtual Machines](https://docs.microsoft.com/answers/topics/azure-virtual-machines.html)

---

## 🎉 ¡FELICIDADES!

Ahora tienes TODO lo necesario para:
- ✅ Desplegar tu servidor en Azure
- ✅ Hacerlo accesible desde Internet
- ✅ Demostrar cloud computing real
- ✅ Aprobar tu proyecto con éxito

**¡Manos a la obra!** 🚀☁️

---

**Autor:** Guía creada para César Caro  
**Fecha:** 25 de Noviembre de 2025  
**Proyecto:** Batalla Naval RMI → Azure Cloud  
**Versión:** 1.0
