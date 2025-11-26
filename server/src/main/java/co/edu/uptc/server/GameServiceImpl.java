package co.edu.uptc.server;

import co.edu.uptc.shared.interfaces.GameService;
import co.edu.uptc.shared.interfaces.GameCallback;
import co.edu.uptc.shared.model.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.RemoteServer;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Implementación del servicio RMI de Batalla Naval
 * Versión tolerante a fallos de red/firewall, callbacks asincrónicos.
 */
public class GameServiceImpl extends UnicastRemoteObject implements GameService {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(GameServiceImpl.class.getName());

    // Estructuras thread-safe
    private final Map<String, Player> players;
    private final Map<String, GameSession> playerToSession;
    private final Map<String, Long> playerLastSeen;
    private final Map<String, String> pendingNewGameRequests;

    // Timeouts (milisegundos)
    private static final long HARD_TIMEOUT_MS = 90_000L; // 90s eliminación definitiva
    private static final long SOFT_TIMEOUT_MS = 30_000L; // 30s considerado inactivo

    private GameSession currentSession;
    private final AtomicInteger playerCounter;

    private final ScheduledExecutorService heartbeatExecutor;
    private final ExecutorService callbackExecutor;

    public GameServiceImpl(int port) throws RemoteException {
        super(port); // Exporta en puerto fijo para evitar bloqueos de firewall
        this.players = new ConcurrentHashMap<>();
        this.playerToSession = new ConcurrentHashMap<>();
        this.playerLastSeen = new ConcurrentHashMap<>();
        this.pendingNewGameRequests = new ConcurrentHashMap<>();
        this.playerCounter = new AtomicInteger(1);

        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-monitor");
            t.setDaemon(true);
            return t;
        });
        this.callbackExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "callback-worker");
            t.setDaemon(true);
            return t;
        });

        startConnectionMonitoring();

        LOGGER.info("Servicio RMI de Batalla Naval inicializado con monitoreo de conexiones");
    }

    @Override
    public synchronized String joinGame(String playerName, GameCallback callback) throws RemoteException {
        // Se acepta la conexión (enforceLanOnly no bloquea aquí para facilitar juego
        // WAN)
        enforceLanOnly();

        LOGGER.info("Solicitud de conexión de jugador: " + playerName);

        String playerId = "player_" + playerCounter.getAndIncrement();
        Player player = new Player(playerId, playerName, callback);

        players.put(playerId, player);
        playerLastSeen.put(playerId, System.currentTimeMillis());

        GameSession session = findOrCreateSession();
        boolean added = session.addPlayer(player);

        if (added) {
            playerToSession.put(playerId, session);
            LOGGER.info("Jugador " + playerName + " (" + playerId + ") conectado al sistema distribuido");

            // Notificar asincrónicamente, tolerando fallos (firewall/NAT)
            callbackExecutor.execute(() -> {
                try {
                    Thread.sleep(500); // darle tiempo al cliente para terminar init UI
                    callback.onGameEvent("Conectado al servidor. Esperando oponente...");
                } catch (RemoteException re) {
                    LOGGER.warning(
                            "Error notificando conexión a " + playerId + " (RemoteException): " + re.getMessage());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (Exception ex) {
                    LOGGER.warning("Error notificando conexión a " + playerId + ": " + ex.getMessage());
                }
            });

            return "SUCCESS:" + playerId + ":" + session.getSessionId();
        } else {
            players.remove(playerId);
            throw new RemoteException("No se pudo unir al juego - Servidor lleno");
        }
    }

    @Override
    public boolean placeShip(String playerId, Position start, Position end) throws RemoteException {
        LOGGER.info("Solicitud colocar barco de " + playerId + ": " + start + " a " + end);

        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            LOGGER.warning("Sesión no encontrada para jugador: " + playerId);
            return false;
        }

        try {
            touchPlayer(playerId);
            return session.placeShip(playerId, start, end);
        } catch (Exception e) {
            LOGGER.warning("Error colocando barco para " + playerId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public String attack(String playerId, Position target) throws RemoteException {
        LOGGER.info("Ataque de " + playerId + " a posición " + target);

        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            return "ERROR_SESSION";
        }

        try {
            touchPlayer(playerId);
            Board.AttackResult result = session.attack(playerId, target);
            if (result != null) {
                return result.name();
            }
            return "NOT_YOUR_TURN";
        } catch (Exception e) {
            LOGGER.warning("Error en ataque de " + playerId + ": " + e.getMessage());
            return "ERROR";
        }
    }

    @Override
    public GameStatus getGameStatus(String playerId) throws RemoteException {
        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            return GameStatus.waiting(0);
        }
        touchPlayer(playerId);
        return session.getGameStatus(playerId);
    }

    @Override
    public boolean setPlayerReady(String playerId) throws RemoteException {
        LOGGER.info("Jugador " + playerId + " marcado como listo (solicitud)");
        playerLastSeen.put(playerId, System.currentTimeMillis());
        GameSession session = playerToSession.get(playerId);
        if (session == null)
            return false;
        touchPlayer(playerId);
        return session.markPlayerReady(playerId);
    }

    @Override
    public void disconnectPlayer(String playerId) throws RemoteException {
        LOGGER.info("Desconectando jugador: " + playerId);

        Player player = players.get(playerId);
        if (player != null) {
            players.remove(playerId);
            playerToSession.remove(playerId);
            playerLastSeen.remove(playerId);

            LOGGER.info("Jugador " + player.getName() + " (" + playerId + ") desconectado del sistema distribuido");
        }
    }

    public boolean ping(String playerId) throws RemoteException {
        if (players.containsKey(playerId)) {
            playerLastSeen.put(playerId, System.currentTimeMillis());
            touchPlayer(playerId);
            return true;
        }
        return false;
    }

    private GameSession findOrCreateSession() {
        if (currentSession == null || currentSession.isFull()) {
            String sessionId = "session_" + System.currentTimeMillis();
            currentSession = new GameSession(sessionId);
            LOGGER.info("Nueva sesión de juego distribuida creada: " + sessionId);
        }
        return currentSession;
    }

    public String getServerStats() {
        return String.format("Jugadores conectados: %d, Sesiones activas: %d",
                players.size(),
                currentSession != null ? 1 : 0);
    }

    private void startConnectionMonitoring() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();

            // Notificar inactivos suavemente (asincrónico, silencioso)
            players.values().forEach(p -> {
                long idle = currentTime - p.getLastActivity();
                if (idle > SOFT_TIMEOUT_MS) {
                    callbackExecutor.execute(() -> {
                        try {
                            p.getCallback()
                                    .onGameEvent("Conexión lenta detectada para " + p.getName() + " (reintentando)...");
                        } catch (Exception ignored) {
                            // Silenciar errores (firewall/NAT)
                        }
                    });
                }
            });

            // Eliminación dura
            playerLastSeen.entrySet().removeIf(entry -> {
                String playerId = entry.getKey();
                long lastSeen = entry.getValue();
                if (currentTime - lastSeen > HARD_TIMEOUT_MS) {
                    LOGGER.warning("Jugador " + playerId + " eliminado por inactividad prolongada");
                    try {
                        disconnectPlayer(playerId);
                    } catch (RemoteException e) {
                        LOGGER.warning("Error desconectando (hard): " + e.getMessage());
                    }
                    return true;
                }
                return false;
            });
        }, 10, 10, TimeUnit.SECONDS);
    }

    private void touchPlayer(String playerId) {
        Player p = players.get(playerId);
        if (p != null) {
            p.touchActivity();
            playerLastSeen.put(playerId, System.currentTimeMillis());
        }
    }

    /**
     * Acepta conexiones desde Internet (no bloquea), pero registra origen.
     */
    private void enforceLanOnly() throws RemoteException {
        try {
            String host = RemoteServer.getClientHost();
            InetAddress addr = InetAddress.getByName(host);
            String ip = addr.getHostAddress();

            LOGGER.info("✅ Conexión ACEPTADA desde: " + ip);
            // No bloquear; registro y auditoría
        } catch (Exception e) {
            throw new RemoteException("No se pudo validar origen", e);
        }
    }

    @Override
    public boolean surrenderGame(String playerId) throws RemoteException {
        LOGGER.info("Jugador " + playerId + " se rinde");

        Player player = players.get(playerId);
        if (player == null) {
            LOGGER.warning("Jugador no encontrado para rendición: " + playerId);
            return false;
        }

        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            LOGGER.warning("Sesión no encontrada para rendición: " + playerId);
            return false;
        }

        try {
            touchPlayer(playerId);

            String opponentId = session.getOpponentId(playerId);
            Player opponent = players.get(opponentId);

            if (opponent != null) {
                player.addLoss();
                opponent.addWin();

                player.endGame(false);
                opponent.endGame(true);

                // Notificaciones asincrónicas tolerantes a fallos
                callbackExecutor.execute(() -> {
                    try {
                        player.getCallback().onGameEvent("Te has rendido. Has perdido la partida.");
                        opponent.getCallback().onGameEvent("¡Victoria! Tu oponente se ha rendido.");
                        player.getCallback().onGameEnded(opponent.getName());
                        opponent.getCallback().onGameEnded(opponent.getName());
                    } catch (Exception ignored) {
                        // Silenciar errores
                    }
                });

                LOGGER.info("Partida terminada por rendición: " + player.getName() + " se rindió, ganó "
                        + opponent.getName());
            }

            return true;

        } catch (Exception e) {
            LOGGER.severe("Error procesando rendición: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean requestNewGame(String playerId) throws RemoteException {
        LOGGER.info("Jugador " + playerId + " solicita nueva partida");

        Player player = players.get(playerId);
        if (player == null) {
            LOGGER.warning("Jugador no encontrado para nueva partida: " + playerId);
            return false;
        }

        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            LOGGER.warning("Sesión no encontrada para nueva partida: " + playerId);
            return false;
        }

        try {
            touchPlayer(playerId);

            String opponentId = session.getOpponentId(playerId);
            Player opponent = players.get(opponentId);

            if (opponent != null) {
                pendingNewGameRequests.put(opponentId, playerId);

                // Enviar solicitud de confirmación al oponente (asincrónico)
                callbackExecutor.execute(() -> {
                    try {
                        opponent.getCallback().onNewGameRequest(player.getName());
                        player.getCallback().onGameEvent("📤 Solicitud de revancha enviada a " + opponent.getName()
                                + ". Esperando respuesta...");
                        LOGGER.info("Solicitud de nueva partida enviada de " + player.getName() + " a "
                                + opponent.getName());
                    } catch (Exception e) {
                        LOGGER.warning("Error enviando solicitud al oponente: " + e.getMessage());
                        pendingNewGameRequests.remove(opponentId);
                    }
                });
            }

            return true;

        } catch (Exception e) {
            LOGGER.severe("Error solicitando nueva partida: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getPlayerStats(String playerId) throws RemoteException {
        Player player = players.get(playerId);
        if (player == null) {
            LOGGER.warning("Jugador no encontrado para estadísticas: " + playerId);
            return "0:0";
        }

        touchPlayer(playerId);
        String stats = player.getStatsString();
        LOGGER.info("Estadísticas de " + player.getName() + ": " + stats);
        return stats;
    }

    @Override
    public boolean respondToNewGameRequest(String playerId, boolean accepts) throws RemoteException {
        LOGGER.info(
                "Jugador " + playerId + " responde a solicitud de nueva partida: " + (accepts ? "ACEPTA" : "RECHAZA"));

        Player responder = players.get(playerId);
        if (responder == null) {
            LOGGER.warning("Jugador no encontrado para responder solicitud: " + playerId);
            return false;
        }

        String requesterId = pendingNewGameRequests.remove(playerId);
        if (requesterId == null) {
            LOGGER.warning("No hay solicitud pendiente para jugador: " + playerId);
            return false;
        }

        Player requester = players.get(requesterId);
        if (requester == null) {
            LOGGER.warning("Solicitante no encontrado: " + requesterId);
            return false;
        }

        try {
            touchPlayer(playerId);

            if (accepts) {
                GameSession session = playerToSession.get(playerId);
                if (session != null) {
                    session.resetForNewGame();

                    requester.resetForNewGame();
                    responder.resetForNewGame();

                    // Notificaciones asincrónicas tolerantes a fallos
                    callbackExecutor.execute(() -> {
                        try {
                            requester.getCallback()
                                    .onGameEvent("✅ ¡Revancha aceptada! Nueva partida iniciada. Coloca tus barcos.");
                            responder.getCallback().onGameEvent("✅ ¡Nueva partida iniciada! Coloca tus barcos.");
                            LOGGER.info("Nueva partida iniciada entre " + requester.getName() + " y "
                                    + responder.getName());
                        } catch (Exception ignored) {
                            // Silenciar errores
                        }
                    });
                }
            } else {
                callbackExecutor.execute(() -> {
                    try {
                        requester.getCallback()
                                .onGameEvent("❌ Tu solicitud de revancha fue rechazada por " + responder.getName());
                        responder.getCallback().onGameEvent("❌ Revancha rechazada.");
                        LOGGER.info("Solicitud de nueva partida rechazada por " + responder.getName());
                    } catch (Exception ignored) {
                        // Silenciar errores
                    }
                });
            }

            return true;

        } catch (Exception e) {
            LOGGER.severe("Error procesando respuesta a nueva partida: " + e.getMessage());
            return false;
        }
    }

    // Métodos de consulta adicionales (estadísticas/oponente)
    public GameStats getGameStats(String playerId) throws RemoteException {
        Player player = players.get(playerId);
        if (player == null)
            return null;
        return player.getCurrentGameStats();
    }

    public GameStats getOpponentStats(String playerId) throws RemoteException {
        GameSession session = playerToSession.get(playerId);
        if (session == null)
            return null;
        return session.

                getOpponentStats(playerId);
    }
}
