# 🎮 Batalla Naval Distribuida - Guía de Ejecución en Azure

## Descripción del Proyecto

Sistema distribuido de Batalla Naval usando **Java RMI** con soporte para conexiones remotas en Azure.

- **Servidor**: Coordina el juego entre dos clientes
- **Cliente**: Interfaz gráfica Swing con soporte para jugar remotamente
- **Comunicación**: Java RMI con callbacks asincronicos para evitar timeouts en redes WAN

## 🚀 Ejecución en Azure

### Opción 1: Script PowerShell (Recomendado)

#### Servidor en Azure:
```powershell
# En la máquina Azure
.\run-server-azure.ps1 -AzureIP 68.211.112.149 -Port 1100
```

#### Cliente Local (conectando a Azure):
```powershell
# En tu máquina local
.\run-client-azure.ps1 -AzureIP 68.211.112.149 -Port 1100
```

### Opción 2: Script Batch (.bat)

#### Servidor en Azure:
```batch
REM En la máquina Azure
run-server-azure.bat 68.211.112.149 1100
```

#### Cliente Local:
```batch
REM En tu máquina local
run-client-azure.bat 68.211.112.149 1100
```

### Opción 3: Comando directo

#### Servidor:
```bash
java -Djava.security.policy=all.policy \
     -Djava.rmi.server.hostname=68.211.112.149 \
     -cp "server/target/server-1.0-SNAPSHOT.jar:shared/target/shared-1.0-SNAPSHOT.jar" \
     co.edu.uptc.server.ServerMain 68.211.112.149 1100
```

#### Cliente:
```bash
java -cp "client/target/client-1.0-SNAPSHOT.jar:shared/target/shared-1.0-SNAPSHOT.jar" \
     co.edu.uptc.client.ClientMain 68.211.112.149 1100
```

## 📋 Requisitos Previos

### 1. Compilación del Proyecto
```bash
mvn clean package -DskipTests
```

Esto genera los siguientes JAR:
- `server/target/server-1.0-SNAPSHOT.jar`
- `client/target/client-1.0-SNAPSHOT.jar`
- `shared/target/shared-1.0-SNAPSHOT.jar`

### 2. Archivo de Seguridad (all.policy)
Debe estar en la raíz del proyecto. Contiene permisos RMI necesarios.

### 3. Puertos Abiertos en Azure
- **Puerto 1100 (TCP)**: Para RMI Registry
- Asegúrate de que el NSG (Network Security Group) permita tráfico entrante en el puerto 1100

## 🔧 Configuración de Firewall en Azure

### Regla de Entrada NSG:
```
Protocolo: TCP
Rango de puertos: 1100
Origen: IP del cliente local (o * para permitir desde cualquier lugar)
Destino: Cualquiera
Acción: Permitir
```

## 📊 Arquitectura RMI

### Flujo de Comunicación:
```
Cliente Local (192.168.x.x)
    ↓
Conecta a Servidor Azure (68.211.112.149:1100)
    ↓
Servidor exporta Callback RMI
    ↓
Callbacks ASINCRONICOS (no bloquean en firewall)
    ↓
Cliente recibe eventos del juego
```

### Características de Callbacks Asincronicos:
- ✅ Evita timeouts en redes WAN
- ✅ Ejecuta en thread pool independiente
- ✅ No bloquea en solicitudes de RMI
- ✅ Mejor tolerancia a latencia de red

## 🎮 Flujo del Juego

1. **Cliente 1** conecta a Servidor Azure
2. **Cliente 2** conecta a Servidor Azure
3. Ambos colocan barcos (máximo 4 barcos de 3 casillas)
4. Cuando ambos están listos, comienza el juego
5. Jugadores se alternan en turnos para atacar
6. El primero que hunda todos los barcos del oponente gana

## 📝 Logs y Debugging

### Servidor:
- Muestra conexiones de clientes
- Eventos del juego en tiempo real
- Timeouts y desconexiones

### Cliente:
- Intenta de conexión (hasta 5 reintentos)
- Eventos del juego recibidos
- Errores de RMI y callbacks

## ⚠️ Problemas Comunes

### Error: "Connection refused to host: 68.211.112.149"
- **Causa**: El servidor Azure no está corriendo o el puerto no está abierto
- **Solución**: 
  1. Verifica que el servidor está ejecutándose en Azure
  2. Abre el puerto 1100 en el NSG
  3. Verifica la IP pública correcta

### Error: "java.net.ConnectException: Connection timed out"
- **Causa**: Firewall bloqueando la conexión
- **Solución**:
  1. Verifica reglas del NSG
  2. Intenta deshabilitar firewall local temporalmente
  3. Asegúrate de usar IP pública correcta

### GUI del Cliente no aparece
- **Causa**: Servidor remoto sin display (headless)
- **Solución**: Debe ejecutarse en máquina con GUI (Windows Desktop, Linux con X11)

## 🔒 Seguridad

- **all.policy**: Permisos mínimos para RMI
- **LAN Only**: Servidor rechaza conexiones fuera de redes privadas (configurable)
- **RMI Security Manager**: Requiere política de seguridad

## 📈 Monitoreo

El servidor incluye:
- Detección de clientes desconectados (timeout 90s)
- Heartbeat automático cada 10s
- Logs estructurados con Java Logging

## 🐛 Testing

### Prueba Local:
```bash
.\run-server-azure.ps1 -AzureIP localhost -Port 1100
# En otra terminal:
.\run-client-azure.ps1 -AzureIP localhost -Port 1100
```

### Prueba Remota:
```bash
# En Azure:
.\run-server-azure.ps1 -AzureIP 68.211.112.149 -Port 1100

# En tu PC local:
.\run-client-azure.ps1 -AzureIP 68.211.112.149 -Port 1100
```

## 📞 Soporte

Para más información sobre el proyecto, consulta:
- `ANALISIS_TECNICO_RMI.md` - Análisis técnico de RMI
- `GUIA_AZURE_DEPLOYMENT.md` - Guía de deployment en Azure
- `PASOS_CONFIGURACION_AZURE.md` - Pasos de configuración

---

**Última actualización**: Noviembre 2025  
**Versión**: 1.0.0
