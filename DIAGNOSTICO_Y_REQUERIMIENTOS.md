# 📋 DIAGNÓSTICO Y REQUERIMIENTOS DEL PROYECTO BATTLESHIP

**Fecha:** 27 de Noviembre de 2025  
**Rama:** socketweb  
**Proyecto:** Batalla Naval Online (WebSocket)  

---

## 1. ARQUITECTURA TÉCNICA

### 1.1 Estructura del Proyecto

```
battleship-websocket/
├── shared/           → Modelos y mensajes compartidos
├── server/           → Servidor WebSocket
├── client/           → Cliente GUI (Swing)
└── cliente_final/    → Distribución para usuarios
```

### 1.2 Componentes

| Componente | Tipo | Tecnología | Función |
|-----------|------|-----------|---------|
| **Servidor** | Java JAR | Tyrus WebSocket 1.17 | Coordina partidas, gestiona lógica de juego |
| **Cliente** | Java JAR | Swing + Tyrus WebSocket | Interfaz gráfica, cliente WebSocket |
| **Comunicación** | Protocol | WebSocket (RFC 6455) | Bidireccional, tiempo real |
| **Serialización** | Format | JSON (Gson 2.10.1) | Mensajes entre cliente-servidor |

---

## 2. REQUERIMIENTOS DE AMBIENTE

### 2.1 En la Máquina de Desarrollo (LOCAL)

#### Software Requerido
- **Java Development Kit (JDK):** 11 o superior
  - Verificación: `java -version`
  - Mínimo recomendado: JDK 11.0.15
  
- **Apache Maven:** 3.8.1 o superior
  - Verificación: `mvn -v`
  - Instalación: https://maven.apache.org/download.cgi

#### Configuración de Variables de Entorno
```
JAVA_HOME=C:\Program Files\Java\jdk-11  (o tu ruta de JDK)
MAVEN_HOME=C:\path\to\maven
PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;...
```

#### Verificación del Ambiente
```powershell
java -version                    # Debe mostrar Java 11+
javac -version                   # Debe mostrar Java 11+
mvn -v                          # Debe mostrar Maven 3.8.1+
```

### 2.2 En Azure VM (SERVIDOR)

#### Software Requerido
- **Java Runtime Environment (JRE):** 11 o superior
  - Instalación en Ubuntu/Debian: `sudo apt-get install openjdk-11-jre-headless`
  - O completo: `sudo apt-get install openjdk-11-jdk-headless`

#### Configuración de Red
- **Port 8080:** Debe estar abierto en Azure NSG (Network Security Group)
- **Regla de entrada:** 
  - Protocolo: TCP
  - Puerto: 8080
  - Origen: Cualquiera (0.0.0.0/0) o IPs específicas
  - Destino: Cualquiera

#### Verificación en Azure
```bash
# Verificar Java está instalado
java -version

# Verificar puerto 8080 está escuchando
netstat -tlnp | grep 8080
# o
ss -tlnp | grep 8080

# Ver información de la VM
hostnamectl
```

---

## 3. CONFIGURACIÓN DE COMPILACIÓN

### 3.1 Estructura Maven

**Proyecto Padre:** `pom.xml`
```xml
<groupId>co.edu.uptc</groupId>
<artifactId>battleship-websocket</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>pom</packaging>

<modules>
  <module>shared</module>
  <module>server</module>
  <module>client</module>
</modules>
```

**Java Version:** 11  
**Source Encoding:** UTF-8  

### 3.2 Dependencias Principales

| Dependencia | Versión | Propósito |
|------------|---------|----------|
| javax.websocket-api | 1.1 | Especificación WebSocket |
| tyrus-server | 1.17 | Implementación servidor WebSocket |
| tyrus-client | 1.17 | Implementación cliente WebSocket |
| tyrus-container-grizzly-server | 1.17 | Contenedor servidor |
| tyrus-container-grizzly-client | 1.17 | Contenedor cliente |
| gson | 2.10.1 | Serialización JSON |

### 3.3 Artefactos Generados

**Server:**
```
server/target/battleship-server-jar-with-dependencies.jar
```
- Main Class: `co.edu.uptc.server.ServerMainWebSocket`
- Tamaño: ~10-12 MB
- Ejecutable: Sí (contiene todas las dependencias)

**Client:**
```
client/target/battleship-client-jar-with-dependencies.jar
```
- Main Class: `co.edu.uptc.client.ClientMain`
- Tamaño: ~12-14 MB
- Ejecutable: Sí (contiene todas las dependencias)

---

## 4. CONFIGURACIÓN DEL SERVIDOR

### 4.1 Punto de Entrada: ServerMainWebSocket.java

```java
public static void main(String[] args) {
    String host = "0.0.0.0";  // Escucha en todas las interfaces
    int port = 8080;           // Puerto fijo
    String path = "/";         // Ruta raíz
    
    // Si se proporciona argumento, se usa como host
    if (args.length > 0) host = args[0];
    
    Server server = new Server(host, port, path, null, GameWebSocketServer.class);
    server.start();
}
```

### 4.2 Configuración WebSocket

**Endpoint:** `/battleship`
```
Protocolo: WebSocket (RFC 6455)
URL Local: ws://localhost:8080/battleship
URL Remoto: ws://68.211.112.149:8080/battleship
```

### 4.3 Ciclo de Vida del Servidor

```
1. OnOpen(Session)
   └─ Cliente conecta
   └─ Se registra en colecciones thread-safe

2. OnMessage(String, Session)
   └─ Recibe mensaje JSON
   └─ Decodifica a ClientMessage
   └─ Ejecuta acción (join, placeShip, attack, etc.)
   └─ GameServiceWebSocket procesa lógica
   └─ Envía respuesta ServerMessage

3. OnClose(Session, CloseReason)
   └─ Cliente desconecta
   └─ Se elimina de colecciones
   └─ Se notifica al oponente

4. OnError(Session, Throwable)
   └─ Error durante comunicación
   └─ Se registra en logs
```

### 4.4 Acciones Soportadas

| Acción | Parámetros | Respuesta |
|--------|-----------|----------|
| `join` | playerName | joined (playerId, sessionId) |
| `placeShip` | start, end (Position) | shipPlaced |
| `attack` | target (Position) | attackResult |
| `ready` | - | readyChanged |
| `surrender` | - | gameEnded |
| `requestNewGame` | - | newGameRequest |
| `respondNewGame` | accepts | respondedNewGame |
| `ping` | - | pong |

---

## 5. CONFIGURACIÓN DEL CLIENTE

### 5.1 Punto de Entrada: ClientMain.java

```java
public static void main(String[] args) {
    // Prioridad de configuración:
    // 1. Arguments (java -jar ... <host> <port>)
    // 2. System Properties (java -D...)
    // 3. Environment Variables
    // 4. Valores por defecto (localhost:8080)
    
    String host = args.length > 0 ? args[0] : "localhost";
    int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
    
    String wsUrl = "ws://" + host + ":" + port + "/battleship";
    GameWindow gameWindow = new GameWindow(wsUrl);
}
```

### 5.2 Métodos de Conexión

**Método 1: Argumentos de Línea de Comandos (RECOMENDADO)**
```powershell
java -jar battleship-client.jar 68.211.112.149 8080
```

**Método 2: Variables de Entorno**
```powershell
$env:BATTLESHIP_HOST="68.211.112.149"
$env:BATTLESHIP_PORT="8080"
java -jar battleship-client.jar
```

**Método 3: System Properties**
```powershell
java -Dbattleship.host=68.211.112.149 -Dbattleship.port=8080 -jar battleship-client.jar
```

**Método 4: Local (Desarrollo)**
```powershell
java -jar battleship-client.jar localhost 8080
```

### 5.3 Ciclo de Vida del Cliente

```
1. ClientMain.main()
   └─ Parsea argumentos/env/properties
   └─ Construye URL WebSocket
   └─ Lanza GUI

2. GameWindow()
   └─ Crea GameController
   └─ Solicita nombre del jugador
   └─ Llama a controller.connectPlayer()

3. GameController.connectPlayer()
   └─ Crea GameWebSocketClient
   └─ Conecta a WebSocket
   └─ Envía mensaje "join"

4. GameWebSocketClient.onOpen()
   └─ Conexión establecida
   └─ Listo para recibir mensajes

5. GameWindow.handleGameEvent()
   └─ Actualiza interfaz gráfica
   └─ Muestra mensajes de estado
```

### 5.4 Timeouts y Configuración

```java
WebSocketContainer container = ContainerProvider.getWebSocketContainer();
container.setDefaultMaxSessionIdleTimeout(120000L);      // 2 minutos
container.setDefaultMaxTextMessageBufferSize(8192);      // 8 KB
```

---

## 6. FLUJO DE COMUNICACIÓN

### 6.1 Ejemplo: Cliente intenta jugar

```
CLIENTE                              SERVIDOR
   |                                    |
   |---> Mensaje JSON (join) --------> |
   |                                    | onMessage()
   |                                    | handleJoin()
   |                                    | gameService.joinGame()
   |                                    |
   |<---- Mensaje JSON (joined) <----- |
   |      (playerId, sessionId)        |
   |                                    |
   | (Coloca barcos)                   |
   |---> Mensaje JSON (placeShip) ---> |
   |                                    | handlePlaceShip()
   |                                    | gameService.placeShip()
   |                                    |
   |<---- Mensaje JSON (event) <------ |
   |      (notificación)                |
   |                                    |
   | (Segundo jugador se conecta)      |
   |                                    | onMessage() (otro cliente)
   |                                    | handleJoin()
   |                                    |
   |<---- Mensaje JSON (event) <------ | (ambos clientes)
   |      (playerJoined)                |
   |                                    |
```

### 6.2 Estructura de Mensajes

**ClientMessage (JSON enviado por cliente):**
```json
{
  "action": "join",
  "playerName": "Juan",
  "playerId": "player_1",
  "position": {"x": 0, "y": 0},
  "target": {"x": 5, "y": 5},
  "accepts": true
}
```

**ServerMessage (JSON enviado por servidor):**
```json
{
  "event": "joined",
  "playerId": "player_1",
  "sessionId": "session_1",
  "message": "Te has unido al juego",
  "currentPlayerName": "Juan",
  "isMyTurn": true,
  "result": "HIT"
}
```

---

## 7. PROCESO DE COMPILACIÓN

### 7.1 Compilar Todo el Proyecto

```powershell
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682

# Compilar shared
mvn -f shared/pom.xml clean install

# Compilar server
mvn -f server/pom.xml clean package

# Compilar client
mvn -f client/pom.xml clean package

# O compilar todo de una vez
mvn clean install
```

### 7.2 Salidas Esperadas

```
✅ Construcción exitosa
---
[INFO] Building battleship-websocket 1.0-SNAPSHOT
[INFO] --- maven-clean-plugin:3.0.0:clean @ battleship-websocket ---
[INFO] --- maven-compile-plugin:3.11.0:compile @ server ---
[INFO] --- maven-assembly-plugin:3.6.0:single @ server ---
[INFO] BUILD SUCCESS
```

### 7.3 Troubleshooting de Compilación

| Problema | Causa | Solución |
|----------|-------|----------|
| Error: "Compile failure" | Versión Java incorrecta | Verificar `java -version` (debe ser 11+) |
| Error: "Cannot find symbol" | Dependencias no descargadas | Ejecutar `mvn clean install` |
| Archivo JAR no existe | Build incompleto | Revisar logs de Maven |
| Tamaño de JAR incorrecto | Dependencias no incluidas | Verificar `jar-with-dependencies` |

---

## 8. CONFIGURACIÓN DE EJECUCIÓN

### 8.1 Servidor en Azure VM

#### Paso 1: Transferir JAR a Azure
```powershell
# Desde local
scp server\target\battleship-server-jar-with-dependencies.jar azureuser@68.211.112.149:/home/azureuser/battleship-server.jar
```

#### Paso 2: Ejecutar Servidor
```bash
# En Azure VM
cd /home/azureuser
nohup java -jar battleship-server.jar > server.log 2>&1 &
```

#### Paso 3: Verificar Servidor
```bash
# En Azure VM
ps aux | grep battleship-server
# o ver logs
tail -f /home/azureuser/server.log
```

**Salida Esperada:**
```
🚀 Iniciando Servidor WebSocket de Batalla Naval...

╔═══════════════════════════════════════════════════════╗
║      SERVIDOR BATALLA NAVAL WEBSOCKET                 ║
╠═══════════════════════════════════════════════════════╣
║ 🌐 Host: 0.0.0.0                                      ║
║ 🔌 Puerto: 8080                                       ║
║ 📡 Endpoint: ws://host:8080/battleship                ║
║ 🎮 Capacidad: 2 jugadores simultáneos                ║
║ 📊 Estado: Esperando conexiones...                    ║
╚═══════════════════════════════════════════════════════╝

⚠️  Para detener el servidor presiona Ctrl+C
```

### 8.2 Cliente en Computadora Local

#### Opción 1: Línea de Comandos
```powershell
java -jar battleship-client-jar-with-dependencies.jar 68.211.112.149 8080
```

#### Opción 2: Archivo Batch
```powershell
# JUGAR.bat (Windows)
@echo off
java -jar battleship-client.jar 68.211.112.149 8080
pause
```

#### Opción 3: Doble Clic (Sin Terminal)
- Crear acceso directo a `JUGAR.bat`
- O crear script VBS que ejecute silenciosamente

---

## 9. DIAGNÓSTICO DE CONECTIVIDAD

### 9.1 Verificar Servidor Está Corriendo

**Desde Azure VM:**
```bash
netstat -tlnp | grep 8080
# o
ss -tlnp | grep 8080
```

**Salida esperada:**
```
tcp  0  0  0.0.0.0:8080  0.0.0.0:*  LISTEN  12345/java
```

### 9.2 Verificar NSG en Azure

```bash
# SSH a Azure
ssh azureuser@68.211.112.149

# Ver reglas de firewall
sudo iptables -L -n

# O verificar en Azure Portal:
# Resource Groups > Tu grupo > VM > Networking > Inbound rules
# Debe haber regla: Port 8080, TCP, Allow, Source Any
```

### 9.3 Probar Conectividad desde Local

```powershell
# Desde Windows
Test-NetConnection -ComputerName 68.211.112.149 -Port 8080

# Salida esperada:
# TcpTestSucceeded : True
```

### 9.4 Prueba de WebSocket Manual

```powershell
# Con PowerShell 6+
$ws = New-WebSocket -Uri "ws://68.211.112.149:8080/battleship"
# Si esto falla, la conectividad WebSocket tiene problemas
```

---

## 10. MATRIZ DE VALIDACIÓN

| Aspecto | Verificación | Estado | Acción |
|--------|-------------|--------|--------|
| **Ambiente Local** | JDK 11+, Maven 3.8+ | ⬜ | `java -version && mvn -v` |
| **Compilación** | Maven build exitoso | ⬜ | `mvn clean install` |
| **Servidor JAR** | Archivo existe | ⬜ | Ver `server/target/` |
| **Cliente JAR** | Archivo existe | ⬜ | Ver `client/target/` |
| **Azure VM** | JRE 11+ instalado | ⬜ | SSH y verificar |
| **Azure NSG** | Puerto 8080 abierto | ⬜ | Azure Portal |
| **Servidor Ejecutando** | Escuchando en 8080 | ⬜ | `netstat -tlnp` en Azure |
| **Conectividad Red** | Puerto alcanzable | ⬜ | `Test-NetConnection` |
| **Cliente Conecta** | Mensaje "Conectado" | ⬜ | Ejecutar cliente |
| **Juego Funciona** | 2 jugadores pueden jugar | ⬜ | Prueba manual |

---

## 11. CHECKLIST DE SETUP INICIAL

### Antes de Desplegar a Producción

- [ ] Compilar proyecto: `mvn clean install`
- [ ] Verificar tamaño de JARs (debe ser >10 MB)
- [ ] Probar servidor localmente: `java -jar battleship-server.jar`
- [ ] Probar cliente localmente: `java -jar battleship-client.jar localhost 8080`
- [ ] Verificar Azure VM tiene JRE 11+
- [ ] Verificar NSG permite puerto 8080
- [ ] Transferir servidor JAR a Azure
- [ ] Ejecutar servidor en Azure
- [ ] Verificar servidor escuchando: `netstat -tlnp`
- [ ] Conectar cliente desde local a Azure
- [ ] Jugar partida de prueba con 2 jugadores
- [ ] Verificar logs de servidor: `tail -f server.log`

---

## 12. CONTACTO Y REFERENCIAS

**Punto de Entrada del Servidor:**
- Archivo: `server/src/main/java/co/edu/uptc/server/ServerMainWebSocket.java`

**Punto de Entrada del Cliente:**
- Archivo: `client/src/main/java/co/edu/uptc/client/ClientMain.java`

**Lógica de Juego:**
- Servidor: `server/src/main/java/co/edu/uptc/server/GameServiceWebSocket.java`
- Sesión: `server/src/main/java/co/edu/uptc/server/GameSession.java`

**Comunicación WebSocket:**
- Servidor: `server/src/main/java/co/edu/uptc/server/GameWebSocketServer.java`
- Cliente: `client/src/main/java/co/edu/uptc/client/GameWebSocketClient.java`

---

**Documentación Actualizada:** 27/11/2025  
**Rama:** socketweb  
**Versión del Proyecto:** 1.0-SNAPSHOT
