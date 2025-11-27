package co.edu.uptc.client;

import co.edu.uptc.shared.model.*;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * Controlador que coordina la comunicación WebSocket y la GUI
 */
public class GameController {
    private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());

    private GameWebSocketClient wsClient;
    private GameWindow gameWindow;
    private String websocketUrl;

    private String playerId;
    private boolean isMyTurn = false;

    public GameController(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    /**
     * Inicializa la conexión
     */
    public void initialize() throws Exception {
        // Nada que inicializar por ahora, la conexión se hace al conectar jugador
    }

    /**
     * Conecta un jugador al juego via WebSocket
     */
    public void connectPlayer(String playerName) throws Exception {
        if (wsClient != null && wsClient.isConnected()) {
            SwingUtilities.invokeLater(() -> gameWindow.showMessage("Ya estás conectado como: " + playerName));
            return;
        }

        wsClient = new GameWebSocketClient(this);
        wsClient.connect(websocketUrl);

        // Esperar un momento para asegurar conexión antes de enviar mensaje
        Thread.sleep(500);
        wsClient.sendJoin(playerName);

        SwingUtilities.invokeLater(() -> {
            gameWindow.updateStatus("Conectando...");
        });
    }

    // === Métodos llamados por GameWebSocketClient (Callbacks) ===

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
        SwingUtilities.invokeLater(() -> {
            gameWindow.showMessage("Conectado al servidor");
            gameWindow.updateStatus("Esperando oponente...");
            gameWindow.showMessage("🚢 Coloca tus barcos haciendo clic en TU TABLERO");
        });
    }

    public void handleGameEvent(String message) {
        SwingUtilities.invokeLater(() -> gameWindow.showMessage(message));

        if (message.contains("¡Juego iniciado!") || message.contains("¡Batalla iniciada!")) {
            // El juego ha comenzado
        }
    }

    public void handleStatusUpdate(String status) {
        SwingUtilities.invokeLater(() -> gameWindow.updateStatus(status));
    }

    public void handleTurnChange(boolean isMyTurn, String currentPlayerName) {
        this.isMyTurn = isMyTurn;
        SwingUtilities.invokeLater(() -> {
            gameWindow.setTurnIndicator(isMyTurn);
            gameWindow.updateGameControls(true, false, isMyTurn);

            String msg = isMyTurn ? "¡Tu turno! Ataca al enemigo." : "Turno de " + currentPlayerName;
            gameWindow.showMessage(msg);
        });
    }

    public void handleStructuredAttack(String attackerName, int x, int y, String result, boolean yourBoard) {
        SwingUtilities.invokeLater(() -> {
            Position pos = new Position(x, y);

            if (yourBoard) {
                // Ataque recibido en mi tablero
                gameWindow.getMyBoard().markAttack(pos, result);
            } else {
                // Mi ataque (o ataque visto en tablero enemigo)
                gameWindow.getEnemyBoard().markAttack(pos, result);
            }

            String msg = (yourBoard ? "[DEFENSA] " : "[ATAQUE] ") + attackerName + " disparó a (" + x + "," + y + "): "
                    + result;
            gameWindow.showMessage(msg);
        });
    }

    public void handleGameEnded(String winnerName) {
        SwingUtilities.invokeLater(() -> {
            boolean won = false;
            if (gameWindow.getPlayerName() != null) {
                won = gameWindow.getPlayerName().equals(winnerName);
            }
            gameWindow.showGameResult(won, winnerName);
            gameWindow.updateGameControls(false, true);
        });
    }

    public void handleOpponentDisconnected() {
        SwingUtilities.invokeLater(() -> {
            gameWindow.showError("El oponente se ha desconectado.");
            gameWindow.updateStatus("Oponente desconectado");
            gameWindow.updateGameControls(false, true); // Permitir nueva partida si se implementa lógica de reset
        });
    }

    public void handleNewGameRequest(String requesterName) {
        SwingUtilities.invokeLater(() -> {
            gameWindow.showNewGameRequest(requesterName);
        });
    }

    // === Acciones del Jugador (Envío de mensajes) ===

    public boolean placeShip(Position start, Position end) {
        if (wsClient == null)
            return false;
        wsClient.sendPlaceShip(start, end);
        return true; // Asumimos éxito, el servidor validará y enviará error si falla
    }

    public void attack(Position target) {
        if (!isMyTurn) {
            gameWindow.showMessage("No es tu turno");
            return;
        }
        wsClient.sendAttack(target);
    }

    public void markReady() {
        if (wsClient != null) {
            wsClient.sendReady();
            gameWindow.showMessage("Enviado estado LISTO al servidor");
        }
    }

    public boolean surrenderGame() {
        if (wsClient != null) {
            wsClient.sendSurrender();
            return true;
        }
        return false;
    }

    public boolean requestNewGame() {
        if (wsClient != null) {
            wsClient.sendRequestNewGame();
            return true;
        }
        return false;
    }

    public boolean respondToNewGameRequest(boolean accepts) {
        if (wsClient != null) {
            wsClient.sendRespondNewGame(accepts);
            return true;
        }
        return false;
    }

    public String getPlayerStats() {
        // En WebSocket esto debería ser asíncrono, pero por simplicidad retornamos
        // placeholder
        // Podríamos enviar un mensaje requestStats y manejar la respuesta
        return "0:0";
    }

    // === Getters y Setters ===

    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public boolean isConnected() {
        return wsClient != null && wsClient.isConnected();
    }
}
