# GUÍA COMPLETA: MIGRACIÓN DE BATALLA NAVAL RMI A CLOUD COMPUTING

**Proyecto:** Batalla Naval Distribuida → Cloud Computing  
**Estudiante:** César Caro (202221682)  
**Nivel:** Básico-Intermedio - Sistemas Distribuidos  
**Objetivo:** Desplegar servidor RMI en AWS EC2 (100% GRATUITO)

---

## 📋 TABLA DE CONTENIDOS

1. [¿Qué es Cloud Computing?](#1-qué-es-cloud-computing)
2. [¿Qué es AWS y EC2?](#2-qué-es-aws-y-ec2)
3. [AWS Free Tier - Capa Gratuita](#3-aws-free-tier---capa-gratuita)
4. [Estrategia de Migración](#4-estrategia-de-migración)
5. [Paso a Paso: Configuración AWS](#5-paso-a-paso-configuración-aws)
6. [Modificaciones al Código](#6-modificaciones-al-código)
7. [Despliegue en EC2](#7-despliegue-en-ec2)
8. [Conexión de Clientes](#8-conexión-de-clientes)
9. [Troubleshooting](#9-troubleshooting)
10. [Demostración para el Profesor](#10-demostración-para-el-profesor)

---

## 1. ¿QUÉ ES CLOUD COMPUTING?

### 1.1 Definición Simple
**Cloud Computing** (computación en la nube) es usar computadoras y servidores de Internet en lugar de tu propia computadora para ejecutar programas.

### 1.2 En Tu Proyecto
**ANTES (RMI Local/LAN):**
```
Tu Laptop ---------------- Laptop de tu Amigo
    ↓                            ↓
[Servidor RMI]  ←→  [Cliente]  [Cliente]
(En tu red local - 192.168.x.x)
```

**DESPUÉS (Cloud Computing):**
```
Tu Laptop                        Laptop de tu Amigo
    ↓                                   ↓
[Cliente] ←→  INTERNET  ←→  [Cliente]
                ↓
        [Servidor RMI en AWS]
        (En la nube - IP pública)
```

### 1.3 Beneficios para tu Proyecto
✅ **Acceso desde cualquier lugar** (no solo LAN)  
✅ **El servidor está 24/7** (no necesitas tu laptop prendida)  
✅ **IP pública fija** (cualquiera puede conectarse)  
✅ **Escalabilidad** (puedes agregar más servidores)  
✅ **Aprendes tecnología profesional**  

---

## 2. ¿QUÉ ES AWS Y EC2?

### 2.1 AWS (Amazon Web Services)
**AWS** es la plataforma de cloud computing de Amazon. Es como "rentar computadoras de Amazon por Internet".

**Servicios principales:**
- **EC2** = Servidores virtuales (lo que usarás)
- **S3** = Almacenamiento de archivos
- **RDS** = Bases de datos
- **Lambda** = Funciones sin servidor

### 2.2 EC2 (Elastic Compute Cloud)
**EC2** es un servicio que te da una **máquina virtual en la nube**.

**Piensa en EC2 como:**
- Una computadora virtual en Internet
- Tiene su propio sistema operativo (Linux/Windows)
- Tiene una IP pública
- Puedes instalarle Java, Maven, etc.
- Funciona 24/7

**Tipos de instancias EC2:**
- **t2.micro** = Pequeña (1 vCPU, 1 GB RAM) ← **GRATIS**
- **t2.small** = Mediana (1 vCPU, 2 GB RAM) ← De pago
- **t2.medium** = Grande (2 vCPU, 4 GB RAM) ← De pago

### 2.3 Regiones de AWS
AWS tiene "data centers" en todo el mundo:
- **us-east-1** (Virginia, USA) ← Más popular
- **us-west-2** (Oregon, USA)
- **eu-west-1** (Irlanda)
- **sa-east-1** (São Paulo, Brasil) ← Más cercano a Colombia

📍 **Recomendación:** Usa `us-east-1` (es la más estable y con más recursos gratuitos)

---

## 3. AWS FREE TIER - CAPA GRATUITA

### 3.1 ¿Qué es Free Tier?
AWS ofrece **servicios gratuitos por 12 meses** para estudiantes y nuevos usuarios.

### 3.2 EC2 Free Tier - Límites Gratuitos
✅ **750 horas/mes** de instancia t2.micro  
✅ **30 GB de almacenamiento EBS**  
✅ **15 GB de transferencia de datos** (salida)  
✅ **1 año gratis** (desde que creas la cuenta)

**Traducción:**
- Si dejas tu servidor prendido 24/7, usas: 24h × 30 días = 720 horas ✅ GRATIS
- 750 horas > 720 horas = ¡Te sobran horas!

### 3.3 Requisitos para Free Tier
⚠️ **Necesitas:**
- Correo electrónico
- Número de teléfono (verificación SMS)
- **Tarjeta de crédito/débito** (NO te cobrarán si no pasas los límites)

🔒 **Seguridad:**
- AWS pide tarjeta para verificar identidad
- NO te cobran si te quedas en Free Tier
- Puedes configurar alertas de gasto
- Puedes poner límite de $0

### 3.4 Alternativas si NO tienes tarjeta
Si realmente no puedes conseguir una tarjeta, hay alternativas:

**Opción 1: AWS Educate**
- Programa gratuito para estudiantes
- No requiere tarjeta de crédito
- $100 USD en créditos
- Requiere correo institucional (.edu)
- Regístrate en: https://aws.amazon.com/education/awseducate/

**Opción 2: GitHub Student Pack**
- Incluye $200 USD en créditos AWS
- No requiere tarjeta
- Requiere ser estudiante verificado
- Regístrate en: https://education.github.com/pack

**Opción 3: Oracle Cloud Always Free**
- Similar a AWS
- No requiere tarjeta en algunos países
- 2 instancias gratuitas PERMANENTES
- https://www.oracle.com/cloud/free/

**Opción 4: Google Cloud Platform (GCP)**
- $300 USD gratis por 90 días
- Requiere tarjeta pero no cobra automáticamente
- https://cloud.google.com/free

---

## 4. ESTRATEGIA DE MIGRACIÓN

### 4.1 Arquitectura Actual vs. Cloud

**ACTUAL (LAN):**
```
┌─────────────────────────────────────┐
│       TU RED LOCAL (192.168.x.x)    │
│                                     │
│  Laptop 1 (Servidor)                │
│  - ServerMain                       │
│  - GameServiceImpl                  │
│  - Puerto 1100                      │
│                                     │
│  Laptop 2 (Cliente)                 │
│  - ClientMain                       │
│  - Conecta a 192.168.x.x:1100       │
└─────────────────────────────────────┘
```

**CLOUD (AWS EC2):**
```
┌──────────────────────────────────────────┐
│           AWS CLOUD (Internet)           │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │   EC2 Instance (t2.micro)          │  │
│  │   IP: 3.82.145.xxx (pública)       │  │
│  │   OS: Ubuntu Linux 22.04           │  │
│  │   Java: 11                         │  │
│  │   - ServerMain                     │  │
│  │   - GameServiceImpl                │  │
│  │   - Puerto 1100                    │  │
│  └────────────────────────────────────┘  │
│              ↑                           │
└──────────────┼───────────────────────────┘
               │
    INTERNET (Firewall, Security Groups)
               │
     ┌─────────┼─────────┐
     ↓                   ↓
┌─────────┐        ┌─────────┐
│ Laptop 1│        │ Laptop 2│
│ Cliente │        │ Cliente │
└─────────┘        └─────────┘
Desde casa        Desde universidad
```

### 4.2 Cambios Necesarios

#### **A. Código (Mínimos)**
✅ Configurar IP pública en servidor  
✅ Agregar scripts de despliegue  
✅ Configurar puertos RMI correctamente  

#### **B. Infraestructura (AWS)**
✅ Crear cuenta AWS  
✅ Lanzar instancia EC2  
✅ Configurar Security Groups (firewall)  
✅ Conectarse por SSH  

#### **C. Operación**
✅ Subir código a EC2  
✅ Instalar Java en EC2  
✅ Ejecutar servidor  
✅ Conectar clientes desde cualquier lugar  

### 4.3 Cronograma Estimado
- **Día 1:** Crear cuenta AWS, configurar EC2 (2 horas)
- **Día 2:** Modificar código, probar localmente (2 horas)
- **Día 3:** Desplegar en EC2, configurar firewall (2 horas)
- **Día 4:** Pruebas y ajustes (1 hora)
- **Día 5:** Preparar demostración (1 hora)

**Total:** ~8 horas (1 semana de trabajo moderado)

---

## 5. PASO A PASO: CONFIGURACIÓN AWS

### 5.1 Crear Cuenta AWS

#### **Paso 1: Registro**
1. Ve a: https://aws.amazon.com/
2. Click en "Create an AWS Account" (Crear cuenta)
3. Ingresa:
   - Email
   - Password
   - Nombre de cuenta: "Battleship-Cloud-[TuNombre]"

#### **Paso 2: Información de Contacto**
1. Tipo de cuenta: **Personal**
2. Nombre completo
3. Teléfono
4. Dirección

#### **Paso 3: Método de Pago**
1. Ingresa tarjeta de crédito/débito
2. ⚠️ **Tranquilo:** No te cobrarán si usas Free Tier
3. AWS carga $1 USD para verificar (lo devuelven inmediatamente)

#### **Paso 4: Verificación de Identidad**
1. Ingresa número de teléfono
2. Recibirás código por SMS
3. Ingresa el código

#### **Paso 5: Plan de Soporte**
1. Selecciona: **Basic Support - Free**
2. NO necesitas pagar por soporte

#### **Paso 6: Confirmación**
1. ¡Cuenta creada!
2. Espera 5-10 minutos para activación completa
3. Recibirás email de confirmación

### 5.2 Configurar Alertas de Facturación (IMPORTANTE)

⚠️ **Esto te protege de cobros accidentales**

#### **Paso 1: Ir a Billing**
1. En consola AWS, click tu nombre (arriba derecha)
2. "Billing and Cost Management"

#### **Paso 2: Crear Alerta**
1. "Budgets" → "Create budget"
2. Template: "Zero spend budget"
3. Budget name: "AlertaGratuito"
4. Email: tu correo
5. "Create budget"

Ahora recibirás email si AWS cobra algo.

### 5.3 Lanzar Instancia EC2

#### **Paso 1: Ir a EC2**
1. En consola AWS: Services → EC2
2. O busca "EC2" en el buscador superior
3. **Región:** Verifica que diga "N. Virginia" (us-east-1)

#### **Paso 2: Launch Instance**
1. Click "Launch Instance" (botón naranja)
2. Verás un formulario

#### **Paso 3: Configuración**

**Name:**
```
battleship-server
```

**Application and OS Images (AMI):**
```
✅ Ubuntu Server 22.04 LTS (HVM), SSD Volume Type
   - Free tier eligible
   - 64-bit (x86)
```

**Instance type:**
```
✅ t2.micro
   - 1 vCPU
   - 1 GB RAM
   - Free tier eligible ✅
```

**Key pair (login):**
```
1. Click "Create new key pair"
2. Name: battleship-key
3. Type: RSA
4. Format: .pem (para Mac/Linux) o .ppk (para Windows con PuTTY)
   📍 Si usas Windows, selecciona .pem (usaremos Git Bash)
5. Click "Create key pair"
6. ⚠️ IMPORTANTE: Guarda el archivo .pem en lugar seguro
   (Lo necesitarás para conectarte por SSH)
```

**Network settings:**
```
✅ Create security group
   Name: battleship-sg
   Description: Security group for Battleship RMI server
   
   Rules:
   1. SSH (22) - Source: My IP (para conectarte tú)
   2. Custom TCP (1100) - Source: Anywhere (0.0.0.0/0) (para RMI)
   3. Custom TCP (1099) - Source: Anywhere (0.0.0.0/0) (para RMI Registry)
```

⚠️ **IMPORTANTE:** Necesitas abrir puertos 1099 y 1100 para RMI.

**Configure storage:**
```
✅ 8 GB gp3 (Free tier: hasta 30 GB)
   (8 GB es suficiente para tu proyecto)
```

#### **Paso 4: Launch!**
1. Click "Launch instance" (botón naranja)
2. Espera ~30 segundos
3. ¡Instancia creada!

#### **Paso 5: Obtener IP Pública**
1. Click en tu instancia (checkbox)
2. En el panel inferior, copia:
   - **Public IPv4 address**: `3.82.145.xxx` (ejemplo)
   - **Public IPv4 DNS**: `ec2-3-82-145-xxx.compute-1.amazonaws.com`

📝 **Anota esta IP**, la necesitarás para conectar clientes.

### 5.4 Configurar Security Group (Firewall)

Si olvidaste agregar los puertos RMI:

1. EC2 → Instances → Click tu instancia
2. Tab "Security" → Click en security group
3. "Edit inbound rules"
4. "Add rule":
   ```
   Type: Custom TCP
   Port: 1099
   Source: Anywhere-IPv4 (0.0.0.0/0)
   Description: RMI Registry
   ```
5. "Add rule":
   ```
   Type: Custom TCP
   Port: 1100
   Source: Anywhere-IPv4 (0.0.0.0/0)
   Description: RMI Server
   ```
6. "Save rules"

---

## 6. MODIFICACIONES AL CÓDIGO

### 6.1 Problema de RMI en Cloud

**Desafío:**
RMI fue diseñado para redes locales. En cloud hay que configurar correctamente las IPs públicas.

**Parámetros críticos de RMI:**
```java
// Servidor necesita saber su IP PÚBLICA (no privada de AWS)
java.rmi.server.hostname = IP_PUBLICA_EC2

// Usar puerto fijo (no aleatorio)
java.rmi.server.useLocalHostname = false
```

### 6.2 Modificar ServerMain.java

**Ubicación:** `server/src/main/java/co/edu/uptc/server/ServerMain.java`

#### **Cambio 1: Detectar IP Pública**

Agrega este método nuevo:

```java
/**
 * Detecta la IP pública de la instancia EC2
 * Lee la variable de entorno RMI_SERVER_HOSTNAME
 */
private static String detectPublicIp() {
    // Opción 1: Variable de entorno (recomendado para cloud)
    String envHost = System.getenv("RMI_SERVER_HOSTNAME");
    if (envHost != null && !envHost.isBlank()) {
        LOGGER.info("Usando IP desde variable de entorno: " + envHost);
        return envHost;
    }
    
    // Opción 2: Argumento de línea de comandos
    // (ya se maneja en el main actual)
    
    // Opción 3: Detectar IP pública desde AWS metadata (solo EC2)
    try {
        URL url = new URL("http://169.254.169.254/latest/meta-data/public-ipv4");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(2000);
        
        if (conn.getResponseCode() == 200) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String publicIp = reader.readLine();
                LOGGER.info("IP pública detectada desde EC2 metadata: " + publicIp);
                return publicIp;
            }
        }
    } catch (Exception e) {
        LOGGER.warning("No se pudo detectar IP desde EC2 metadata: " + e.getMessage());
    }
    
    // Fallback: usar detección LAN (código actual)
    return detectLanIp();
}
```

**Agregar imports necesarios:**
```java
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
```

#### **Cambio 2: Modificar main() para usar IP pública**

Reemplaza esta línea en el `main()`:
```java
// ANTES:
String host = (argHost != null) ? argHost : (detected != null ? detected : "localhost");

// DESPUÉS:
String detected = detectPublicIp();  // Ahora detecta IP pública si está en EC2
String host = (argHost != null) ? argHost : (detected != null ? detected : "localhost");
```

#### **Cambio 3: Configurar propiedades RMI correctamente**

Modifica la sección de propiedades RMI:
```java
// Propiedades RMI - CONFIGURACIÓN PARA CLOUD
System.setProperty("java.rmi.server.hostname", host);
System.setProperty("java.rmi.server.useLocalHostname", "false");  // NUEVO
System.setProperty("java.security.policy", "all.policy");
System.setProperty("sun.rmi.transport.tcp.responseTimeout", "10000");
System.setProperty("sun.rmi.transport.tcp.readTimeout", "10000");

LOGGER.info("java.rmi.server.hostname configurado a: " + host);
```

### 6.3 Nuevo Script de Ejecución para Cloud

Crea un nuevo archivo: `server-cloud.sh`

```bash
#!/bin/bash
# Script para ejecutar servidor en AWS EC2

echo "================================================"
echo "  BATTLESHIP RMI - SERVIDOR CLOUD (AWS EC2)"
echo "================================================"
echo ""

# Detectar IP pública desde metadata de EC2
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)

if [ -z "$PUBLIC_IP" ]; then
    echo "⚠️  No se detectó IP pública de EC2."
    echo "Ingresa la IP pública manualmente:"
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

Dale permisos de ejecución:
```bash
chmod +x server-cloud.sh
```

### 6.4 Script de Cliente para Conectar a Cloud

Crea: `client-cloud.bat` (Windows) o `client-cloud.sh` (Linux/Mac)

**client-cloud.bat:**
```batch
@echo off
title Cliente Cloud - Batalla Naval

echo ============================================
echo       CLIENTE CLOUD - BATALLA NAVAL
echo ============================================
echo.

set /p SERVER_IP="Ingresa la IP pública del servidor EC2: "

if "%SERVER_IP%"=="" (
    echo Error: Debes ingresar una IP
    pause
    exit /b 1
)

echo.
echo Conectando a servidor cloud: %SERVER_IP%:1100
echo.

java -Djava.security.policy=all.policy ^
     -cp shared\target\classes;client\target\classes ^
     co.edu.uptc.client.ClientMain %SERVER_IP% 1100

pause
```

**client-cloud.sh:**
```bash
#!/bin/bash

echo "============================================"
echo "       CLIENTE CLOUD - BATALLA NAVAL"
echo "============================================"
echo ""

read -p "Ingresa la IP pública del servidor EC2: " SERVER_IP

if [ -z "$SERVER_IP" ]; then
    echo "Error: Debes ingresar una IP"
    exit 1
fi

echo ""
echo "Conectando a servidor cloud: $SERVER_IP:1100"
echo ""

java -Djava.security.policy=all.policy \
     -cp shared/target/classes:client/target/classes \
     co.edu.uptc.client.ClientMain $SERVER_IP 1100
```

### 6.5 Verificar Cambios con Git

```bash
# Ver archivos modificados
git status

# Ver cambios específicos
git diff server/src/main/java/co/edu/uptc/server/ServerMain.java

# Confirmar cambios
git add .
git commit -m "Migración a cloud computing - AWS EC2 compatible"
```

---

## 7. DESPLIEGUE EN EC2

### 7.1 Conectarse a EC2 por SSH

#### **Opción A: Usar Git Bash (Windows) o Terminal (Mac/Linux)**

1. **Ubicar tu archivo .pem:**
   - Lo descargaste al crear la instancia
   - Ejemplo: `battleship-key.pem`

2. **Dar permisos correctos (Mac/Linux/Git Bash):**
```bash
chmod 400 battleship-key.pem
```

3. **Conectarse:**
```bash
ssh -i battleship-key.pem ubuntu@3.82.145.xxx
# Reemplaza 3.82.145.xxx con TU IP pública de EC2
```

4. **Primera vez te preguntará:**
```
Are you sure you want to continue connecting (yes/no)?
```
Escribe: `yes`

5. **Estás dentro!**
```
ubuntu@ip-172-31-xx-xx:~$
```

#### **Opción B: Usar PuTTY (Windows alternativo)**

Si descargaste .ppk:
1. Abre PuTTY
2. Host Name: `ubuntu@3.82.145.xxx`
3. Connection → SSH → Auth → Browse → Selecciona .ppk
4. Click "Open"

### 7.2 Instalar Java y Maven en EC2

Una vez conectado por SSH:

```bash
# Actualizar sistema
sudo apt update
sudo apt upgrade -y

# Instalar Java 11
sudo apt install openjdk-11-jdk -y

# Verificar instalación
java -version
# Debe mostrar: openjdk version "11.x.x"

# Instalar Maven
sudo apt install maven -y

# Verificar Maven
mvn -version
# Debe mostrar: Apache Maven 3.x.x

# Instalar Git (para clonar tu proyecto)
sudo apt install git -y
```

### 7.3 Subir tu Proyecto a EC2

#### **Opción 1: Desde GitHub (Recomendado)**

Si tu proyecto está en GitHub:

```bash
# En EC2, clonar repositorio
git clone https://github.com/TuUsuario/battleship-rmi.git
cd battleship-rmi
```

Si tu proyecto es privado:
```bash
# Necesitarás configurar SSH keys o usar Personal Access Token
git clone https://github.com/TuUsuario/battleship-rmi.git
# Te pedirá username/password (usa token como password)
```

#### **Opción 2: SCP (Secure Copy) desde tu laptop**

Desde tu laptop (otra terminal, NO dentro de SSH):

```bash
# Comprimir tu proyecto
cd d:/OneDrive/Escritorio/vscode-projects/
tar -czf battleship.tar.gz batlleship_CesarCaro_202221682/

# Copiar a EC2
scp -i battleship-key.pem battleship.tar.gz ubuntu@3.82.145.xxx:~/

# Luego en EC2 (SSH):
tar -xzf battleship.tar.gz
cd batlleship_CesarCaro_202221682/
```

#### **Opción 3: GitHub con HTTPS**

```bash
# En tu laptop:
cd d:/OneDrive/Escritorio/vscode-projects/batlleship_CesarCaro_202221682/

# Si no has inicializado git:
git init
git add .
git commit -m "Proyecto inicial"

# Crear repositorio en GitHub (desde web)
# Luego:
git remote add origin https://github.com/TuUsuario/battleship-rmi.git
git push -u origin main
# (Te pedirá user/password, usa Personal Access Token como password)

# En EC2:
git clone https://github.com/TuUsuario/battleship-rmi.git
```

### 7.4 Compilar Proyecto en EC2

```bash
# Dentro del directorio del proyecto en EC2
cd battleship-rmi  # o batlleship_CesarCaro_202221682

# Compilar con Maven
mvn clean package

# Verificar que se crearon los JARs
ls -lh server/target/
ls -lh client/target/
ls -lh shared/target/

# Deberías ver:
# - shared-1.0-SNAPSHOT.jar
# - server-1.0-SNAPSHOT.jar
# - client-1.0-SNAPSHOT.jar (o battleship-client-jar-with-dependencies.jar)
```

### 7.5 Ejecutar Servidor en EC2

#### **Forma Manual:**
```bash
# Obtener IP pública de EC2
export PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
echo "Mi IP pública es: $PUBLIC_IP"

# Ejecutar servidor
java -Djava.rmi.server.hostname=$PUBLIC_IP \
     -Djava.rmi.server.useLocalHostname=false \
     -Djava.security.policy=all.policy \
     -cp shared/target/shared-1.0-SNAPSHOT.jar:server/target/server-1.0-SNAPSHOT.jar \
     co.edu.uptc.server.ServerMain $PUBLIC_IP 1100
```

#### **Forma con Script:**
```bash
# Usar el script que creamos
./server-cloud.sh
```

#### **Ejecutar en Background (para que siga corriendo después de cerrar SSH):**
```bash
# Usar nohup (no hang up)
nohup ./server-cloud.sh > server.log 2>&1 &

# Ver log en tiempo real
tail -f server.log

# Para detener presiona Ctrl+C (solo sal del log, el servidor sigue)

# Para ver procesos Java corriendo
ps aux | grep java

# Para matar el servidor
pkill -f ServerMain
```

#### **Forma Profesional: Usar systemd service**

Crear servicio que inicie automáticamente:

```bash
sudo nano /etc/systemd/system/battleship.service
```

Contenido:
```ini
[Unit]
Description=Battleship RMI Server
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/battleship-rmi
ExecStart=/usr/bin/java -Djava.rmi.server.hostname=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4) -Djava.rmi.server.useLocalHostname=false -Djava.security.policy=all.policy -cp shared/target/shared-1.0-SNAPSHOT.jar:server/target/server-1.0-SNAPSHOT.jar co.edu.uptc.server.ServerMain $(curl -s http://169.254.169.254/latest/meta-data/public-ipv4) 1100
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Comandos:
```bash
# Recargar systemd
sudo systemctl daemon-reload

# Iniciar servicio
sudo systemctl start battleship

# Ver estado
sudo systemctl status battleship

# Ver logs
sudo journalctl -u battleship -f

# Habilitar para que inicie automáticamente al reiniciar EC2
sudo systemctl enable battleship

# Detener servicio
sudo systemctl stop battleship
```

---

## 8. CONEXIÓN DE CLIENTES

### 8.1 Cliente desde tu Laptop

En tu laptop local (Windows):

```bash
# Compilar si no lo has hecho
mvn clean compile -pl shared,client

# Conectar al servidor EC2
java -Djava.security.policy=all.policy ^
     -cp shared\target\classes;client\target\classes ^
     co.edu.uptc.client.ClientMain 3.82.145.xxx 1100
     
# Reemplaza 3.82.145.xxx con la IP PÚBLICA de tu EC2
```

O usa el script:
```bash
client-cloud.bat
# Te pedirá la IP, ingrésala: 3.82.145.xxx
```

### 8.2 Cliente desde Otra Computadora

1. **Opción A: Distribuir JAR ejecutable**

En tu laptop:
```bash
# Copiar archivos necesarios
mkdir cliente-portable
cp client/target/battleship-client-jar-with-dependencies.jar cliente-portable/
cp all.policy cliente-portable/
cp client-cloud.bat cliente-portable/

# Comprimir
tar -czf cliente-portable.zip cliente-portable/
```

Envía `cliente-portable.zip` a tu compañero.

Él solo necesita:
```bash
# Descomprimir
# Ejecutar:
client-cloud.bat
# Ingresar IP: 3.82.145.xxx
```

2. **Opción B: Desde GitHub**

Tu compañero clona el repo:
```bash
git clone https://github.com/TuUsuario/battleship-rmi.git
cd battleship-rmi
mvn clean compile -pl shared,client
./client-cloud.sh
# Ingresa IP: 3.82.145.xxx
```

### 8.3 Verificar Conexión

**En el cliente verás:**
```
🚀 Iniciando Cliente de Batalla Naval...
Conectando al servidor RMI en 3.82.145.xxx:1100
Conexión exitosa al servidor RMI
```

**En el servidor (EC2 logs):**
```
✅ Jugador 'Juan' conectado desde 181.xxx.xxx.xxx
📊 Jugadores activos: 1
```

---

## 9. TROUBLESHOOTING

### 9.1 Error: "Connection refused"

**Síntoma:**
```
java.rmi.ConnectException: Connection refused to host: 3.82.145.xxx
```

**Causas y Soluciones:**

1. **Firewall de AWS (Security Groups)**
   - Verifica que puertos 1099 y 1100 estén abiertos
   - EC2 → Security Groups → Inbound rules
   - Debe tener: Custom TCP 1099 y 1100, Source: 0.0.0.0/0

2. **Servidor no está corriendo**
   - Conéctate por SSH y verifica:
   ```bash
   ps aux | grep ServerMain
   # Si no muestra nada, el servidor no está corriendo
   ```

3. **IP incorrecta**
   - Verifica la IP pública de EC2:
   ```bash
   curl http://169.254.169.254/latest/meta-data/public-ipv4
   ```
   - Compara con la que usas en el cliente

### 9.2 Error: "java.rmi.server.hostname not set correctly"

**Síntoma:**
Cliente se conecta pero RMI falla al invocar métodos.

**Solución:**
```bash
# En EC2, verificar que la propiedad esté configurada
echo $RMI_SERVER_HOSTNAME

# Debe mostrar la IP pública
# Si no, exportar:
export RMI_SERVER_HOSTNAME=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)

# Reiniciar servidor
```

### 9.3 Error: "Port already in use"

**Síntoma:**
```
java.rmi.server.ExportException: Port already in use: 1100
```

**Solución:**
```bash
# Ver qué está usando el puerto
sudo lsof -i :1100

# Matar el proceso
sudo kill -9 [PID]

# O reiniciar servicio systemd
sudo systemctl restart battleship
```

### 9.4 Servidor se cae después de cerrar SSH

**Síntoma:**
Cuando cierras la terminal SSH, el servidor se detiene.

**Solución:**
Usa `nohup` o `systemd`:
```bash
# Con nohup
nohup java ... > server.log 2>&1 &

# O mejor, usa systemd (ver sección 7.5)
sudo systemctl start battleship
```

### 9.5 "Free tier exceeded" - Alertas de costo

**Prevención:**
1. **Configurar Budget Alerts** (ver sección 5.2)
2. **Monitorear uso:**
   - AWS Console → Billing → Free Tier
   - Verifica horas de EC2 usadas

3. **Si pasa los límites:**
   - Detén la instancia cuando no la uses:
   ```bash
   # Desde consola AWS
   EC2 → Instances → Select → Instance State → Stop
   ```
   - **IMPORTANTE:** Stop ≠ Terminate
   - Stop = Apagar (puedes reiniciar después, mantienes datos)
   - Terminate = Eliminar (pierdes todo)

### 9.6 No puedo conectarme por SSH

**Soluciones:**

1. **Verificar Security Group tiene regla SSH:**
   ```
   Type: SSH
   Port: 22
   Source: My IP (o 0.0.0.0/0)
   ```

2. **Permisos del archivo .pem:**
   ```bash
   chmod 400 battleship-key.pem
   ```

3. **Usuario correcto:**
   ```bash
   # Ubuntu AMI usa usuario 'ubuntu'
   ssh -i battleship-key.pem ubuntu@IP
   
   # Amazon Linux AMI usa 'ec2-user'
   ssh -i battleship-key.pem ec2-user@IP
   ```

---

## 10. DEMOSTRACIÓN PARA EL PROFESOR

### 10.1 Script de Demostración

Prepara este guion de demostración:

#### **1. Mostrar Arquitectura (5 min)**

PowerPoint o dibujo en pizarra:
```
[Computadora 1] ←→ INTERNET ←→ [AWS EC2 - Servidor RMI] ←→ INTERNET ←→ [Computadora 2]
                                        ↓
                                  IP Pública: 3.82.145.xxx
                                  Región: us-east-1
                                  Instancia: t2.micro (Free Tier)
```

**Explicar:**
- "El servidor ya no está en mi laptop, está en la nube de Amazon (AWS)"
- "Cualquier persona con Internet puede conectarse, no solo en LAN"
- "El servidor está disponible 24/7, no necesito mi computadora prendida"
- "Esto es cloud computing: usar recursos de cómputo remotos vía Internet"

#### **2. Mostrar Consola AWS (3 min)**

Compartir pantalla y mostrar:
1. AWS Console → EC2 Dashboard
2. Mostrar instancia corriendo (estado: running)
3. Mostrar IP pública
4. Mostrar Security Groups (puertos abiertos)
5. Mostrar región (us-east-1)

**Explicar:**
- "Esta es mi máquina virtual en AWS"
- "Tiene 1 vCPU y 1 GB RAM (t2.micro - gratis)"
- "Configuré firewall para abrir puertos RMI (1099, 1100)"

#### **3. Conectarse por SSH y Mostrar Servidor (5 min)**

```bash
# En terminal (compartir pantalla)
ssh -i battleship-key.pem ubuntu@3.82.145.xxx

# Mostrar procesos corriendo
ps aux | grep ServerMain

# Mostrar logs del servidor
tail -50 server.log

# O si usas systemd:
sudo systemctl status battleship
sudo journalctl -u battleship -n 50
```

**Explicar:**
- "Estoy conectándome remotamente al servidor en AWS"
- "El servidor RMI está corriendo aquí, en esta máquina virtual"
- "Los logs muestran jugadores conectados y eventos del juego"

#### **4. Conectar Clientes y Jugar (10 min)**

**Laptop 1 (la tuya):**
```bash
# Ejecutar cliente
client-cloud.bat
# Ingresar IP: 3.82.145.xxx
# Ingresar nombre: "Profesor"
```

**Laptop 2 (compañero o simulador):**
```bash
# Ejecutar cliente
client-cloud.bat
# Ingresar IP: 3.82.145.xxx
# Ingresar nombre: "Estudiante"
```

**Jugar partida completa:**
- Colocar barcos
- Turnos alternados
- Mostrar que funciona en tiempo real
- Mostrar estadísticas

**Mientras juegas, en SSH mostrar logs en vivo:**
```bash
tail -f server.log
# Se verán ataques, cambios de turno, etc.
```

#### **5. Mostrar Código Modificado (5 min)**

Abrir VS Code:

**ServerMain.java cambios:**
```java
// Mostrar método detectPublicIp()
// Mostrar configuración de java.rmi.server.hostname
```

**Explicar:**
- "RMI necesita saber la IP pública para funcionar en cloud"
- "Agregué detección automática de IP desde metadata de EC2"
- "Configuré propiedades del sistema para cloud"

**Scripts:**
```bash
# Mostrar server-cloud.sh
# Mostrar client-cloud.bat
```

**Explicar:**
- "Scripts específicos para deployment en cloud"
- "Detectan automáticamente IP pública"
- "Configuran variables de entorno correctas"

#### **6. Explicar Conceptos de Cloud Computing (5 min)**

**IaaS (Infrastructure as a Service):**
- "EC2 es IaaS: Amazon nos da infraestructura (máquinas virtuales)"
- "Nosotros instalamos y configuramos software (Java, nuestro servidor)"

**Elasticidad:**
- "Podemos escalar: si necesitamos más capacidad, creamos más instancias"
- "Actualmente usamos 1 instancia t2.micro (gratis)"
- "Podríamos agregar instancias más grandes si hubiera miles de jugadores"

**Alta Disponibilidad:**
- "El servidor está en un data center de AWS, con respaldos de energía, red, etc."
- "Es más confiable que mi laptop en casa"

**Pay-as-you-go:**
- "Solo pagamos por lo que usamos"
- "En nuestro caso: Free Tier, no pagamos nada"
- "Si escala, pagaríamos por hora de uso"

### 10.2 Documento Técnico para Entregar

Crea un PDF con:

**Portada:**
```
MIGRACIÓN A CLOUD COMPUTING
Batalla Naval Distribuida con Java RMI

Estudiante: César Caro (202221682)
Curso: Sistemas Distribuidos
Profesor: [Nombre]
Fecha: [Fecha]
```

**Contenido:**
1. Introducción (¿Qué es cloud computing?)
2. Arquitectura Original (RMI en LAN)
3. Arquitectura Migrada (RMI en AWS EC2)
4. Cambios Realizados (código, scripts)
5. Proceso de Deployment
6. Capturas de Pantalla:
   - AWS Console (EC2 Dashboard)
   - Terminal SSH conectado
   - Logs del servidor
   - Juego funcionando con IP pública
7. Conceptos Aplicados:
   - IaaS
   - Elasticidad
   - Alta Disponibilidad
   - Security Groups
   - IP Pública vs Privada
8. Conclusiones

### 10.3 Preguntas que Puede Hacer el Profesor

**P: ¿Por qué usaste EC2 y no otro servicio?**  
R: "EC2 es IaaS, nos da control total para instalar Java y ejecutar RMI. Otros servicios como Lambda (serverless) no soportan RMI porque necesita conexión persistente."

**P: ¿Esto funciona desde cualquier lugar del mundo?**  
R: "Sí, profesor. Cualquier persona con Internet puede conectarse usando la IP pública. Lo demostré conectando desde [mi casa y universidad / compañero en otra ciudad]."

**P: ¿Qué pasa si tu instancia EC2 se cae?**  
R: "Configuré systemd para que el servicio se reinicie automáticamente. También podría usar Auto Scaling Groups para crear otra instancia automáticamente, pero eso está fuera del Free Tier."

**P: ¿Cómo manejas la seguridad?**  
R: "Usé Security Groups de AWS (firewall) para solo abrir los puertos necesarios (1099, 1100 para RMI, 22 para SSH). En producción, agregaría autenticación y encriptación."

**P: ¿Cuánto cuesta esto?**  
R: "Usando Free Tier de AWS es 100% gratis por 12 meses. Después, una t2.micro cuesta ~$8 USD/mes. Configuré alertas de facturación para evitar cobros accidentales."

---

## ANEXO A: CHECKLIST COMPLETO

### ☐ Preparación (Día 1)
- ☐ Crear cuenta AWS
- ☐ Configurar alerta de billing ($0)
- ☐ Lanzar instancia EC2 (t2.micro, Ubuntu 22.04)
- ☐ Configurar Security Group (puertos 22, 1099, 1100)
- ☐ Guardar archivo .pem de forma segura
- ☐ Conectarse por SSH exitosamente

### ☐ Instalación en EC2 (Día 2)
- ☐ Instalar Java 11
- ☐ Instalar Maven
- ☐ Instalar Git
- ☐ Subir proyecto a EC2 (git clone o scp)
- ☐ Compilar proyecto en EC2 (`mvn clean package`)
- ☐ Verificar JARs creados

### ☐ Modificación de Código (Día 2-3)
- ☐ Agregar método `detectPublicIp()` en ServerMain
- ☐ Modificar configuración de propiedades RMI
- ☐ Crear script `server-cloud.sh`
- ☐ Crear script `client-cloud.bat`
- ☐ Compilar y probar localmente
- ☐ Commit cambios a Git

### ☐ Deployment (Día 3)
- ☐ Ejecutar servidor en EC2
- ☐ Verificar que escucha en puerto 1100
- ☐ Configurar para correr en background (nohup o systemd)
- ☐ Verificar logs
- ☐ Anotar IP pública

### ☐ Pruebas (Día 4)
- ☐ Conectar cliente desde tu laptop
- ☐ Conectar segundo cliente (compañero u otra PC)
- ☐ Jugar partida completa
- ☐ Verificar que funcione con diferentes redes
- ☐ Probar revancha
- ☐ Verificar estadísticas

### ☐ Documentación (Día 5)
- ☐ Capturas de pantalla de AWS Console
- ☐ Capturas de juego funcionando
- ☐ Capturas de logs
- ☐ Documento técnico (PDF)
- ☐ Script de demostración
- ☐ Preparar respuestas a preguntas

---

## ANEXO B: COMANDOS DE REFERENCIA RÁPIDA

### SSH
```bash
# Conectar (Linux/Mac/Git Bash)
ssh -i battleship-key.pem ubuntu@IP_PUBLICA

# Copiar archivos
scp -i battleship-key.pem archivo.txt ubuntu@IP_PUBLICA:~/
```

### EC2 - Gestión de Instancia
```bash
# Ver IP pública (desde dentro de EC2)
curl http://169.254.169.254/latest/meta-data/public-ipv4

# Ver procesos Java
ps aux | grep java

# Matar proceso
pkill -f ServerMain

# Ver uso de CPU/RAM
top
htop  # más bonito (instalar: sudo apt install htop)
```

### Servidor
```bash
# Compilar
mvn clean package

# Ejecutar en foreground
./server-cloud.sh

# Ejecutar en background
nohup ./server-cloud.sh > server.log 2>&1 &

# Ver logs en tiempo real
tail -f server.log

# Systemd
sudo systemctl start battleship
sudo systemctl status battleship
sudo systemctl stop battleship
sudo journalctl -u battleship -f
```

### Cliente
```bash
# Windows
client-cloud.bat

# Linux/Mac
./client-cloud.sh

# Manual
java -Djava.security.policy=all.policy \
     -cp shared/target/classes:client/target/classes \
     co.edu.uptc.client.ClientMain IP_PUBLICA 1100
```

---

## ANEXO C: RECURSOS ADICIONALES

### Tutoriales AWS
- [AWS EC2 para principiantes](https://aws.amazon.com/ec2/getting-started/)
- [AWS Free Tier](https://aws.amazon.com/free/)
- [AWS Educate](https://aws.amazon.com/education/awseducate/)

### Documentación Java RMI
- [Java RMI Tutorial Oracle](https://docs.oracle.com/javase/tutorial/rmi/)
- [RMI over Internet](https://docs.oracle.com/javase/8/docs/technotes/guides/rmi/faq.html)

### Videos Recomendados
- "AWS EC2 Tutorial for Beginners" (YouTube)
- "Java RMI Explained" (YouTube)
- "Cloud Computing Concepts" (Coursera/YouTube)

### Comunidades
- Stack Overflow: [java-rmi tag](https://stackoverflow.com/questions/tagged/java-rmi)
- AWS re:Post: [Foro oficial AWS](https://repost.aws/)

---

## CONCLUSIÓN

¡Felicidades! Ahora tienes una guía completa para migrar tu juego de Batalla Naval a cloud computing usando AWS EC2.

**Resumen de lo que lograrás:**
✅ Servidor RMI corriendo en la nube (AWS)  
✅ Acceso desde cualquier lugar del mundo  
✅ 100% gratuito (Free Tier)  
✅ Experiencia práctica en cloud computing  
✅ Demostración impresionante para el profesor  

**Próximos pasos:**
1. Crear cuenta AWS (hoy)
2. Lanzar EC2 y configurar (mañana)
3. Modificar código (1-2 días)
4. Desplegar y probar (1 día)
5. Preparar demostración (1 día)

**Tiempo total estimado:** 5-7 días de trabajo moderado.

**Recuerda:**
- No tengas miedo de experimentar (Free Tier te protege)
- Documenta todo con capturas
- Prueba con anticipación, no el día de la entrega
- Si algo falla, consulta el Troubleshooting (Sección 9)

¡Mucho éxito con tu proyecto! 🚀☁️

---

**Autor:** Guía creada para César Caro  
**Fecha:** 25 de Noviembre de 2025  
**Versión:** 1.0
