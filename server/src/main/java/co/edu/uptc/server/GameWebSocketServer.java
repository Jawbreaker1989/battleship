package co.edu.uptc.server;

import co.edu.uptc.shared.messages.ClientMessage;
import co.edu.uptc.shared.messages.ServerMessage;
import com.google.gson.Gson;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * WebSocket server endpoint for Battleship game
 * Handles bidirectional communication with clients
 */
@ServerEndpoint("/battleship")
public class GameWebSocketServer {
    private static final Logger LOGGER = Logger.getLogger(GameWebSocketServer.class.getName());
    private static final Gson gson = new Gson();

    // Thread-safe collections
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Map<Session, String> sessionToPlayerId = new ConcurrentHashMap<>();
    private static final Map<String, Session> playerIdToSession = new ConcurrentHashMap<>();

    // The actual game service that handles game logic
    private static GameServiceWebSocket gameService;

    static {
        try {
            gameService = new GameServiceWebSocket();
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize game service: " + e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        LOGGER.info("New client connected: " + session.getId());
        LOGGER.info("Total connected clients: " + sessions.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            LOGGER.info("Received message: " + message);

            ClientMessage clientMsg = gson.fromJson(message, ClientMessage.class);
            String action = clientMsg.getAction();

            if (action == null) {
                sendError(session, "Invalid message: no action specified");
                return;
            }

            switch (action) {
                case "join":
                    handleJoin(session, clientMsg);
                    break;

                case "placeShip":
                    handlePlaceShip(session, clientMsg);
                    break;

                case "attack":
                    handleAttack(session, clientMsg);
                    break;

                case "ready":
                    handleReady(session, clientMsg);
                    break;

                case "surrender":
                    handleSurrender(session, clientMsg);
                    break;

                case "requestNewGame":
                    handleRequestNewGame(session, clientMsg);
                    break;

                case "respondNewGame":
                    handleRespondNewGame(session, clientMsg);
                    break;

                case "ping":
                    handlePing(session, clientMsg);
                    break;

                default:
                    sendError(session, "Unknown action: " + action);
            }

        } catch (Exception e) {
            LOGGER.severe("Error processing message: " + e.getMessage());
            e.printStackTrace();
            sendError(session, "Server error: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.info("Client disconnected: " + session.getId() + ", reason: " + reason);

        String playerId = sessionToPlayerId.remove(session);
        if (playerId != null) {
            playerIdToSession.remove(playerId);
            try {
                gameService.disconnectPlayer(playerId);
            } catch (Exception e) {
                LOGGER.warning("Error disconnecting player: " + e.getMessage());
            }
        }

        sessions.remove(session);
        LOGGER.info("Total connected clients: " + sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.severe("WebSocket error for session " + session.getId() + ": " + throwable.getMessage());
        throwable.printStackTrace();
    }

    // Action handlers

    private void handleJoin(Session session, ClientMessage msg) {
        try {
            String result = gameService.joinGame(msg.getPlayerName(), session);

            if (result.startsWith("SUCCESS:")) {
                String[] parts = result.split(":");
                String playerId = parts[1];
                String sessionId = parts[2];

                sessionToPlayerId.put(session, playerId);
                playerIdToSession.put(playerId, session);

                ServerMessage response = ServerMessage.joined(playerId, sessionId);
                sendToSession(session, response);

                LOGGER.info("Player joined: " + msg.getPlayerName() + " with ID: " + playerId);
            } else {
                sendError(session, "Failed to join game: " + result);
            }
        } catch (Exception e) {
            LOGGER.severe("Error handling join: " + e.getMessage());
            sendError(session, "Error joining game: " + e.getMessage());
        }
    }

    private void handlePlaceShip(Session session, ClientMessage msg) {
        try {
            boolean success = gameService.placeShip(msg.getPlayerId(), msg.getStart(), msg.getEnd());

            ServerMessage response = ServerMessage.gameEvent(
                    success ? "Barco colocado exitosamente" : "Error colocando barco");
            sendToSession(session, response);
        } catch (Exception e) {
            sendError(session, "Error placing ship: " + e.getMessage());
        }
    }

    private void handleAttack(Session session, ClientMessage msg) {
        try {
            String result = gameService.attack(msg.getPlayerId(), msg.getTarget());

            ServerMessage response = ServerMessage.attackResult(result);
            sendToSession(session, response);
        } catch (Exception e) {
            sendError(session, "Error processing attack: " + e.getMessage());
        }
    }

    private void handleReady(Session session, ClientMessage msg) {
        try {
            boolean success = gameService.setPlayerReady(msg.getPlayerId());

            ServerMessage response = ServerMessage.gameEvent(
                    success ? "Marcado como listo" : "Error marcando como listo");
            sendToSession(session, response);
        } catch (Exception e) {
            sendError(session, "Error setting ready: " + e.getMessage());
        }
    }

    private void handleSurrender(Session session, ClientMessage msg) {
        try {
            gameService.surrenderGame(msg.getPlayerId());
        } catch (Exception e) {
            sendError(session, "Error surrendering: " + e.getMessage());
        }
    }

    private void handleRequestNewGame(Session session, ClientMessage msg) {
        try {
            gameService.requestNewGame(msg.getPlayerId());
        } catch (Exception e) {
            sendError(session, "Error requesting new game: " + e.getMessage());
        }
    }

    private void handleRespondNewGame(Session session, ClientMessage msg) {
        try {
            gameService.respondToNewGameRequest(msg.getPlayerId(), msg.getAccepts());
        } catch (Exception e) {
            sendError(session, "Error responding to new game: " + e.getMessage());
        }
    }

    private void handlePing(Session session, ClientMessage msg) {
        try {
            gameService.ping(msg.getPlayerId());
            ServerMessage response = ServerMessage.gameEvent("pong");
            sendToSession(session, response);
        } catch (Exception e) {
            LOGGER.warning("Error handling ping: " + e.getMessage());
        }
    }

    // Utility methods

    public static void sendToPlayer(String playerId, ServerMessage message) {
        Session session = playerIdToSession.get(playerId);
        if (session != null && session.isOpen()) {
            sendToSession(session, message);
        } else {
            LOGGER.warning("Cannot send message to player " + playerId + ": session not found or closed");
        }
    }

    private static void sendToSession(Session session, ServerMessage message) {
        try {
            String json = gson.toJson(message);
            // Usar envío asíncrono para evitar bloqueos
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            LOGGER.severe("Error sending message to session " + session.getId() + ": " + e.getMessage());
        }
    }

    private void sendError(Session session, String errorMessage) {
        ServerMessage errorMsg = ServerMessage.gameEvent("ERROR: " + errorMessage);
        sendToSession(session, errorMsg);
    }
}
