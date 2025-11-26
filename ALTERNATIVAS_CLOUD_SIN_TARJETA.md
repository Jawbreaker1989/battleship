# ALTERNATIVAS CLOUD SIN TARJETA DE CRÉDITO
## Para Proyecto Batalla Naval RMI - Sistemas Distribuidos

**Última actualización:** 25 de Noviembre de 2025

---

## 🚫 POR QUÉ AZURE 13-17 AÑOS NO FUNCIONA

### Servicios Incluidos en Azure para 13-17 años:
- ❌ **Azure App Services** → Solo aplicaciones web (HTTP), no soporta RMI
- ❌ **Azure Functions** → Serverless, sin estado, incompatible con RMI
- ❌ **Notification Hubs** → Solo notificaciones push móviles
- ❌ **MySQL** → Base de datos, no necesitas esto
- ❌ **Application Insights** → Telemetría, no necesitas esto
- ❌ **Azure DevOps** → CI/CD, no es una VM

### ❌ EL GRAN PROBLEMA:
**NO incluye Azure Virtual Machines (VMs)**, que es lo que necesitas para ejecutar tu servidor RMI Java.

### ¿Por qué necesitas una VM?
Tu proyecto Java RMI requiere:
1. ✅ Máquina con sistema operativo completo (Ubuntu/Windows)
2. ✅ Poder instalar Java 11
3. ✅ IP pública accesible
4. ✅ Puertos abiertos (1099, 1100)
5. ✅ Proceso corriendo 24/7

**Azure App Services y Functions NO permiten esto.** Son para aplicaciones web y funciones sin estado.

---

## ✅ ALTERNATIVAS QUE SÍ FUNCIONAN (SIN TARJETA)

### Ranking de Opciones (de mejor a peor):

```
🥇 1. Oracle Cloud Always Free         ⭐⭐⭐⭐⭐
🥈 2. Azure for Students (18+)          ⭐⭐⭐⭐⭐
🥉 3. Google Cloud (con tarjeta virtual) ⭐⭐⭐⭐☆
4. Render.com                           ⭐⭐⭐☆☆
5. Heroku Free (depricated)             ❌ YA NO EXISTE
```

---

## 🥇 OPCIÓN 1: ORACLE CLOUD ALWAYS FREE (RECOMENDADA)

### ⭐ ¿Por qué es LA MEJOR opción?

✅ **100% GRATIS PARA SIEMPRE** (no solo 12 meses)  
✅ **NO requiere tarjeta de crédito** (en la mayoría de países)  
✅ **2 VMs gratuitas PERMANENTES**  
✅ **MÁS potentes que AWS t2.micro**  
✅ **IP pública gratuita**  
✅ **Perfecto para Java RMI**  

### 📊 Comparación:

| Característica | Oracle Cloud | AWS Free Tier |
|----------------|--------------|---------------|
| Duración | **PERMANENTE** | 12 meses |
| VMs gratuitas | **2 VMs** | 1 VM |
| CPU | **1 OCPU ARM** o 1/8 OCPU x86 | 1 vCPU |
| RAM | **1 GB** | 1 GB |
| Almacenamiento | **100 GB** | 30 GB |
| Transferencia | **10 TB/mes** | 15 GB/mes |
| Tarjeta requerida | **NO** (mayoría países) | SÍ |
| Complejidad | Media | Fácil |

### 🎯 Especificaciones de la VM Gratuita:

**Opción A: AMD (x86) - Ampliamente disponible**
```
Tipo: VM.Standard.E2.1.Micro
CPU: 1/8 OCPU (aprox. 12.5% de 1 core físico)
RAM: 1 GB
Almacenamiento: 50 GB boot volume
Red: 480 Mbps
Cantidad: Hasta 2 VMs
```

**Opción B: ARM (Ampere) - MÁS POTENTE**
```
Tipo: VM.Standard.A1.Flex
CPU: 1 OCPU (1 core completo ARM)
RAM: 6 GB (puedes asignar hasta 24 GB entre tus VMs)
Almacenamiento: 50 GB boot volume × 2 = 100 GB
Cantidad: Puedes dividir los recursos
  - 1 VM con 4 OCPU + 24 GB RAM
  - 2 VMs con 2 OCPU + 12 GB RAM cada una
  - 4 VMs con 1 OCPU + 6 GB RAM cada una
```

**Para tu proyecto:** Una VM AMD E2.1.Micro es más que suficiente.

### 📝 PASO A PASO: CREAR CUENTA ORACLE CLOUD

#### **Paso 1: Registro**
1. Ve a: https://www.oracle.com/cloud/free/
2. Click "Start for free"
3. Selecciona tu país
4. Ingresa:
   - Email
   - Nombre y apellido
   - Nombre de compañía: "Universidad/Personal" (cualquiera)

#### **Paso 2: Verificación**
1. **Teléfono:** Recibirás código SMS
2. **Email:** Confirma tu correo
3. ⚠️ **Tarjeta:** Dependiendo del país:
   - **Colombia, México, Argentina:** Generalmente NO la piden
   - **USA, Europa:** Sí la piden (pero no cobran)
   - Si te la piden: prueba con tarjeta virtual (ver sección abajo)

#### **Paso 3: Configuración de Cuenta**
1. **Home Region:** Selecciona la más cercana
   - **Región recomendada:** `Brazil East (Sao Paulo)` ← Más cercano
   - Alternativas: `US East (Ashburn)`, `US West (Phoenix)`
   - ⚠️ **IMPORTANTE:** No puedes cambiar región después

2. **Cloud Account Name:** Elige un nombre único
   - Ejemplo: `battleship-cesar-2024`
   - Esto formará parte de tu URL

3. **Crear!**

#### **Paso 4: Configurar Presupuesto (Protección)**
1. Ve a: Cost Management → Budgets
2. "Create Budget"
3. Target: $0.01
4. Alert: Email cuando pase $0.01
5. Esto te alertará si hay algún cargo

### 🚀 LANZAR VM EN ORACLE CLOUD

#### **Paso 1: Ir a Compute**
1. Menú ☰ (hamburguesa) → Compute → Instances
2. Click "Create Instance"

#### **Paso 2: Configurar Instancia**

**Name:**
```
battleship-server
```

**Placement:**
```
✅ Availability Domain: (cualquiera que muestre "Always Free-eligible")
```

**Image and Shape:**

Click "Change Image":
```
✅ Canonical Ubuntu 22.04
  (Minimal o aarch64 si usas ARM)
```

Click "Change Shape":
```
✅ Ampere (ARM) → VM.Standard.A1.Flex
   - OCPU: 1
   - Memory: 6 GB
   
O si no está disponible:

✅ AMD → VM.Standard.E2.1.Micro
   - OCPU: 1/8
   - Memory: 1 GB
```

**Networking:**
```
✅ Create new virtual cloud network
✅ Assign a public IPv4 address
```

**Add SSH Keys:**
```
Opción 1: Generate SSH key pair (recomendado)
  - Click "Generate a key pair for me"
  - Click "Save Private Key" (guardar .key)
  - Click "Save Public Key" (opcional)

Opción 2: Subir tu propia clave pública
  - Si ya tienes clave SSH
```

**Boot Volume:**
```
✅ Default (50 GB) - Suficiente
```

#### **Paso 3: Launch!**
1. Click "Create"
2. Espera 1-2 minutos
3. Estado cambiará a "RUNNING" (verde)
4. **Anota la IP pública** (Public IP Address)

#### **Paso 4: Configurar Firewall (Security List)**

Oracle Cloud tiene firewall por defecto MUY restrictivo.

1. En tu instancia, click en "Virtual Cloud Network"
2. Click en "Security Lists" → "Default Security List"
3. "Add Ingress Rules":

**Regla 1: RMI Registry**
```
Source CIDR: 0.0.0.0/0
IP Protocol: TCP
Destination Port: 1099
Description: RMI Registry
```

**Regla 2: RMI Server**
```
Source CIDR: 0.0.0.0/0
IP Protocol: TCP
Destination Port: 1100
Description: RMI Server
```

4. "Add Ingress Rules"

### 🔐 CONECTARSE POR SSH

**Linux/Mac/Git Bash:**
```bash
# Dale permisos a la clave privada
chmod 400 ~/Downloads/ssh-key-*.key

# Conectar
ssh -i ~/Downloads/ssh-key-*.key ubuntu@TU_IP_PUBLICA
```

**Windows (PuTTY):**
1. Descargar PuTTYgen
2. Load → Selecciona tu .key
3. "Save private key" como .ppk
4. Abre PuTTY
5. Host Name: `ubuntu@TU_IP_PUBLICA`
6. Connection → SSH → Auth → Browse → Selecciona .ppk
7. Open

### ⚙️ CONFIGURAR FIREWALL DEL SISTEMA OPERATIVO

Oracle Ubuntu viene con firewall de OS también:

```bash
# Conectado por SSH, ejecuta:

# Ver reglas actuales
sudo iptables -L -n

# Abrir puerto RMI
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 1099 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 1100 -j ACCEPT

# Guardar reglas (persistente al reiniciar)
sudo netfilter-persistent save

# O puedes deshabilitar firewall (menos seguro pero más simple)
sudo systemctl disable iptables
sudo systemctl stop iptables
```

### 📦 INSTALAR JAVA Y MAVEN

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

# Verificar
java -version
mvn -version
git --version
```

### 🎮 DESPLEGAR TU PROYECTO

Igual que AWS, sigue los pasos de la sección 7 de la guía principal.

```bash
# Clonar proyecto
git clone https://github.com/TuUsuario/battleship-rmi.git
cd battleship-rmi

# Compilar
mvn clean package

# Ejecutar
./server-cloud.sh
```

### 💡 VENTAJAS EXTRAS DE ORACLE CLOUD

✅ **Permanente:** No expira en 12 meses  
✅ **2 VMs:** Puedes tener servidor de respaldo  
✅ **100 GB:** Más espacio que AWS  
✅ **10 TB/mes:** Transferencia generosa  
✅ **Gratis para siempre:** Ideal para proyectos personales  

### ⚠️ DESVENTAJAS

❌ **Disponibilidad:** A veces las VMs gratuitas se agotan en ciertas regiones  
❌ **Complejidad:** Firewall más complicado que AWS  
❌ **Documentación:** Menos recursos que AWS  
❌ **Performance:** ARM puede tener incompatibilidades (raro con Java)  

---

## 🥈 OPCIÓN 2: AZURE FOR STUDENTS (18+)

### ⚠️ REQUIERE SER ESTUDIANTE UNIVERSITARIO (18+)

Si tienes **18 años o más** y un **correo electrónico institucional (.edu)**, puedes usar Azure for Students.

### ✅ Beneficios:

- **$100 USD en créditos** (renuevan cada año)
- **12 meses gratis** de servicios selectos
- **NO requiere tarjeta de crédito**
- Incluye **Azure Virtual Machines** (B1s)

### 📝 Cómo Registrarse:

1. Ve a: https://azure.microsoft.com/en-us/free/students/
2. Click "Activate now"
3. Inicia sesión con cuenta Microsoft (o crea una)
4. Verifica con:
   - **Correo institucional** (.edu, .edu.co, etc.)
   - O **Carnet estudiantil** (foto)
5. ¡Aprobado! Recibes $100 créditos

### 🖥️ Crear VM en Azure:

**Especificaciones Gratuitas:**
```
Tipo: B1s
CPU: 1 vCPU
RAM: 1 GB
Almacenamiento: 64 GB
IP Pública: Incluida
Duración: 12 meses o hasta gastar $100
```

**Paso a paso:**
1. Azure Portal → Virtual Machines → Create
2. Basics:
   - VM name: `battleship-server`
   - Region: `Brazil South` o `East US`
   - Image: `Ubuntu Server 22.04 LTS`
   - Size: `B1s` (Free services eligible)
   - Authentication: SSH public key
3. Networking:
   - Public IP: Yes
   - Inbound ports: 22, 1099, 1100
4. Review + Create

**Configurar y usar:**
Igual que AWS/Oracle, luego instalas Java, despliegas tu proyecto.

### 💰 Gestión de Créditos:

- Monitorea créditos en: Cost Management + Billing
- $100 dura ~3-4 meses con 1 VM B1s 24/7
- Configura alertas de gasto

---

## 🥉 OPCIÓN 3: GOOGLE CLOUD (CON TARJETA VIRTUAL)

### 💳 Usar Tarjeta Virtual (Trucos Legales)

Si no tienes tarjeta física, puedes crear una virtual:

**Servicios de Tarjeta Virtual Gratuitos:**

1. **Mercado Pago** (Latinoamérica):
   - Crea cuenta en Mercado Pago
   - Solicita tarjeta virtual
   - Carga $1-5 USD
   - Usa para verificar Google Cloud

2. **Nequi** (Colombia):
   - App Nequi → Tarjeta virtual
   - Recarga $1 USD equivalente
   - Usa para verificación

3. **DaviPlata** (Colombia):
   - Tarjeta virtual disponible
   - Recarga mínima

**Con Google Cloud:**
- $300 USD gratis por 90 días
- No cobra automáticamente después
- Sí requiere tarjeta (virtual sirve)

---

## 📊 COMPARACIÓN DETALLADA

| Característica | Oracle Cloud | Azure Students | AWS Free Tier | Google Cloud |
|----------------|--------------|----------------|---------------|--------------|
| **Tarjeta requerida** | NO* | NO | SÍ | SÍ |
| **Duración** | PERMANENTE | 12 meses | 12 meses | 90 días |
| **Créditos** | N/A | $100 USD | N/A | $300 USD |
| **VM gratuita** | 2 VMs | 1 VM | 1 VM | Varias |
| **RAM** | 1-6 GB | 1 GB | 1 GB | 0.6 GB |
| **IP Pública** | Gratis | Gratis | Gratis | Gratis |
| **Facilidad setup** | Media | Fácil | Fácil | Media |
| **Documentación** | Poca | Mucha | Mucha | Mucha |
| **Para tu proyecto** | ✅ IDEAL | ✅ Bueno | ✅ Bueno | ✅ Bueno |

*Oracle Cloud puede pedir tarjeta en algunos países

---

## 🎯 MI RECOMENDACIÓN PARA TI

### Escenario 1: No tienes tarjeta y <18 años
```
🥇 Oracle Cloud Always Free
   - Intenta primero sin tarjeta
   - Si pide tarjeta, pide ayuda a familiar
   - Alternativa: Tarjeta virtual Mercado Pago
```

### Escenario 2: Estudiante universitario 18+ con correo .edu
```
🥇 Azure for Students
   - Más fácil que Oracle Cloud
   - $100 USD suficiente para el semestre
   - Sin tarjeta
```

### Escenario 3: Puedes conseguir tarjeta (virtual o prestada)
```
🥇 AWS Free Tier
   - Más documentación
   - Más tutoriales
   - Más fácil de usar
```

---

## 🚀 PLAN DE ACCIÓN INMEDIATO (HOY)

### Opción A: Probar Oracle Cloud (30 minutos)

1. Ve a: https://www.oracle.com/cloud/free/
2. Regístrate con tu email
3. Si NO pide tarjeta → ¡Perfecto! Continúa
4. Si SÍ pide tarjeta → Prueba Opción B o C

### Opción B: Azure for Students (si aplicas)

1. Ve a: https://azure.microsoft.com/en-us/free/students/
2. Verifica con correo institucional o carnet
3. Activa $100 créditos
4. Crea VM

### Opción C: Crear Tarjeta Virtual + AWS/GCP

1. Descarga app Mercado Pago o Nequi
2. Crea cuenta
3. Solicita tarjeta virtual (24-48 horas)
4. Recarga $1-5 USD
5. Usa con AWS Free Tier

---

## 📞 RECURSOS DE AYUDA

### Oracle Cloud:
- Tutorial oficial: https://docs.oracle.com/en-us/iaas/Content/GSG/Tasks/launchinginstance.htm
- Video: "Oracle Cloud Always Free Tier Tutorial" (YouTube)
- Foro: https://community.oracle.com/

### Azure for Students:
- Portal: https://portal.azure.com/
- Tutorial: https://docs.microsoft.com/en-us/learn/modules/create-linux-virtual-machine-in-azure/
- Soporte: Incluido para estudiantes

### General:
- Stack Overflow: [oracle-cloud] tag
- Reddit: r/oraclecloud, r/AZURE
- YouTube: Busca "Oracle Cloud free VM tutorial"

---

## ❓ PREGUNTAS FRECUENTES

**P: ¿Oracle Cloud es confiable?**  
R: Sí, es de Oracle Corporation (empresa Fortune 500). Menos popular que AWS pero igualmente profesional.

**P: ¿Por qué Oracle da VMs gratis para siempre?**  
R: Estrategia de marketing para competir con AWS. Esperan que luego pagues por servicios adicionales.

**P: ¿Me cobrarán después?**  
R: NO si te quedas en Always Free tier. Configura alertas de billing para estar seguro.

**P: ¿La VM ARM funciona con Java?**  
R: Sí perfectamente. Java 11 tiene excelente soporte ARM. Tu código RMI funcionará sin cambios.

**P: ¿Puedo usar ambos (Oracle + Azure)?**  
R: ¡Sí! Puedes tener servidor en Oracle y respaldo en Azure. O úsalos para diferentes proyectos.

**P: ¿Cuál es más fácil para principiantes?**  
R: Azure for Students > AWS Free Tier > Oracle Cloud (en orden de facilidad)

---

## ✅ CONCLUSIÓN

### Para tu proyecto académico:

**La mejor opción es:**
1. **Oracle Cloud Always Free** (si no pide tarjeta en tu región)
2. **Azure for Students** (si tienes 18+ y correo .edu)
3. **AWS con tarjeta virtual** Mercado Pago/Nequi

### Próximos pasos:

1. ✅ **HOY:** Intenta registrarte en Oracle Cloud
2. ✅ **Si funciona:** Sigue pasos de lanzar VM (arriba)
3. ✅ **Si no funciona:** Prueba Azure for Students
4. ✅ **Último recurso:** Tarjeta virtual + AWS

### No te preocupes:

- Hay VARIAS opciones sin costo
- Tu profesor entenderá si usas Oracle en vez de AWS
- Lo importante es el concepto de cloud computing
- Todas las nubes son equivalentes para tu demo

---

**¿Necesitas ayuda específica con alguna plataforma?**  
Puedo darte guías detalladas para cualquiera de estas opciones.

¡Mucho éxito! 🚀☁️
