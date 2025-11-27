#  Batalla Naval Online - Proyecto Educativo

**Versión:** 1.0-SNAPSHOT  
**Rama:** socketweb  
**Tecnología:** Java 11 + WebSocket (Tyrus) + Swing GUI  
**Estado:** ✅ Completamente Funcional  

---

##  Descripción del Proyecto

**Batalla Naval Online** es una aplicación educativa de juego de estrategia que demuestra:

- ✅ Arquitectura **cliente-servidor distribuida**
- ✅ Comunicación **WebSocket en tiempo real**
- ✅ Serialización **JSON** para mensajes
- ✅ Interfaz gráfica con **Swing**
- ✅ Lógica de juego **multijugador sincronizada**
- ✅ Despliegue en **Azure VM**

---

## 🎯 Objetivos Educativos

Este proyecto enseña a estudiantes:

1. **Arquitectura Distribuida:** Separación de responsabilidades (servidor/cliente)
2. **Comunicación en Tiempo Real:** WebSocket vs HTTP tradicional
3. **Serialización de Datos:** JSON para intercambio de información
4. **Programación Concurrente:** Thread-safe collections, sync
5. **Interfaz Gráfica:** Desarrollo de GUIs con Swing
6. **Build Automation:** Maven para compilación
7. **Despliegue en Cloud:** Azure VM con Linux

---

## 🚀 Inicio Rápido

### Para Desarrolladores

**1. Requisitos:**
- Java 11 o superior
- Maven 3.8+

**2. Compilar:**
```bash
mvn clean install
```

**3. Ejecutar Servidor (Local):**
```bash
java -jar server/target/battleship-server-jar-with-dependencies.jar
```

**4. Ejecutar Cliente (Otra terminal):**
```bash
java -jar client/target/battleship-client-jar-with-dependencies.jar localhost 8080
```

### Para Jugadores

**Con Azure (Servidor ya corriendo):**
```bash
java -jar battleship-client-jar-with-dependencies.jar 68.211.112.149 8080
```

**O hacer doble clic en `JUGAR.bat` en carpeta `cliente_final/`**

---

##  Estructura del Proyecto

```
battleship-websocket/
├── README.md                          ← Este archivo
├── DIAGNOSTICO_Y_REQUERIMIENTOS.md    ← Análisis técnico detallado
├── GUIA_PRACTICA_SETUP.md            ← Guía paso a paso
│
├── shared/                            ← Modelos compartidos
│   ├── pom.xml
│   └── src/main/java/co/edu/uptc/shared/
│       ├── messages/
│       │   ├── ClientMessage.java
│       │   └── ServerMessage.java
│       └── model/
│           ├── Board.java
│           ├── GameStatus.java
│           ├── GameStats.java
│           ├── PlayerStats.java
│           ├── Position.java
│           └── Ship.java
│
├── server/                            ← Servidor WebSocket
│   ├── pom.xml
│   └── src/main/java/co/edu/uptc/server/
│       ├── ServerMainWebSocket.java        (Punto de entrada)
│       ├── GameWebSocketServer.java        (Endpoint WebSocket)
│       ├── GameServiceWebSocket.java       (Lógica de juego)
│       ├── GameSession.java                (Sesión de partida)
│       └── Player.java                     (Jugador)
│
├── client/                            ← Cliente GUI
│   ├── pom.xml
│   └── src/main/java/co/edu/uptc/client/
│       ├── ClientMain.java                 (Punto de entrada)
│       ├── GameWebSocketClient.java        (Cliente WebSocket)
│       ├── GameController.java             (Controlador lógica)
│       ├── GameWindow.java                 (Ventana principal)
│       ├── BoardPanel.java                 (Panel del tablero)
│       └── GameStatsPanel.java             (Panel de estadísticas)
│
└── cliente_final/                     ← Distribución para usuarios
    ├── INSTRUCCIONES.md
    ├── JUGAR.bat
    └── battleship-client.jar          (Se actualiza con compilaciones)
```

---

## 🔧 Tecnología

| Componente | Versión | Propósito |
|-----------|---------|----------|
| Java | 11 LTS | Lenguaje principal |
| Maven | 3.8+ | Build automation |
| WebSocket API | 1.1 | Especificación WebSocket |
| Tyrus | 1.17 | Implementación WebSocket |
| Gson | 2.10.1 | Serialización JSON |
| Swing | Built-in | GUI |
| Grizzly | 1.17 (Tyrus) | Contenedor WebSocket |

---

## 📚 Documentación

### Para Entender la Arquitectura
**Lee primero:** [`DIAGNOSTICO_Y_REQUERIMIENTOS.md`](DIAGNOSTICO_Y_REQUERIMIENTOS.md)

Contiene:
- Arquitectura del sistema
- Requerimientos de ambiente
- Configuración de compilación
- Flujo de comunicación
- Matriz de validación

### Para Setup y Ejecución
**Lee siguiente:** [`GUIA_PRACTICA_SETUP.md`](GUIA_PRACTICA_SETUP.md)

Contiene:
- Setup paso a paso
- Compilación
- Pruebas locales
- Deploy a Azure
- Troubleshooting

### Para Jugar
**Lee:** [`cliente_final/INSTRUCCIONES.md`](cliente_final/INSTRUCCIONES.md)

---

##  Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                     ARQUITECTURA GENERAL                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────┐           ┌─────────────────────────┐
│   CLIENTE 1 (GUI)       │           │   CLIENTE 2 (GUI)       │
│  ┌─────────────────┐    │           │  ┌─────────────────┐    │
│  │  GameWindow     │    │           │  │  GameWindow     │    │
│  │  BoardPanel     │    │           │  │  BoardPanel     │    │
│  └────────┬────────┘    │           │  └────────┬────────┘    │
│           │             │           │           │             │
│  ┌────────▼────────┐    │           │  ┌────────▼────────┐    │
│  │GameController   │    │           │  │GameController   │    │
│  └────────┬────────┘    │           │  └────────┬────────┘    │
│           │             │           │           │             │
│  ┌────────▼────────────────────────────────────────┐           │
│  │  GameWebSocketClient (Tyrus)                    │           │
│  │  - Connect/Disconnect                          │           │
│  │  - Send ClientMessage (JSON)                   │           │
│  │  - Receive ServerMessage (JSON)                │           │
│  └──────┬─────────────────────────────────────────┘           │
│         │                                    │                 │
└─────────┼────────────────────────────────────┼─────────────────┘
          │       WebSocket (RFC 6455)        │
          │  ws://68.211.112.149:8080/battleship
          │                                    │
┌─────────▼────────────────────────────────────▼─────────────────┐
│                   SERVIDOR (Azure VM)                           │
│                                                                 │
│  ┌──────────────────────────────────────────────────┐           │
│  │  ServerMainWebSocket                             │           │
│  │  - Inicializa servidor en puerto 8080           │           │
│  │  - Inicia Tyrus Server                          │           │
│  └──────────────────────────────────────────────────┘           │
│                                                                 │
│  ┌──────────────────────────────────────────────────┐           │
│  │  GameWebSocketServer (@ServerEndpoint)          │           │
│  │  - @OnOpen → Registra cliente                   │           │
│  │  - @OnMessage → Procesa mensajes                │           │
│  │  - @OnClose → Desconecta cliente                │           │
│  │  - @OnError → Maneja errores                    │           │
│  └──────────────────────────────────────────────────┘           │
│                      │                                         │
│  ┌───────────────────▼──────────────────────────────┐           │
│  │  GameServiceWebSocket (Lógica de Juego)         │           │
│  │  - joinGame()                                   │           │
│  │  - placeShip()                                  │           │
│  │  - attack()                                     │           │
│  │  - markPlayerReady()                            │           │
│  │  - surrenderGame()                              │           │
│  └──────────────────────────────────────────────────┘           │
│                      │                                         │
│  ┌───────────────────▼──────────────────────────────┐           │
│  │  GameSession (Sesión de Partida)                │           │
│  │  - Dos jugadores                                │           │
│  │  - Tableros (mis barcos, enemigos)              │           │
│  │  - Control de turnos                            │           │
│  │  - Estado del juego                             │           │
│  └──────────────────────────────────────────────────┘           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flujo de Juego

```
1. CONEXIÓN
   ├─ Cliente solicita conectarse con nombre
   ├─ Servidor crea Player y GameSession
   └─ Cliente recibe playerId y sessionId

2. ESPERA DEL OPONENTE
   └─ El primer cliente espera al segundo

3. FASE: COLOCACIÓN DE BARCOS
   ├─ Ambos clientes reciben "Fase de colocación iniciada"
   ├─ Cada cliente coloca 5 barcos en su tablero
   ├─ Envían al servidor: startPosition, endPosition
   └─ Servidor valida y confirma

4. FASE: LISTO PARA JUGAR
   ├─ Cliente hace clic en "¡LISTO!"
   ├─ Servidor marca como ready
   ├─ Cuando ambos ready → Juego comienza
   └─ Se asigna jugador que comenzará

5. FASE: JUGANDO
   ├─ Jugador A ataca a Jugador B
   ├─ Servidor procesa: HIT, MISS, SUNK, GAME_OVER
   ├─ Notifica resultado a ambos
   ├─ Si es MISS → Cambia de turno
   ├─ Si es HIT/SUNK → Continúa turno
   └─ Repite hasta que alguien gane

6. FINALIZACIÓN
   ├─ Jugador ganador recibe: "¡HAS GANADO!"
   ├─ Jugador perdedor recibe: "¡HAS PERDIDO!"
   └─ Opción para nueva partida
```

---

##  Testing

### Prueba Local 

```powershell
# Terminal 1: Servidor
java -jar server/target/battleship-server-jar-with-dependencies.jar

# Terminal 2: Cliente 1
java -jar client/target/battleship-client-jar-with-dependencies.jar localhost 8080

# Terminal 3: Cliente 2
java -jar client/target/battleship-client-jar-with-dependencies.jar localhost 8080
```

### Verificación de Conectividad

```powershell
# Verificar servidor está escuchando
netstat -ano | findstr :8080

# Verificar conexión a Azure
Test-NetConnection -ComputerName 68.211.112.149 -Port 8080

# Si sale "TcpTestSucceeded: True" → OK
```

---

## 📈 Compilación y Build

### Build Local

```bash
# Compilar todo
mvn clean install

# Solo compilar (sin tests)
mvn clean install -DskipTests

# Compilar módulo específico
mvn -f server/pom.xml clean package
```

### Artefactos Generados

```
server/target/
  └─ battleship-server-jar-with-dependencies.jar    (~10-12 MB)

client/target/
  └─ battleship-client-jar-with-dependencies.jar    (~12-14 MB)

shared/target/
  └─ shared-1.0-SNAPSHOT.jar
```

---

##  Deploy a Azure

### Paso 1: Preparar Azure VM

```bash
# En Azure VM (Ubuntu)
sudo apt update
sudo apt install -y openjdk-11-jre-headless
java -version
```

### Paso 2: Abrir Puerto 8080

- Ir a Azure Portal
- VM → Networking → Inbound rules
- Add rule: Port 8080, TCP, Allow

### Paso 3: Transferir JAR

```powershell
# Desde local a Azure
scp server/target/battleship-server-jar-with-dependencies.jar azureuser@68.211.112.149:/home/azureuser/battleship-server.jar
```

### Paso 4: Ejecutar Servidor

```bash
# En Azure
ssh azureuser@68.211.112.149
nohup java -jar battleship-server.jar > server.log 2>&1 &
tail -f server.log  # Ver logs
```

### Paso 5: Conectar Clientes

```powershell
# Desde cualquier máquina local
java -jar battleship-client-jar-with-dependencies.jar 68.211.112.149 8080
```

---

## 🔍 Monitoreo y Logs

### Logs del Servidor (Azure)

```bash
# Ver últimas líneas
tail -20 /home/azureuser/server.log

# Ver en tiempo real
tail -f /home/azureuser/server.log

# Buscar errores
grep -i error /home/azureuser/server.log

# Ver información de conexiones
grep -i connected /home/azureuser/server.log
```

### Verificar Proceso

```bash
# Ver si servidor está corriendo
ps aux | grep battleship-server

# Ver puertos escuchando
netstat -tlnp | grep 8080

# Ver conexiones activas
ss -tan | grep 8080
```

---

##  Reglas de Batalla Naval

### Preparación
- 2 jugadores
- 5 barcos de diferentes tamaños
- Tablero 10x10

### Juego
1. Por turnos, intenta hundir barcos del enemigo
2. Si aciertas (HIT) → Tu turno continúa
3. Si fallas (MISS) → Turno del enemigo
4. Cuando hundes todos los barcos (SUNK) → ¡Ganaste!

### Barra de Estado
- **AGUA** → No acertaste
- **IMPACTO** → Golpeaste un barco
- **HUNDIDO** → Barco completamente destruido
- **GANADOR** → Hundiste todos los barcos

---


---

##  Información de Contacto

**Autor del Proyecto:** César Caro  
**Estudiante ID:** 202221682  
**Institución:** UPTC (Universidad Pedagógica y Tecnológica de Colombia)  

**Repositorio:** https://github.com/Jawbreaker1989/battleship  
**Rama Actual:** `socketweb`  
**Versión:** 1.0-SNAPSHOT  

---

## Licencia

Este es un proyecto educativo. Úsalo libremente para aprender.


