package co.edu.uptc.server;

import co.edu.uptc.shared.messages.ServerMessage;
import co.edu.uptc.shared.model.*;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * WebSocket version of GameService
 * Replaces RMI callbacks with WebSocket Session send
 */
public class GameServiceWebSocket {
    private static final Logger LOGGER = Logger.getLogger(GameServiceWebSocket.class.getName());

    // Structures
    private final Map<String, Player> players;
    private final Map<String, GameSession> playerToSession;
    private final Map<String, Long> playerLastSeen;
    private final Map<String, String> pendingNewGameRequests;

    // Timeouts
    private static final long HARD_TIMEOUT_MS = 90_000L;
    private static final long SOFT_TIMEOUT_MS = 30_000L;

    private GameSession currentSession;
    private final AtomicInteger playerCounter;

    private final ScheduledExecutorService heartbeatExecutor;
    private final ExecutorService callbackExecutor;

    public GameServiceWebSocket() {
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

        LOGGER.info("Game Service WebSocket initialized");
    }

    public synchronized String joinGame(String playerName, Session session) {
        LOGGER.info("Join request from: " + playerName);

        String playerId = "player_" + playerCounter.getAndIncrement();
        Player player = new Player(playerId, playerName, session);

        players.put(playerId, player);
        playerLastSeen.put(playerId, System.currentTimeMillis());

        GameSession gameSession = findOrCreateSession();
        boolean added = gameSession.addPlayer(player);

        if (added) {
            playerToSession.put(playerId, gameSession);
            LOGGER.info("Player " + playerName + " (" + playerId + ") joined");

            // Send initial notification asynchronously
            callbackExecutor.execute(() -> {
                try {
                    Thread.sleep(500);
                    sendToPlayer(playerId, ServerMessage.gameEvent("Conectado al servidor. Esperando oponente..."));
                } catch (Exception ignored) {
                }
            });

            return "SUCCESS:" + playerId + ":" + gameSession.getSessionId();
        } else {
            players.remove(playerId);
            return "ERROR:Server full";
        }
    }

    public boolean placeShip(String playerId, Position start, Position end) {
        LOGGER.info("Place ship request from " + playerId);

        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            return false;
        }

        try {
            touchPlayer(playerId);
            return session.placeShip(playerId, start, end);
        } catch (Exception e) {
            LOGGER.warning("Error placing ship: " + e.getMessage());
            return false;
        }
    }

    public String attack(String playerId, Position target) {
        LOGGER.info("Attack from " + playerId + " to " + target);

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
            LOGGER.warning("Error in attack: " + e.getMessage());
            return "ERROR";
        }
    }

    public GameStatus getGameStatus(String playerId) {
        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            return GameStatus.waiting(0);
        }
        touchPlayer(playerId);
        return session.getGameStatus(playerId);
    }

    public boolean setPlayerReady(String playerId) {
        LOGGER.info("Player " + playerId + " ready");
        playerLastSeen.put(playerId, System.currentTimeMillis());
        GameSession session = playerToSession.get(playerId);
        if (session == null)
            return false;
        touchPlayer(playerId);
        return session.markPlayerReady(playerId);
    }

    public void disconnectPlayer(String playerId) {
        LOGGER.info("Disconnecting player: " + playerId);

        Player player = players.get(playerId);
        if (player != null) {
            players.remove(playerId);
            playerToSession.remove(playerId);
            playerLastSeen.remove(playerId);

            LOGGER.info("Player " + player.getName() + " disconnected");
        }
    }

    public boolean ping(String playerId) {
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
            LOGGER.info("New game session created: " + sessionId);
        }
        return currentSession;
    }

    private void startConnectionMonitoring() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();

            players.values().forEach(p -> {
                long idle = currentTime - p.getLastActivity();
                if (idle > SOFT_TIMEOUT_MS) {
                    callbackExecutor.execute(() -> {
                        try {
                            sendToPlayer(p.getId(), ServerMessage.gameEvent("Conexión lenta detectada..."));
                        } catch (Exception ignored) {
                        }
                    });
                }
            });

            playerLastSeen.entrySet().removeIf(entry -> {
                String playerId = entry.getKey();
                long lastSeen = entry.getValue();
                if (currentTime - lastSeen > HARD_TIMEOUT_MS) {
                    LOGGER.warning("Player " + playerId + " removed due to inactivity");
                    try {
                        disconnectPlayer(playerId);
                    } catch (Exception e) {
                        LOGGER.warning("Error disconnecting: " + e.getMessage());
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

    public boolean surrenderGame(String playerId) {
        LOGGER.info("Player " + playerId + " surrenders");

        Player player = players.get(playerId);
        if (player == null) {
            return false;
        }

        GameSession session = playerToSession.get(playerId);
        if (session == null) {
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

                callbackExecutor.execute(() -> {
                    try {
                        sendToPlayer(playerId, ServerMessage.gameEvent("Te has rendido. Has perdido."));
                        sendToPlayer(opponentId, ServerMessage.gameEvent("¡Victoria! Tu oponente se rindió."));
                        sendToPlayer(playerId, ServerMessage.gameEnded(opponent.getName()));
                        sendToPlayer(opponentId, ServerMessage.gameEnded(opponent.getName()));
                    } catch (Exception ignored) {
                    }
                });
            }

            return true;
        } catch (Exception e) {
            LOGGER.severe("Error surrendering: " + e.getMessage());
            return false;
        }
    }

    public boolean requestNewGame(String playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            return false;
        }

        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            return false;
        }

        try {
            touchPlayer(playerId);

            String opponentId = session.getOpponentId(playerId);
            Player opponent = players.get(opponentId);

            if (opponent != null) {
                pendingNewGameRequests.put(opponentId, playerId);

                callbackExecutor.execute(() -> {
                    try {
                        sendToPlayer(opponentId, ServerMessage.newGameRequest(player.getName()));
                        sendToPlayer(playerId, ServerMessage.gameEvent("📤 Solicitud de revancha enviada..."));
                    } catch (Exception e) {
                        pendingNewGameRequests.remove(opponentId);
                    }
                });
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getPlayerStats(String playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            return "0:0";
        }

        touchPlayer(playerId);
        return player.getStatsString();
    }

    public boolean respondToNewGameRequest(String playerId, boolean accepts) {
        Player responder = players.get(playerId);
        if (responder == null) {
            return false;
        }

        String requesterId = pendingNewGameRequests.remove(playerId);
        if (requesterId == null) {
            return false;
        }

        Player requester = players.get(requesterId);
        if (requester == null) {
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

                    callbackExecutor.execute(() -> {
                        try {
                            sendToPlayer(requesterId,
                                    ServerMessage.gameEvent("✅ ¡Revancha aceptada! Coloca tus barcos."));
                            sendToPlayer(playerId,
                                    ServerMessage.gameEvent("✅ ¡Nueva partida iniciada! Coloca tus barcos."));
                        } catch (Exception ignored) {
                        }
                    });
                }
            } else {
                callbackExecutor.execute(() -> {
                    try {
                        sendToPlayer(requesterId, ServerMessage.gameEvent("❌ Revancha rechazada"));
                        sendToPlayer(playerId, ServerMessage.gameEvent("❌ Revancha rechazada"));
                    } catch (Exception ignored) {
                    }
                });
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public GameStats getGameStats(String playerId) {
        Player player = players.get(playerId);
        if (player == null)
            return null;
        return player.getCurrentGameStats();
    }

    public GameStats getOpponentStats(String playerId) {
        GameSession session = playerToSession.get(playerId);
        if (session == null)
            return null;
        return session.getOpponentStats(playerId);
    }

    // WebSocket send helper
    private void sendToPlayer(String playerId, ServerMessage message) {
        GameWebSocketServer.sendToPlayer(playerId, message);
    }
}
