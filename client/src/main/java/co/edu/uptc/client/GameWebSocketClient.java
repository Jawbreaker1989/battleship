package co.edu.uptc.client;

import co.edu.uptc.shared.messages.ClientMessage;
import co.edu.uptc.shared.messages.ServerMessage;
import com.google.gson.Gson;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * WebSocket client endpoint for Battleship game
 * Handles bidirectional communication with server
 */
@ClientEndpoint
public class GameWebSocketClient {
    private static final Logger LOGGER = Logger.getLogger(GameWebSocketClient.class.getName());
    private static final Gson gson = new Gson();

    private Session session;
    private GameController controller;
    private String playerId;
    private String sessionId;

    public GameWebSocketClient(GameController controller) {
        this.controller = controller;
    }

    public void connect(String serverUrl) throws Exception {
        LOGGER.info("Connecting to WebSocket server: " + serverUrl);

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        // Configurar timeouts optimizados para Azure
        container.setDefaultMaxSessionIdleTimeout(120000L); // 2 minutos
        container.setDefaultMaxTextMessageBufferSize(8192);

        this.session = container.connectToServer(this, new URI(serverUrl));

        LOGGER.info("Connected to WebSocket server");
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("WebSocket connection opened: " + session.getId());
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            LOGGER.info("Received message: " + message);

            ServerMessage serverMsg = gson.fromJson(message, ServerMessage.class);
            String event = serverMsg.getEvent();

            if (event == null) {
                LOGGER.warning("Received message with no event type");
                return;
            }

            switch (event) {
                case "joined":
                    handleJoined(serverMsg);
                    break;

                case "playerJoined":
                    handlePlayerJoined(serverMsg);
                    break;

                case "turnChanged":
                    handleTurnChanged(serverMsg);
                    break;

                case "attackResult":
                    handleAttackResult(serverMsg);
                    break;

                case "gameEnded":
                    handleGameEnded(serverMsg);
                    break;

                case "opponentDisconnected":
                    handleOpponentDisconnected();
                    break;

                case "attackEvent":
                    handleAttackEvent(serverMsg);
                    break;

                case "newGameRequest":
                    handleNewGameRequest(serverMsg);
                    break;

                case "gameEvent":
                    handleGameEvent(serverMsg);
                    break;

                case "statusUpdate":
                    handleStatusUpdate(serverMsg);
                    break;

                default:
                    LOGGER.warning("Unknown event type: " + event);
            }

        } catch (Exception e) {
            LOGGER.severe("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.info("WebSocket connection closed: " + reason);
        this.session = null;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.severe("WebSocket error: " + throwable.getMessage());
        throwable.printStackTrace();
    }

    // Event handlers

    private void handleJoined(ServerMessage msg) {
        this.playerId = msg.getPlayerId();
        this.sessionId = msg.getSessionId();
        LOGGER.info("Joined game with player ID: " + playerId);
        controller.setPlayerId(playerId);
    }

    private void handlePlayerJoined(ServerMessage msg) {
        controller.handleGameEvent("Jugador conectado: " + msg.getPlayerName());
    }

    private void handleTurnChanged(ServerMessage msg) {
        controller.handleTurnChange(msg.getIsMyTurn(), msg.getCurrentPlayerName());
    }

    private void handleAttackResult(ServerMessage msg) {
        // Attack result is typically shown via attackEvent
        LOGGER.info("Attack result: " + msg.getResult());
    }

    private void handleGameEnded(ServerMessage msg) {
        controller.handleGameEvent("¡Juego terminado! Ganador: " + msg.getWinner());
        controller.handleGameEnded(msg.getWinner());
    }

    private void handleOpponentDisconnected() {
        controller.handleGameEvent("Oponente desconectado");
        controller.handleOpponentDisconnected();
    }

    private void handleAttackEvent(ServerMessage msg) {
        controller.handleStructuredAttack(
                msg.getAttackerName(),
                msg.getTargetX(),
                msg.getTargetY(),
                msg.getResult(),
                msg.getYourBoard());
    }

    private void handleNewGameRequest(ServerMessage msg) {
        controller.handleNewGameRequest(msg.getRequesterName());
    }

    private void handleGameEvent(ServerMessage msg) {
        controller.handleGameEvent(msg.getMessage());
    }

    private void handleStatusUpdate(ServerMessage msg) {
        controller.handleStatusUpdate(msg.getStatus());
    }

    // Send methods

    public void sendJoin(String playerName) {
        sendMessage(ClientMessage.join(playerName));
    }

    public void sendPlaceShip(co.edu.uptc.shared.model.Position start, co.edu.uptc.shared.model.Position end) {
        sendMessage(ClientMessage.placeShip(playerId, start, end));
    }

    public void sendAttack(co.edu.uptc.shared.model.Position target) {
        sendMessage(ClientMessage.attack(playerId, target));
    }

    public void sendReady() {
        sendMessage(ClientMessage.ready(playerId));
    }

    public void sendSurrender() {
        sendMessage(ClientMessage.surrender(playerId));
    }

    public void sendRequestNewGame() {
        sendMessage(ClientMessage.requestNewGame(playerId));
    }

    public void sendRespondNewGame(boolean accepts) {
        sendMessage(ClientMessage.respondNewGame(playerId, accepts));
    }

    public void sendPing() {
        sendMessage(ClientMessage.ping(playerId));
    }

    private void sendMessage(ClientMessage message) {
        if (session == null || !session.isOpen()) {
            LOGGER.warning("Cannot send message: session not open");
            return;
        }

        try {
            String json = gson.toJson(message);
            // Usar envío asíncrono para evitar bloqueos
            session.getAsyncRemote().sendText(json);
            LOGGER.info("Sent message: " + message.getAction());
        } catch (Exception e) {
            LOGGER.severe("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    public void disconnect() {
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                LOGGER.warning("Error closing session: " + e.getMessage());
            }
        }
    }

    public String getPlayerId() {
        return playerId;
    }
}
