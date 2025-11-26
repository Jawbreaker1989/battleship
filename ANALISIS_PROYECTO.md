# ANÁLISIS EXHAUSTIVO DEL PROYECTO BATTLESHIP RMI
**Proyecto:** Batalla Naval Distribuida con Java RMI  
**Autor:** César Caro (202221682)  
**Fecha de Análisis:** 25 de Noviembre de 2025

---

## 📋 TABLA DE CONTENIDOS

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Entorno de Desarrollo](#entorno-de-desarrollo)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Análisis de Módulos](#análisis-de-módulos)
6. [Tecnologías y Patrones](#tecnologías-y-patrones)
7. [Flujo de Comunicación](#flujo-de-comunicación)
8. [Características Implementadas](#características-implementadas)
9. [Seguridad y Configuración](#seguridad-y-configuración)
10. [Scripts de Compilación y Ejecución](#scripts-de-compilación-y-ejecución)
11. [Análisis de Calidad del Código](#análisis-de-calidad-del-código)
12. [Conclusiones y Recomendaciones](#conclusiones-y-recomendaciones)

---

## 1. RESUMEN EJECUTIVO

### 1.1 Descripción General
Este proyecto implementa un **juego de Batalla Naval multijugador distribuido** utilizando **Java RMI (Remote Method Invocation)** como tecnología de comunicación. El sistema permite que dos jugadores en diferentes computadoras dentro de una red LAN jueguen entre sí en tiempo real.

### 1.2 Características Principales
- ✅ **Arquitectura Cliente-Servidor Distribuida** con RMI
- ✅ **Comunicación Bidireccional** mediante callbacks RMI
- ✅ **Interfaz Gráfica** con Java Swing
- ✅ **Gestión de Sesiones** para múltiples partidas
- ✅ **Sistema de Estadísticas** (victorias/derrotas)
- ✅ **Detección Automática de IP LAN**
- ✅ **Monitoreo de Conexiones** con heartbeat
- ✅ **Sistema de Revancha** entre jugadores
- ✅ **Restricción de Conexiones LAN** para seguridad

### 1.3 Tecnologías Clave
- **Lenguaje:** Java 11
- **Build Tool:** Maven 3.x
- **Framework de Comunicación:** Java RMI
- **GUI:** Java Swing
- **Arquitectura:** Cliente-Servidor con callbacks bidireccionales
- **Serialización:** Java Serialization para objetos distribuidos

---

## 2. ARQUITECTURA DEL SISTEMA

### 2.1 Patrón Arquitectónico
El proyecto sigue una **arquitectura de tres capas** con separación de responsabilidades:

```
┌─────────────────────────────────────────────────────────┐
│                    CAPA CLIENTE                         │
│  - Interfaz Gráfica (Swing)                            │
│  - Controlador de Juego                                │
│  - Implementación de Callbacks                         │
└─────────────────────────────────────────────────────────┘
                          ↕ RMI
┌─────────────────────────────────────────────────────────┐
│                   CAPA SERVIDOR                         │
│  - Servicio RMI (GameServiceImpl)                      │
│  - Gestión de Sesiones (GameSession)                   │
│  - Lógica de Negocio                                   │
└─────────────────────────────────────────────────────────┘
                          ↕
┌─────────────────────────────────────────────────────────┐
│                   CAPA COMPARTIDA                       │
│  - Interfaces RMI                                       │
│  - Modelos de Datos (Serializables)                    │
│  - Enumeraciones y DTOs                                │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Componentes Principales

#### **Módulo SHARED (Compartido)**
Contiene las definiciones compartidas entre cliente y servidor:
- **Interfaces RMI:**
  - `GameService`: Contrato del servicio principal
  - `GameCallback`: Contrato para notificaciones bidireccionales
  
- **Modelos de Datos:**
  - `Board`: Tablero de juego 10x10
  - `Ship`: Representación de barcos
  - `Position`: Coordenadas en el tablero
  - `GameStatus`: Estado del juego
  - `GameStats`: Estadísticas de partida
  - `PlayerStats`: Estadísticas del jugador

#### **Módulo SERVER (Servidor)**
Implementa la lógica del servidor distribuido:
- **ServerMain**: Punto de entrada, configuración RMI Registry
- **GameServiceImpl**: Implementación del servicio RMI
- **GameSession**: Gestión de sesiones de juego
- **Player**: Representación de jugadores conectados

#### **Módulo CLIENT (Cliente)**
Implementa la interfaz de usuario y cliente RMI:
- **ClientMain**: Punto de entrada del cliente
- **GameWindow**: Ventana principal del juego
- **GameController**: Controlador de lógica del cliente
- **BoardPanel**: Panel visual del tablero
- **GameCallbackImpl**: Implementación de callbacks
- **NetworkDiscovery**: Descubrimiento de servidores (opcional)

### 2.3 Diagrama de Flujo de Comunicación

```
Cliente 1                    Servidor                    Cliente 2
   │                            │                            │
   │──joinGame()───────────────>│                            │
   │<────playerId───────────────│                            │
   │                            │<───joinGame()──────────────│
   │                            │────playerId────────────────>│
   │<──onPlayerJoined()─────────│                            │
   │                            │────onPlayerJoined()────────>│
   │                            │                            │
   │──placeShip()──────────────>│                            │
   │                            │                            │
   │                            │<───placeShip()─────────────│
   │                            │                            │
   │──setPlayerReady()─────────>│                            │
   │                            │<───setPlayerReady()────────│
   │<──onTurnChanged()──────────│────onTurnChanged()────────>│
   │                            │                            │
   │──attack(pos)──────────────>│                            │
   │                            │────onAttackEvent()─────────>│
   │<──onAttackEvent()──────────│                            │
   │<──onTurnChanged()──────────│────onTurnChanged()────────>│
```

---

## 3. ENTORNO DE DESARROLLO

### 3.1 Requisitos del Sistema
- **Java Development Kit (JDK):** 11 o superior
- **Apache Maven:** 3.6.0 o superior
- **Sistema Operativo:** Windows (scripts .bat), Linux/Mac (requiere adaptación)
- **Red:** Conexión LAN para juego multijugador

### 3.2 Configuración de Maven

#### **POM Principal (pom.xml)**
```xml
<groupId>co.edu.uptc</groupId>
<artifactId>battleship-rmi</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>pom</packaging>

<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<modules>
    <module>shared</module>
    <module>server</module>
    <module>client</module>
</modules>
```

#### **Estructura Multi-Módulo**
El proyecto utiliza Maven multi-módulo para:
- ✅ Separación clara de responsabilidades
- ✅ Gestión de dependencias centralizada
- ✅ Compilación coordinada
- ✅ Reutilización del módulo shared

### 3.3 Dependencias
El proyecto es **minimalista** y solo utiliza:
- **Java SE 11** (bibliotecas estándar)
- **Java RMI** (incluido en JDK)
- **Java Swing** (incluido en JDK)
- **Maven Assembly Plugin** (para empaquetado)

**No hay dependencias externas**, lo que facilita el despliegue.

### 3.4 Configuración de Seguridad RMI

#### **Archivo all.policy**
```java
grant {
    permission java.security.AllPermission;
};
```

⚠️ **Nota de Seguridad:** Este archivo otorga permisos completos. En producción, se deberían especificar permisos más restrictivos.

### 3.5 Propiedades del Sistema
El servidor configura:
```java
System.setProperty("java.rmi.server.hostname", host);
System.setProperty("java.security.policy", "all.policy");
System.setProperty("sun.rmi.transport.tcp.responseTimeout", "10000");
System.setProperty("sun.rmi.transport.tcp.readTimeout", "10000");
```

---

## 4. ESTRUCTURA DEL PROYECTO

### 4.1 Árbol de Directorios

```
batlleship_CesarCaro_202221682/
├── .git/                           # Control de versiones
├── .github/                        # Configuración GitHub
│   └── java-upgrade/
├── client/                         # Módulo Cliente
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── co/edu/uptc/client/
│   │               ├── BoardPanel.java
│   │               ├── ClientMain.java
│   │               ├── GameCallbackImpl.java
│   │               ├── GameController.java
│   │               ├── GameStatsPanel.java
│   │               ├── GameWindow.java
│   │               └── NetworkDiscovery.java
│   ├── target/                     # Compilados
│   └── pom.xml
├── server/                         # Módulo Servidor
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── co/edu/uptc/server/
│   │               ├── GameServiceImpl.java
│   │               ├── GameSession.java
│   │               ├── Player.java
│   │               └── ServerMain.java
│   ├── target/
│   └── pom.xml
├── shared/                         # Módulo Compartido
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── co/edu/uptc/shared/
│   │               ├── interfaces/
│   │               │   ├── GameCallback.java
│   │               │   └── GameService.java
│   │               └── model/
│   │                   ├── Board.java
│   │                   ├── GameStats.java
│   │                   ├── GameStatus.java
│   │                   ├── PlayerStats.java
│   │                   ├── Position.java
│   │                   └── Ship.java
│   ├── target/
│   └── pom.xml
├── paquete_cliente_para segunda_pc/  # Distribución cliente
├── 4_COMPILAR.bat                  # Script compilación
├── servidor-simple.bat             # Script servidor
├── run-cliente-local.bat           # Script cliente local
├── run-client-lan-simple.bat       # Script cliente LAN
├── build-dist-client.bat           # Script empaquetado
├── all.policy                      # Política seguridad
├── pom.xml                         # POM principal
└── .gitignore                      # Exclusiones Git
```

### 4.2 Convenciones de Nomenclatura
- **Paquetes:** `co.edu.uptc.{modulo}.{categoria}`
- **Clases:** PascalCase (ej: `GameServiceImpl`)
- **Métodos:** camelCase (ej: `joinGame()`)
- **Constantes:** UPPER_SNAKE_CASE (ej: `DEFAULT_PORT`)

---

## 5. ANÁLISIS DE MÓDULOS

### 5.1 MÓDULO SHARED

#### **5.1.1 Interface GameService**
**Ubicación:** `shared/src/main/java/co/edu/uptc/shared/interfaces/GameService.java`

**Responsabilidad:** Define el contrato del servicio RMI principal.

**Métodos Principales:**
```java
// Gestión de Jugadores
String joinGame(String playerName, GameCallback callback) throws RemoteException;
void disconnectPlayer(String playerId) throws RemoteException;
boolean ping(String playerId) throws RemoteException;

// Configuración de Juego
boolean placeShip(String playerId, Position start, Position end) throws RemoteException;
boolean setPlayerReady(String playerId) throws RemoteException;

// Jugabilidad
String attack(String playerId, Position target) throws RemoteException;
GameStatus getGameStatus(String playerId) throws RemoteException;

// Gestión de Partidas
boolean surrenderGame(String playerId) throws RemoteException;
boolean requestNewGame(String playerId) throws RemoteException;
boolean respondToNewGameRequest(String playerId, boolean accepts) throws RemoteException;

// Estadísticas
String getPlayerStats(String playerId) throws RemoteException;
GameStats getGameStats(String playerId) throws RemoteException;
GameStats getOpponentStats(String playerId) throws RemoteException;
```

**Características RMI:**
- ✅ Extiende `Remote`
- ✅ Todos los métodos lanzan `RemoteException`
- ✅ Parámetros y retornos serializables

#### **5.1.2 Interface GameCallback**
**Ubicación:** `shared/src/main/java/co/edu/uptc/shared/interfaces/GameCallback.java`

**Responsabilidad:** Define callbacks para notificaciones servidor → cliente.

**Métodos:**
```java
void onGameEvent(String message) throws RemoteException;
void onPlayerJoined(String playerName) throws RemoteException;
void onTurnChanged(boolean isMyTurn, String currentPlayerName) throws RemoteException;
void onGameEnded(String winner) throws RemoteException;
void onOpponentDisconnected() throws RemoteException;
void onAttackEvent(String attackerName, int targetX, int targetY, 
                   String result, boolean yourBoard) throws RemoteException;
void onNewGameRequest(String requesterName) throws RemoteException;
```

**Patrón de Diseño:** Observer distribuido mediante RMI callbacks.

#### **5.1.3 Clase Board**
**Ubicación:** `shared/src/main/java/co/edu/uptc/shared/model/Board.java`

**Responsabilidad:** Representa el tablero de juego 10x10.

**Atributos:**
```java
public static final int SIZE = 10;
private final List<Ship> ships;
private final boolean[][] attacks;  // Posiciones atacadas
private final boolean[][] hits;     // Impactos confirmados
```

**Métodos Clave:**
```java
boolean placeShip(Position start, Position end)
ExtendedAttackResult receiveAttackExtended(Position position)
AttackResult receiveAttack(Position position)
boolean allShipsSunk()
void reset()  // Para nueva partida
```

**Enum AttackResult:**
```java
HIT("Impacto")
MISS("Agua")
SUNK("Barco hundido")
SUNK_AND_GAME_OVER("Último barco hundido - Juego terminado")
ALREADY_ATTACKED("Ya atacado")
```

**Serialización:** Implementa `Serializable` para transferencia RMI.

#### **5.1.4 Clase Ship**
**Ubicación:** `shared/src/main/java/co/edu/uptc/shared/model/Ship.java`

**Responsabilidad:** Representa un barco en el juego.

**Características:**
- Calcula posiciones automáticamente (horizontal/vertical)
- Rastrea impactos por posición
- Determina si está hundido
- Serializable para RMI

**Métodos:**
```java
boolean occupiesPosition(Position position)
boolean hit(Position position)
boolean isSunk()
List<Position> getPositions()
int getSize()
```

#### **5.1.5 Clase Position**
**Ubicación:** `shared/src/main/java/co/edu/uptc/shared/model/Position.java`

**Responsabilidad:** Representa coordenadas (x, y) en el tablero.

**Características:**
- Inmutable (final fields)
- Implementa `equals()` y `hashCode()`
- Serializable

#### **5.1.6 Clase GameStatus**
**Ubicación:** `shared/src/main/java/co/edu/uptc/shared/model/GameStatus.java`

**Responsabilidad:** Representa el estado actual del juego.

**Estados Posibles:**
```java
WAITING_FOR_PLAYERS    // Esperando segundo jugador
PLACING_SHIPS          // Colocando barcos
READY_TO_START         // Ambos listos
IN_PROGRESS            // Juego en curso
FINISHED               // Juego terminado
```

#### **5.1.7 Clases GameStats y PlayerStats**
**Responsabilidad:** Almacenan estadísticas de juego y jugador.

**GameStats:**
- Barcos restantes
- Ataques realizados
- Impactos/fallos
- Barcos hundidos

**PlayerStats:**
- Victorias
- Derrotas
- Partidas jugadas

---

### 5.2 MÓDULO SERVER

#### **5.2.1 Clase ServerMain**
**Ubicación:** `server/src/main/java/co/edu/uptc/server/ServerMain.java`

**Responsabilidad:** Punto de entrada del servidor, configuración RMI.

**Funcionalidades:**
1. **Detección Automática de IP LAN**
```java
private static String detectLanIp() {
    // Busca interfaces de red activas
    // Filtra IPs privadas (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
    // Retorna IP LAN o localhost
}
```

2. **Configuración RMI Registry**
```java
// Crear o reutilizar registry
Registry registry;
try {
    registry = LocateRegistry.createRegistry(port);
} catch (ExportException ee) {
    registry = LocateRegistry.getRegistry(port);
}
```

3. **Publicación del Servicio**
```java
GameServiceImpl gameService = new GameServiceImpl();
registry.rebind(SERVICE_NAME, gameService);
```

4. **Información de Conexión**
- Muestra IP y puerto del servidor
- Instrucciones para clientes
- Shutdown hook para limpieza

**Configuración:**
- Puerto por defecto: 1100
- Nombre del servicio: "GameService"
- Timeouts: 10 segundos

#### **5.2.2 Clase GameServiceImpl**
**Ubicación:** `server/src/main/java/co/edu/uptc/server/GameServiceImpl.java`

**Responsabilidad:** Implementación del servicio RMI, coordinación de juegos.

**Características Principales:**

1. **Gestión de Sesiones**
```java
private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
private final Map<String, Player> players = new ConcurrentHashMap<>();
```

2. **Monitoreo de Conexiones**
```java
private void startConnectionMonitoring() {
    // Ejecutor programado para heartbeat
    // Verifica conexiones cada 5 segundos
    // Desconecta jugadores inactivos (30 segundos)
}
```

3. **Restricción LAN**
```java
private void enforceLanOnly() throws RemoteException {
    String clientHost = getClientHost();
    if (!isPrivateIp(clientHost) && !clientHost.equals("127.0.0.1")) {
        throw new RemoteException("Solo conexiones LAN permitidas");
    }
}
```

4. **Gestión de Jugadores**
```java
public String joinGame(String playerName, GameCallback callback) {
    enforceLanOnly();
    String playerId = UUID.randomUUID().toString();
    Player player = new Player(playerId, playerName, callback);
    GameSession session = findOrCreateSession();
    session.addPlayer(player);
    // Notificar a ambos jugadores
}
```

5. **Lógica de Ataque**
```java
public String attack(String playerId, Position target) {
    touchPlayer(playerId);
    GameSession session = sessions.get(playerId);
    return session.attack(playerId, target);
}
```

6. **Sistema de Revancha**
```java
public boolean requestNewGame(String playerId) {
    // Solicita nueva partida al oponente
    // Notifica mediante callback
}

public boolean respondToNewGameRequest(String playerId, boolean accepts) {
    // Procesa respuesta
    // Si acepta, resetea sesión
    // Si rechaza, notifica rechazo
}
```

**Concurrencia:**
- Usa `ConcurrentHashMap` para thread-safety
- `ScheduledExecutorService` para tareas periódicas
- Sincronización en métodos críticos

#### **5.2.3 Clase GameSession**
**Ubicación:** `server/src/main/java/co/edu/uptc/server/GameSession.java`

**Responsabilidad:** Coordina una partida entre dos jugadores.

**Atributos:**
```java
private final String sessionId;
private Player player1;
private Player player2;
private String currentTurnPlayerId;
private boolean gameStarted;
```

**Flujo de Juego:**

1. **Colocación de Barcos**
```java
public boolean placeShip(String playerId, Position start, Position end) {
    Player player = getPlayer(playerId);
    Ship newShip = new Ship(start, end);
    
    // Validar tamaño disponible
    int[] availableSizes = {5, 4, 3, 3, 2};
    // Verificar que el tamaño esté disponible
    
    return player.getBoard().placeShip(start, end);
}
```

2. **Inicio de Juego**
```java
public boolean markPlayerReady(String playerId) {
    Player player = getPlayer(playerId);
    player.setReady(true);
    
    if (bothPlayersReady()) {
        startGame();
        return true;
    }
    return false;
}
```

3. **Procesamiento de Ataques**
```java
public String attack(String playerId, Position target) {
    // Verificar turno
    if (!currentTurnPlayerId.equals(playerId)) {
        return "NOT_YOUR_TURN";
    }
    
    Player defender = getOpponent(playerId);
    Board.ExtendedAttackResult extResult = 
        defender.getBoard().receiveAttackExtended(target);
    
    // Notificar a ambos jugadores
    sendAttackStructured(attacker, defender, target, result);
    
    // Cambiar turno si no hundió barco
    if (result != AttackResult.SUNK && 
        result != AttackResult.SUNK_AND_GAME_OVER) {
        switchTurn();
    }
    
    // Verificar fin de juego
    if (result == AttackResult.SUNK_AND_GAME_OVER) {
        notifyGameEnded(attacker.getName());
    }
    
    return result.name();
}
```

4. **Reset para Nueva Partida**
```java
public void resetForNewGame() {
    // Resetear tableros
    player1.getBoard().reset();
    player2.getBoard().reset();
    
    // Resetear estados
    player1.setReady(false);
    player2.setReady(false);
    
    // Resetear turno
    currentTurnPlayerId = null;
    gameStarted = false;
}
```

#### **5.2.4 Clase Player**
**Ubicación:** `server/src/main/java/co/edu/uptc/server/Player.java`

**Responsabilidad:** Representa un jugador conectado.

**Atributos:**
```java
private final String id;
private final String name;
private final GameCallback callback;
private final Board board;
private boolean ready;
private int wins;
private int losses;
private long lastPingTime;
```

**Métodos:**
- Getters/Setters
- Gestión de estadísticas
- Actualización de heartbeat

---

### 5.3 MÓDULO CLIENT

#### **5.3.1 Clase ClientMain**
**Ubicación:** `client/src/main/java/co/edu/uptc/client/ClientMain.java`

**Responsabilidad:** Punto de entrada del cliente, conexión RMI.

**Funcionalidades:**

1. **Configuración Flexible**
```java
// Prioridad: args > system properties > env vars > default
String host = DEFAULT_HOST;
int port = DEFAULT_PORT;

if (System.getenv("BATTLESHIP_HOST") != null) 
    host = System.getenv("BATTLESHIP_HOST");
if (args.length > 0) 
    host = args[0];
```

2. **Conexión con Reintentos**
```java
private static Registry connectWithRetry(String host, int port, int maxRetries) {
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            registry.list();  // Verificar disponibilidad
            return registry;
        } catch (Exception e) {
            Thread.sleep(2000 * attempt);  // Backoff exponencial
        }
    }
    throw new Exception("No se pudo conectar");
}
```

3. **Inicialización GUI**
```java
SwingUtilities.invokeLater(() -> {
    Registry registry = connectWithRetry(host, port, 5);
    GameWindow gameWindow = new GameWindow(registry);
    gameWindow.setVisible(true);
});
```

#### **5.3.2 Clase GameWindow**
**Ubicación:** `client/src/main/java/co/edu/uptc/client/GameWindow.java`

**Responsabilidad:** Ventana principal del juego, interfaz gráfica.

**Componentes de UI:**

1. **Tableros de Juego**
```java
private BoardPanel myBoard;      // Tablero propio
private BoardPanel enemyBoard;   // Tablero enemigo
```

2. **Paneles de Control**
```java
private JPanel statusPanel;      // Estado del juego
private JPanel shipControls;     // Controles de barcos
private JPanel gameControls;     // Botones de juego
private JTextArea messageArea;   // Mensajes
```

3. **Información de Estado**
```java
private JLabel playerNameLabel;
private JLabel statusLabel;
private JLabel turnIndicator;
private GameStatsPanel statsPanel;
```

**Flujo de Interacción:**

1. **Solicitud de Nombre**
```java
private String requestPlayerName() {
    // Diálogo personalizado con validación
    // No permite nombres vacíos
    // Máximo 20 caracteres
}
```

2. **Colocación de Barcos**
```java
// Interfaz visual para colocar barcos
// Selección de tamaño
// Click en tablero para posición inicial
// Click en tablero para posición final
// Validación automática
```

3. **Ataques**
```java
private void onEnemyCellClicked(int x, int y) {
    controller.performAttack(x, y);
}
```

4. **Actualización de UI**
```java
public void setTurnIndicator(boolean isMyTurn) {
    if (isMyTurn) {
        turnIndicator.setText("🎯 TU TURNO");
        turnIndicator.setForeground(Color.GREEN);
    } else {
        turnIndicator.setText("⏳ TURNO DEL OPONENTE");
        turnIndicator.setForeground(Color.ORANGE);
    }
}
```

5. **Pantalla de Resultado**
```java
private void showGameResult(boolean won, String winnerName) {
    // Diálogo personalizado
    // Muestra ganador
    // Opciones: Nueva partida, Ver estadísticas, Cerrar
}
```

#### **5.3.3 Clase GameController**
**Ubicación:** `client/src/main/java/co/edu/uptc/client/GameController.java`

**Responsabilidad:** Controlador de lógica del cliente, intermediario con RMI.

**Funcionalidades:**

1. **Conexión al Servicio**
```java
public void connect(String playerName) {
    gameService = (GameService) registry.lookup("GameService");
    playerId = gameService.joinGame(playerName, callbackImpl);
    startHeartbeat();
}
```

2. **Heartbeat**
```java
private void startHeartbeat() {
    heartbeatExecutor.scheduleAtFixedRate(() -> {
        gameService.ping(playerId);
    }, 0, 5, TimeUnit.SECONDS);
}
```

3. **Colocación de Barcos**
```java
public boolean placeShip(Position start, Position end) {
    return gameService.placeShip(playerId, start, end);
}
```

4. **Ataques**
```java
public void performAttack(int x, int y) {
    Position target = new Position(x, y);
    String result = gameService.attack(playerId, target);
    // Actualizar UI según resultado
}
```

5. **Gestión de Partidas**
```java
public void surrender() {
    gameService.surrenderGame(playerId);
}

public void requestNewGame() {
    gameService.requestNewGame(playerId);
}
```

#### **5.3.4 Clase GameCallbackImpl**
**Ubicación:** `client/src/main/java/co/edu/uptc/client/GameCallbackImpl.java`

**Responsabilidad:** Implementación de callbacks RMI, recepción de notificaciones.

**Métodos Implementados:**

```java
public void onGameEvent(String message) {
    SwingUtilities.invokeLater(() -> {
        window.showMessage(message);
    });
}

public void onPlayerJoined(String playerName) {
    SwingUtilities.invokeLater(() -> {
        window.showMessage("Jugador conectado: " + playerName);
    });
}

public void onTurnChanged(boolean isMyTurn, String currentPlayerName) {
    SwingUtilities.invokeLater(() -> {
        window.setTurnIndicator(isMyTurn);
        window.updateStatus(isMyTurn ? "Tu turno" : "Turno de " + currentPlayerName);
    });
}

public void onGameEnded(String winner) {
    SwingUtilities.invokeLater(() -> {
        boolean won = winner.equals(window.getPlayerName());
        window.showGameResult(won, winner);
    });
}

public void onAttackEvent(String attackerName, int targetX, int targetY, 
                          String result, boolean yourBoard) {
    SwingUtilities.invokeLater(() -> {
        Position pos = new Position(targetX, targetY);
        if (yourBoard) {
            window.getMyBoard().markAttack(pos, result);
        } else {
            window.getEnemyBoard().markAttack(pos, result);
        }
    });
}

public void onNewGameRequest(String requesterName) {
    SwingUtilities.invokeLater(() -> {
        window.showNewGameRequest(requesterName);
    });
}
```

**Patrón de Diseño:**
- Callbacks ejecutados en `SwingUtilities.invokeLater()` para thread-safety
- Delegación a `GameWindow` para actualización de UI

#### **5.3.5 Clase BoardPanel**
**Ubicación:** `client/src/main/java/co/edu/uptc/client/BoardPanel.java`

**Responsabilidad:** Panel visual del tablero de juego.

**Características:**
- Grid 10x10 de celdas clickeables
- Visualización de barcos propios
- Marcado de ataques (impactos/fallos)
- Interacción para colocación y ataque

**Estados de Celda:**
- Agua (azul)
- Barco (gris)
- Impacto (rojo)
- Fallo (blanco)
- Barco hundido (negro)

---

## 6. TECNOLOGÍAS Y PATRONES

### 6.1 Java RMI (Remote Method Invocation)

**Características Utilizadas:**
- ✅ **Invocación de Métodos Remotos:** Cliente invoca métodos en servidor
- ✅ **Callbacks Bidireccionales:** Servidor notifica a clientes
- ✅ **Serialización Automática:** Objetos transferidos automáticamente
- ✅ **Registry:** Servicio de nombres para lookup
- ✅ **Stub/Skeleton:** Generación automática de proxies

**Ventajas en el Proyecto:**
- Transparencia de ubicación
- Comunicación tipo Java nativo
- Manejo automático de red
- Serialización integrada

**Desventajas:**
- Acoplamiento a Java
- Firewall/NAT pueden causar problemas
- No es RESTful (no web-friendly)

### 6.2 Patrones de Diseño Implementados

#### **6.2.1 Observer (Distribuido)**
- **Sujeto:** `GameServiceImpl`
- **Observadores:** Clientes mediante `GameCallback`
- **Notificaciones:** Eventos de juego, cambios de turno, fin de partida

#### **6.2.2 MVC (Model-View-Controller)**
- **Model:** Clases en `shared/model`
- **View:** `GameWindow`, `BoardPanel`
- **Controller:** `GameController`

#### **6.2.3 Facade**
- **Facade:** `GameServiceImpl`
- **Subsistemas:** `GameSession`, `Player`, `Board`
- **Beneficio:** Simplifica interfaz RMI

#### **6.2.4 Singleton (Implícito)**
- **Servicio RMI:** Una instancia por servidor
- **Registry:** Único punto de registro

#### **6.2.5 Strategy**
- **Contexto:** `Board`
- **Estrategias:** Diferentes tipos de ataques (futuro)

### 6.3 Principios SOLID

#### **Single Responsibility Principle (SRP)**
✅ **Cumplido:**
- `Board`: Solo gestiona tablero
- `Ship`: Solo representa barco
- `GameSession`: Solo coordina partida
- `GameWindow`: Solo UI

#### **Open/Closed Principle (OCP)**
✅ **Cumplido:**
- Interfaces RMI permiten extensión
- Enums para resultados extensibles

#### **Liskov Substitution Principle (LSP)**
✅ **Cumplido:**
- Implementaciones de interfaces son intercambiables

#### **Interface Segregation Principle (ISP)**
✅ **Cumplido:**
- `GameService` y `GameCallback` separadas
- Interfaces específicas por rol

#### **Dependency Inversion Principle (DIP)**
✅ **Cumplido:**
- Cliente depende de `GameService` (abstracción)
- Servidor depende de `GameCallback` (abstracción)

### 6.4 Concurrencia

**Mecanismos Utilizados:**

1. **ConcurrentHashMap**
```java
private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
```

2. **ScheduledExecutorService**
```java
private final ScheduledExecutorService connectionMonitor = 
    Executors.newSingleThreadScheduledExecutor();
```

3. **SwingUtilities.invokeLater()**
```java
SwingUtilities.invokeLater(() -> {
    window.updateStatus(message);
});
```

4. **Synchronized (cuando necesario)**
```java
synchronized (session) {
    // Operaciones críticas
}
```

---

## 7. FLUJO DE COMUNICACIÓN

### 7.1 Secuencia de Conexión

```
1. Cliente inicia → ClientMain.main()
2. Solicita nombre → GameWindow.requestPlayerName()
3. Conecta a Registry → LocateRegistry.getRegistry(host, port)
4. Lookup servicio → registry.lookup("GameService")
5. Join game → gameService.joinGame(name, callback)
6. Servidor crea Player → new Player(id, name, callback)
7. Asigna a sesión → findOrCreateSession().addPlayer(player)
8. Notifica callback → callback.onPlayerJoined(otherPlayerName)
9. Cliente actualiza UI → window.showMessage()
```

### 7.2 Secuencia de Colocación de Barcos

```
1. Usuario selecciona tamaño → shipControls
2. Click en celda inicial → BoardPanel.cellClicked()
3. Click en celda final → BoardPanel.cellClicked()
4. Invoca RMI → gameService.placeShip(playerId, start, end)
5. Servidor valida → GameSession.placeShip()
6. Actualiza tablero → player.getBoard().placeShip()
7. Retorna resultado → boolean success
8. Cliente actualiza UI → myBoard.addShip()
```

### 7.3 Secuencia de Ataque

```
1. Usuario click en tablero enemigo → enemyBoard.cellClicked()
2. Controlador procesa → controller.performAttack(x, y)
3. Invoca RMI → gameService.attack(playerId, target)
4. Servidor valida turno → session.attack()
5. Procesa ataque → defender.getBoard().receiveAttack()
6. Notifica a ambos → callback.onAttackEvent()
7. Cambia turno → session.switchTurn()
8. Notifica turno → callback.onTurnChanged()
9. Clientes actualizan UI → window.setTurnIndicator()
```

### 7.4 Secuencia de Fin de Juego

```
1. Último barco hundido → AttackResult.SUNK_AND_GAME_OVER
2. Actualiza estadísticas → winner.incrementWins()
3. Notifica fin → callback.onGameEnded(winnerName)
4. Clientes muestran resultado → window.showGameResult()
5. Opción revancha → window.showNewGameRequest()
6. Usuario acepta/rechaza → gameService.respondToNewGameRequest()
7. Si acepta → session.resetForNewGame()
8. Reinicia flujo → onGameEvent("Nueva partida iniciada")
```

---

## 8. CARACTERÍSTICAS IMPLEMENTADAS

### 8.1 Funcionalidades Core

✅ **Juego Multijugador Distribuido**
- Dos jugadores en red LAN
- Comunicación en tiempo real
- Sincronización de estado

✅ **Tablero 10x10**
- Visualización gráfica
- Interacción por clicks
- Marcado de ataques

✅ **Barcos Configurables**
- Tamaños: 5, 4, 3, 3, 2
- Colocación horizontal/vertical
- Validación de superposición

✅ **Sistema de Turnos**
- Alternancia automática
- Indicador visual
- Validación de turno

✅ **Detección de Victoria**
- Todos los barcos hundidos
- Notificación automática
- Actualización de estadísticas

### 8.2 Funcionalidades Avanzadas

✅ **Sistema de Callbacks Bidireccionales**
- Notificaciones en tiempo real
- Eventos de juego
- Cambios de estado

✅ **Monitoreo de Conexiones**
- Heartbeat cada 5 segundos
- Desconexión automática (30s inactividad)
- Notificación de desconexión

✅ **Restricción LAN**
- Solo IPs privadas
- Validación automática
- Seguridad básica

✅ **Sistema de Revancha**
- Solicitud de nueva partida
- Confirmación del oponente
- Reset automático

✅ **Estadísticas de Jugador**
- Victorias/Derrotas
- Persistencia en sesión
- Visualización gráfica

✅ **Detección Automática de IP**
- Busca IP LAN automáticamente
- Fallback a localhost
- Configuración manual opcional

✅ **Gestión de Sesiones**
- Múltiples sesiones simultáneas
- Asignación automática
- Limpieza de sesiones vacías

### 8.3 Interfaz de Usuario

✅ **Diseño Intuitivo**
- Dos tableros lado a lado
- Controles claros
- Mensajes informativos

✅ **Feedback Visual**
- Colores para estados
- Indicadores de turno
- Animaciones (implícitas)

✅ **Diálogos Personalizados**
- Solicitud de nombre
- Confirmaciones
- Resultados de partida

✅ **Panel de Estadísticas**
- Información en tiempo real
- Estadísticas del oponente
- Historial de partidas

---

## 9. SEGURIDAD Y CONFIGURACIÓN

### 9.1 Seguridad RMI

#### **Política de Seguridad**
```java
grant {
    permission java.security.AllPermission;
};
```

⚠️ **Riesgos:**
- Permisos completos
- Sin restricciones de acceso
- Vulnerable a código malicioso

✅ **Mitigaciones Implementadas:**
- Restricción a LAN
- Validación de IPs
- No expuesto a Internet

#### **Recomendaciones de Producción:**
```java
grant codeBase "file:/path/to/app/-" {
    permission java.net.SocketPermission "localhost:1024-", "connect,accept";
    permission java.net.SocketPermission "192.168.*:1024-", "connect,accept";
    permission java.util.PropertyPermission "java.rmi.server.hostname", "read,write";
};
```

### 9.2 Validación de Entradas

✅ **Validaciones Implementadas:**
- Nombre de jugador (no vacío, max 20 chars)
- Posiciones de barcos (dentro del tablero)
- Tamaños de barcos (permitidos)
- Turnos (solo jugador actual)
- IPs (solo LAN)

❌ **Validaciones Faltantes:**
- Sanitización de nombres (caracteres especiales)
- Rate limiting (ataques)
- Autenticación de jugadores
- Encriptación de comunicación

### 9.3 Configuración de Red

**Puerto RMI:** 1100 (configurable)
**Timeouts:**
- Response: 10 segundos
- Read: 10 segundos
- Heartbeat: 5 segundos
- Inactividad: 30 segundos

**IPs Permitidas:**
- 127.0.0.1 (localhost)
- 192.168.0.0/16 (Clase C privada)
- 10.0.0.0/8 (Clase A privada)
- 172.16.0.0/12 (Clase B privada)

### 9.4 Manejo de Errores

✅ **Excepciones Manejadas:**
- `RemoteException`: Errores RMI
- `NotBoundException`: Servicio no encontrado
- `ConnectException`: Servidor no disponible
- `IllegalArgumentException`: Parámetros inválidos

✅ **Estrategias de Recuperación:**
- Reintentos con backoff exponencial
- Notificación al usuario
- Limpieza de recursos
- Shutdown hooks

---

## 10. SCRIPTS DE COMPILACIÓN Y EJECUCIÓN

### 10.1 Script de Compilación

**Archivo:** `4_COMPILAR.bat`

```batch
@echo off
echo ============================================
echo    COMPILANDO PROYECTO BATALLA NAVAL
echo    (VERSION COMPLETA CON EMPAQUETADO)
echo ============================================

echo Limpiando y compilando todos los modulos...
mvn clean package

if %ERRORLEVEL%==0 (
  echo COMPILACION COMPLETADA EXITOSAMENTE!
  echo - Modulos: shared, server, client
  echo - JARs generados en target/
  echo - Cliente con dependencias listo
) else (
  echo ERROR EN COMPILACION
  echo Verifica Maven y dependencias
)

pause
```

**Funcionalidad:**
- Limpia compilaciones anteriores
- Compila todos los módulos
- Genera JARs
- Empaqueta cliente con dependencias

### 10.2 Script de Servidor

**Archivo:** `servidor-simple.bat`

```batch
@echo off
echo ============================================
echo   SERVIDOR BATALLA NAVAL - INICIANDO
echo ============================================

REM Compilar si es necesario
echo Compilando proyecto...
call mvn clean package -q

echo Servidor iniciando en puerto 1100...
echo Esperando clientes...

echo IMPORTANTE: Tu IP es:
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4"') do echo%%a

echo Ejecuta el servidor:
java -Djava.security.policy=all.policy ^
     -cp shared\target\shared-1.0-SNAPSHOT.jar;server\target\server-1.0-SNAPSHOT.jar ^
     co.edu.uptc.server.ServerMain

pause
```

**Funcionalidad:**
- Compila proyecto
- Muestra IP del servidor
- Inicia servidor RMI
- Configura política de seguridad

### 10.3 Script de Cliente Local

**Archivo:** `run-cliente-local.bat`

```batch
@echo off
title Cliente local - Batalla Naval

echo ============================================
echo       CLIENTE LOCAL - BATALLA NAVAL
echo           (Conexion rapida)
echo ============================================

REM Compilar si es necesario
if not exist "client\target\classes" (
    echo Compilando...
    call mvn -q compile -pl shared,client -am
)

REM Ejecutar cliente
java -Djava.security.policy=all.policy ^
     -cp shared\target\classes;client\target\classes ^
     co.edu.uptc.client.ClientMain localhost 1100

pause
```

**Funcionalidad:**
- Compila si es necesario
- Conecta a localhost:1100
- Ideal para pruebas locales

### 10.4 Script de Cliente LAN

**Archivo:** `run-client-lan-simple.bat`

```batch
@echo off
set /p SERVER_IP="Ingresa la IP del servidor: "
set /p SERVER_PORT="Ingresa el puerto (default 1100): "

if "%SERVER_PORT%"=="" set SERVER_PORT=1100

java -Djava.security.policy=all.policy ^
     -cp shared\target\classes;client\target\classes ^
     co.edu.uptc.client.ClientMain %SERVER_IP% %SERVER_PORT%

pause
```

**Funcionalidad:**
- Solicita IP del servidor
- Solicita puerto (opcional)
- Conecta a servidor remoto

### 10.5 Script de Empaquetado

**Archivo:** `build-dist-client.bat`

```batch
@echo off
echo ============================================
echo   EMPAQUETANDO CLIENTE PARA DISTRIBUCION
echo ============================================

call mvn clean package

if %ERRORLEVEL%==0 (
    echo Copiando archivos...
    xcopy /Y client\target\battleship-client-jar-with-dependencies.jar dist\
    xcopy /Y all.policy dist\
    xcopy /Y run-client-lan.bat dist\
    
    echo ============================================
    echo   EMPAQUETADO COMPLETADO!
    echo   Archivos en carpeta dist/
    echo ============================================
)

pause
```

**Funcionalidad:**
- Compila y empaqueta
- Crea JAR con dependencias
- Copia archivos necesarios
- Genera distribución portable

---

## 11. ANÁLISIS DE CALIDAD DEL CÓDIGO

### 11.1 Fortalezas

✅ **Arquitectura Clara**
- Separación de responsabilidades
- Módulos bien definidos
- Interfaces limpias

✅ **Código Documentado**
- Javadoc en interfaces
- Comentarios explicativos
- Nombres descriptivos

✅ **Manejo de Concurrencia**
- Uso de `ConcurrentHashMap`
- Thread-safety en UI (SwingUtilities)
- Ejecutores programados

✅ **Patrones de Diseño**
- Observer, MVC, Facade
- Principios SOLID
- Separación de capas

✅ **Gestión de Errores**
- Try-catch apropiados
- Logging con `Logger`
- Mensajes informativos

✅ **Serialización**
- Objetos correctamente serializables
- `serialVersionUID` definido
- Inmutabilidad donde corresponde

### 11.2 Áreas de Mejora

❌ **Testing**
- No hay pruebas unitarias
- No hay pruebas de integración
- No hay mocks para RMI

**Recomendación:**
```java
// Ejemplo de test unitario
@Test
public void testBoardPlaceShip() {
    Board board = new Board();
    Position start = new Position(0, 0);
    Position end = new Position(0, 4);
    
    assertTrue(board.placeShip(start, end));
    assertEquals(1, board.getShipCount());
}
```

❌ **Validación de Datos**
- Sanitización de nombres limitada
- No hay rate limiting
- Validación de IPs básica

**Recomendación:**
```java
private String sanitizeName(String name) {
    return name.replaceAll("[^a-zA-Z0-9 ]", "")
               .trim()
               .substring(0, Math.min(20, name.length()));
}
```

❌ **Persistencia**
- Estadísticas no persisten
- No hay base de datos
- Sesiones se pierden al reiniciar

**Recomendación:**
```java
// Usar SQLite o archivos JSON
public void saveStats(PlayerStats stats) {
    try (FileWriter writer = new FileWriter("stats.json")) {
        gson.toJson(stats, writer);
    }
}
```

❌ **Logging**
- Logging básico
- No hay niveles configurables
- No hay rotación de logs

**Recomendación:**
```java
// Usar Log4j o SLF4J
private static final Logger LOGGER = LoggerFactory.getLogger(GameServiceImpl.class);
LOGGER.info("Jugador {} conectado desde {}", playerName, clientHost);
```

❌ **Configuración**
- Valores hardcodeados
- No hay archivo de configuración
- Difícil personalización

**Recomendación:**
```properties
# config.properties
server.port=1100
server.max.sessions=10
heartbeat.interval=5000
connection.timeout=30000
```

❌ **Internacionalización**
- Mensajes en español hardcodeados
- No hay soporte multi-idioma

**Recomendación:**
```java
ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
String welcome = messages.getString("welcome.message");
```

### 11.3 Métricas de Código

**Complejidad Ciclomática:**
- Mayoría de métodos: Baja (1-5)
- Algunos métodos: Media (6-10)
- Pocos métodos: Alta (11+)

**Líneas de Código:**
- `GameServiceImpl`: ~490 líneas (considerar refactorización)
- `GameWindow`: ~770 líneas (considerar separación)
- `GameSession`: ~295 líneas (aceptable)

**Acoplamiento:**
- Bajo entre módulos (gracias a interfaces)
- Medio dentro de módulos
- Alto en UI (GameWindow)

**Cohesión:**
- Alta en clases de modelo
- Media en clases de servicio
- Baja en GameWindow (hace demasiado)

### 11.4 Deuda Técnica

**Deuda Baja:**
- Falta de tests
- Logging básico
- Configuración hardcodeada

**Deuda Media:**
- GameWindow muy grande
- Falta de persistencia
- Validación limitada

**Deuda Alta:**
- Política de seguridad permisiva
- No hay autenticación
- No hay encriptación

---

## 12. CONCLUSIONES Y RECOMENDACIONES

### 12.1 Logros del Proyecto

✅ **Implementación Exitosa de RMI**
- Comunicación cliente-servidor funcional
- Callbacks bidireccionales operativos
- Serialización correcta de objetos

✅ **Arquitectura Sólida**
- Separación clara de responsabilidades
- Módulos independientes
- Interfaces bien definidas

✅ **Funcionalidad Completa**
- Juego jugable de principio a fin
- Gestión de sesiones
- Sistema de revancha

✅ **Interfaz de Usuario**
- Intuitiva y funcional
- Feedback visual adecuado
- Experiencia de usuario aceptable

### 12.2 Recomendaciones de Mejora

#### **Corto Plazo (1-2 semanas)**

1. **Agregar Tests Unitarios**
   - JUnit para clases de modelo
   - Mockito para servicios RMI
   - Cobertura mínima 60%

2. **Mejorar Logging**
   - Implementar SLF4J + Logback
   - Niveles configurables
   - Rotación de archivos

3. **Externalizar Configuración**
   - Archivo `config.properties`
   - Variables de entorno
   - Argumentos de línea de comandos

#### **Medio Plazo (1-2 meses)**

4. **Implementar Persistencia**
   - SQLite para estadísticas
   - Historial de partidas
   - Rankings de jugadores

5. **Refactorizar GameWindow**
   - Separar en componentes más pequeños
   - Extraer lógica a controladores
   - Mejorar cohesión

6. **Mejorar Seguridad**
   - Política de seguridad restrictiva
   - Autenticación básica
   - Validación robusta de entradas

#### **Largo Plazo (3-6 meses)**

7. **Migrar a Tecnología Moderna**
   - Considerar REST API (Spring Boot)
   - WebSockets para tiempo real
   - Frontend web (React/Vue)

8. **Agregar Características**
   - Chat entre jugadores
   - Múltiples modos de juego
   - Torneos y rankings globales

9. **Escalabilidad**
   - Soporte para más de 2 jugadores
   - Múltiples partidas simultáneas
   - Servidor distribuido (cluster)

### 12.3 Valoración Final

**Puntos Fuertes:**
- ⭐⭐⭐⭐⭐ Arquitectura y diseño
- ⭐⭐⭐⭐☆ Implementación RMI
- ⭐⭐⭐⭐☆ Funcionalidad
- ⭐⭐⭐☆☆ Interfaz de usuario
- ⭐⭐⭐☆☆ Calidad de código

**Puntos Débiles:**
- ⭐☆☆☆☆ Testing
- ⭐⭐☆☆☆ Seguridad
- ⭐⭐☆☆☆ Persistencia
- ⭐⭐⭐☆☆ Configurabilidad
- ⭐⭐⭐☆☆ Documentación

**Calificación Global: 7.5/10**

Este proyecto demuestra una **sólida comprensión de Java RMI** y **arquitectura distribuida**. La implementación es funcional y bien estructurada, con oportunidades claras de mejora en testing, seguridad y persistencia. Es un excelente proyecto académico que podría evolucionar a un producto más robusto con las mejoras sugeridas.

---

## APÉNDICES

### A. Comandos Útiles

**Compilar proyecto:**
```bash
mvn clean package
```

**Ejecutar servidor:**
```bash
java -Djava.security.policy=all.policy \
     -cp shared/target/shared-1.0-SNAPSHOT.jar:server/target/server-1.0-SNAPSHOT.jar \
     co.edu.uptc.server.ServerMain
```

**Ejecutar cliente:**
```bash
java -Djava.security.policy=all.policy \
     -cp shared/target/classes:client/target/classes \
     co.edu.uptc.client.ClientMain localhost 1100
```

**Limpiar proyecto:**
```bash
mvn clean
```

**Ver dependencias:**
```bash
mvn dependency:tree
```

### B. Estructura de Paquetes

```
co.edu.uptc
├── client
│   ├── BoardPanel
│   ├── ClientMain
│   ├── GameCallbackImpl
│   ├── GameController
│   ├── GameStatsPanel
│   ├── GameWindow
│   └── NetworkDiscovery
├── server
│   ├── GameServiceImpl
│   ├── GameSession
│   ├── Player
│   └── ServerMain
└── shared
    ├── interfaces
    │   ├── GameCallback
    │   └── GameService
    └── model
        ├── Board
        ├── GameStats
        ├── GameStatus
        ├── PlayerStats
        ├── Position
        └── Ship
```

### C. Puertos y Protocolos

| Componente | Puerto | Protocolo | Descripción |
|------------|--------|-----------|-------------|
| RMI Registry | 1100 | TCP | Registro de servicios |
| RMI Callbacks | Aleatorio | TCP | Comunicación bidireccional |
| Heartbeat | N/A | RMI | Verificación de conexión |

### D. Referencias

- [Java RMI Tutorial](https://docs.oracle.com/javase/tutorial/rmi/)
- [Maven Multi-Module Projects](https://maven.apache.org/guides/mini/guide-multiple-modules.html)
- [Java Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Effective Java (Joshua Bloch)](https://www.oreilly.com/library/view/effective-java/9780134686097/)

---

**Fin del Análisis**

*Documento generado el 25 de Noviembre de 2025*  
*Proyecto: Battleship RMI - César Caro (202221682)*
