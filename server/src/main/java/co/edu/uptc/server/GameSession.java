package co.edu.uptc.server;

import co.edu.uptc.shared.model.*;
import co.edu.uptc.shared.messages.ServerMessage;
import java.util.logging.Logger;

/**
 * Sesión simple de juego entre 2 jugadores
 * Coordina la partida distribuida con lógica extendida (ready y primer turno)
 */
public class GameSession {
    private static final Logger LOGGER = Logger.getLogger(GameSession.class.getName());

    private final String sessionId;
    private Player player1;
    private Player player2;
    private String currentTurn; // ID del jugador actual
    private GameStatus.GamePhase phase;
    private String firstReadyPlayerId; // Quién presionó listo primero

    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        this.phase = GameStatus.GamePhase.WAITING;
        this.firstReadyPlayerId = null;
    }

    // Añade un jugador a la sesión
    public synchronized boolean addPlayer(Player player) {
        if (player1 == null) {
            player1 = player;
            notifyPlayer(player, "Esperando segundo jugador...");
            sendStatusUpdate(player, "Esperando oponente...");
            return true;
        } else if (player2 == null) {
            player2 = player;
            phase = GameStatus.GamePhase.PLACING_SHIPS;
            
            // Notificar a ambos jugadores que pueden comenzar
            notifyPlayer(player1, "¡Jugador conectado! " + player2.getName() + " se ha unido. ¡Coloca tus barcos!");
            notifyPlayer(player2, "¡Conectado contra: " + player1.getName() + "! ¡Coloca tus barcos!");
            notifyBothPlayers("¡Fase de colocación de barcos iniciada!");

            // Send status update to both players - ACTUALIZADO PARA AMBOS
            sendStatusUpdate(player1, "Oponente conectado: " + player2.getName() + ". Coloca tus barcos");
            sendStatusUpdate(player2, "Jugando contra: " + player1.getName() + ". Coloca tus barcos");
            return true;
        }
        return false; // Sesión llena
    }

    // Coloca un barco para un jugador con validación de tamaño disponible
    public synchronized boolean placeShip(String playerId, Position start, Position end) {
        Player player = getPlayer(playerId);
        if (player == null || phase != GameStatus.GamePhase.PLACING_SHIPS) {
            return false;
        }
        try {
            int size;
            if (start.getX() == end.getX()) {
                size = Math.abs(start.getY() - end.getY()) + 1;
            } else if (start.getY() == end.getY()) {
                size = Math.abs(start.getX() - end.getX()) + 1;
            } else {
                notifyPlayer(player, "El barco debe ser horizontal o vertical");
                return false;
            }
            if (!player.hasShipAvailable(size)) {
                notifyPlayer(player,
                        "No tienes barco de tamaño " + size + " disponible. Restantes: " + player.getRemainingShips());
                return false;
            }
            player.consumeShip(size);
            boolean placed = player.getBoard().placeShip(start, end);
            if (!placed) {
                player.returnShip(size);
                notifyPlayer(player, "Posición inválida o superposición");
                return false;
            }
            notifyPlayer(player, "Barco de tamaño " + size + " colocado. Restantes: " + player.getRemainingShips());
            return true;
        } catch (Exception e) {
            LOGGER.warning("Error colocando barco: " + e.getMessage());
            return false;
        }
    }

    // Marca jugador como listo; inicia juego si ambos listos
    public synchronized boolean markPlayerReady(String playerId) {
        Player p = getPlayer(playerId);
        if (p == null || phase != GameStatus.GamePhase.PLACING_SHIPS)
            return false;
        if (!p.allShipsPlaced()) {
            notifyPlayer(p, "Aún no has colocado todos tus barcos");
            return false;
        }
        p.setReady(true);
        if (firstReadyPlayerId == null)
            firstReadyPlayerId = playerId;
        notifyPlayer(p, "Marcado como listo. Esperando oponente...");
        if (bothPlayersReady())
            startGame();
        return true;
    }

    // Realiza un ataque
    public synchronized Board.AttackResult attack(String playerId, Position target) {
        if (phase != GameStatus.GamePhase.PLAYING || !playerId.equals(currentTurn)) {
            return null; // No es tu turno
        }
        Player attacker = getPlayer(playerId);
        Player defender = getOpponent(playerId);
        if (attacker == null || defender == null) {
            return null;
        }

        // Usar método extendido para obtener información del barco hundido
        Board.ExtendedAttackResult extendedResult = defender.getBoard().receiveAttackExtended(target);
        Board.AttackResult result = extendedResult.getResult();

        // Si ya fue atacada, rechazar el intento
        if (result == Board.AttackResult.ALREADY_ATTACKED) {
            notifyPlayer(attacker, "⚠️ Ya atacaste esa posición (" + target + ")");
            return result;
        }

        // Trackear estadísticas del atacante
        attacker.recordShotMade(result != Board.AttackResult.ALREADY_ATTACKED);

        // Si se hundió un barco, trackear estadísticas
        if ((result == Board.AttackResult.SUNK || result == Board.AttackResult.SUNK_AND_GAME_OVER)
                && extendedResult.getSunkShip() != null) {
            attacker.recordShipDestroyed(extendedResult.getSunkShip().getSize());
        }

        // Notificaciones textuales básicas
        notifyPlayer(attacker, "💥 Atacaste " + target + ": " + result.getDescription());
        notifyPlayer(defender, "🎯 " + attacker.getName() + " atacó " + target + ": " + result.getDescription());
        // Callback estructurado para pintar en clientes
        sendAttackStructured(attacker, defender, target, result);

        if (result == Board.AttackResult.SUNK_AND_GAME_OVER) {
            phase = GameStatus.GamePhase.FINISHED;
            // Finalizar estadísticas para ambos jugadores
            attacker.endGame(true); // ganador
            defender.endGame(false); // perdedor

            // Notificar fin del juego a ambos jugadores con callback
            notifyGameEnded(attacker.getName());
            notifyBothPlayers("¡" + attacker.getName() + " GANA!");
            return result;
        }
        
        // Reglas clásicas: Sólo cambia turno con MISS.
        if (result == Board.AttackResult.MISS) {
            switchTurn();
        } else {
            // Si fue HIT o SUNK pero el juego no terminó, el atacante continúa
            notifyPlayer(attacker, "✓ ¡Golpe acertado! Tu turno continúa...");
        }
        
        return result;
    }

    // Obtiene el estado del juego para un jugador
    public GameStatus getGameStatus(String playerId) {
        Player requestingPlayer = getPlayer(playerId);
        int playersConnected = (player1 != null ? 1 : 0) + (player2 != null ? 1 : 0);
        switch (phase) {
            case WAITING:
                return GameStatus.waiting(playersConnected);
            case PLACING_SHIPS:
                return GameStatus.placingShips(playersConnected);
            case PLAYING:
                Player current = getPlayer(currentTurn);
                String currentName = current != null ? current.getName() : "";
                boolean isMyTurn = requestingPlayer != null && currentTurn.equals(requestingPlayer.getId());
                return GameStatus.playing(currentName, isMyTurn);
            case FINISHED:
                String winner = "Juego terminado"; // Mensaje genérico (ya se notificó)
                return GameStatus.finished(winner);
            default:
                return GameStatus.waiting(playersConnected);
        }
    }

    private Player getPlayer(String playerId) {
        if (player1 != null && player1.getId().equals(playerId))
            return player1;
        if (player2 != null && player2.getId().equals(playerId))
            return player2;
        return null;
    }

    private Player getOpponent(String playerId) {
        if (player1 != null && player1.getId().equals(playerId))
            return player2;
        if (player2 != null && player2.getId().equals(playerId))
            return player1;
        return null;
    }

    private void startGame() {
        phase = GameStatus.GamePhase.PLAYING;
        if (firstReadyPlayerId != null) {
            currentTurn = firstReadyPlayerId;
        } else {
            currentTurn = player1 != null ? player1.getId() : (player2 != null ? player2.getId() : null);
        }
        Player starter = getPlayer(currentTurn);
        Player waiter = getOpponent(currentTurn);

        // Mensajes claros de inicio del juego
        String startMessage = "¡JUEGO INICIADO! " + (starter != null ? starter.getName() : "?") + " ataca primero.";
        notifyBothPlayers(startMessage);

        // Send personalized status updates to both players
        if (starter != null) {
            sendStatusUpdate(starter, "¡ES TU TURNO! Ataca al enemigo en su tablero");
        }
        if (waiter != null) {
            sendStatusUpdate(waiter, "🛡️ Turno del oponente (" + (starter != null ? starter.getName() : "?") + ")");
        }
        
        LOGGER.info("Game started in session " + sessionId + ". Current turn: " + currentTurn);
    }

    private void switchTurn() {
        String previousTurn = currentTurn;
        currentTurn = currentTurn.equals(player1.getId()) ? player2.getId() : player1.getId();
        
        Player current = getPlayer(currentTurn);
        Player previous = getPlayer(previousTurn);
        
        // Notificación de cambio de turno
        notifyBothPlayers("🔄 Cambio de turno: " + current.getName() + " ahora ataca");
        
        // Enviar statusUpdate personalizado a cada jugador
        if (current != null) {
            sendStatusUpdate(current, "¡ES TU TURNO! Ataca al enemigo");
        }
        if (previous != null) {
            sendStatusUpdate(previous, "⏳ Turno del oponente (" + current.getName() + ")");
        }
        
        LOGGER.info("Turn switched from " + previous.getName() + " to " + current.getName());
    }

    private void sendAttackStructured(Player attacker, Player defender, Position pos, Board.AttackResult result) {
        try {
            // Para el atacante: yourBoard=false (marcar en tablero enemigo)
            ServerMessage msg = ServerMessage.attackEvent(attacker.getName(), pos.getX(), pos.getY(), result.name(),
                    false);
            GameWebSocketServer.sendToPlayer(attacker.getId(), msg);
        } catch (Exception e) {
            LOGGER.warning("No se pudo enviar ataque a atacante: " + e.getMessage());
        }
        try {
            // Para el defensor: yourBoard=true (marcar en su propio tablero)
            ServerMessage msg = ServerMessage.attackEvent(attacker.getName(), pos.getX(), pos.getY(), result.name(),
                    true);
            GameWebSocketServer.sendToPlayer(defender.getId(), msg);
        } catch (Exception e) {
            LOGGER.warning("No se pudo enviar ataque a defensor: " + e.getMessage());
        }
    }

    private boolean bothPlayersReady() {
        return player1 != null && player2 != null && player1.isReady() && player2.isReady();
    }

    private void notifyPlayer(Player player, String message) {
        try {
            ServerMessage msg = ServerMessage.gameEvent(message);
            GameWebSocketServer.sendToPlayer(player.getId(), msg);
        } catch (Exception e) {
            LOGGER.warning("Error notificando a " + player.getName() + ": " + e.getMessage());
        }
    }

    private void notifyBothPlayers(String message) {
        if (player1 != null)
            notifyPlayer(player1, message);
        if (player2 != null)
            notifyPlayer(player2, message);
    }

    private void sendStatusUpdate(Player player, String status) {
        try {
            ServerMessage msg = ServerMessage.statusUpdate(status);
            GameWebSocketServer.sendToPlayer(player.getId(), msg);
        } catch (Exception e) {
            LOGGER.warning("Error sending status to " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Notifica el fin del juego a ambos jugadores
     */
    private void notifyGameEnded(String winnerName) {
        try {
            ServerMessage msg = ServerMessage.gameEnded(winnerName);
            if (player1 != null) {
                GameWebSocketServer.sendToPlayer(player1.getId(), msg);
            }
            if (player2 != null) {
                GameWebSocketServer.sendToPlayer(player2.getId(), msg);
            }
            LOGGER.info("Notificación de fin de juego enviada. Ganador: " + winnerName);
        } catch (Exception e) {
            LOGGER.warning("Error notificando fin de juego: " + e.getMessage());
        }
    }

    // Getters simples
    public String getSessionId() {
        return sessionId;
    }

    public boolean isFull() {
        return player1 != null && player2 != null;
    }

    public boolean isEmpty() {
        return player1 == null && player2 == null;
    }

    /**
     * Obtiene el ID del oponente de un jugador dado
     */
    public String getOpponentId(String playerId) {
        if (player1 != null && player1.getId().equals(playerId)) {
            return player2 != null ? player2.getId() : null;
        } else if (player2 != null && player2.getId().equals(playerId)) {
            return player1 != null ? player1.getId() : null;
        }
        return null;
    }

    /**
     * Obtiene las estadísticas del oponente de un jugador dado
     */
    public GameStats getOpponentStats(String playerId) {
        Player opponent = getOpponent(playerId);
        return opponent != null ? opponent.getCurrentGameStats() : null;
    }

    /**
     * Resetea la sesión para una nueva partida manteniendo los jugadores
     */
    public synchronized void resetForNewGame() {
        LOGGER.info("Reseteando sesión " + sessionId + " para nueva partida");

        // Resetear completamente el estado de la sesión
        this.phase = GameStatus.GamePhase.PLACING_SHIPS;
        this.currentTurn = null;
        this.firstReadyPlayerId = null;

        // Verificar que ambos jugadores existen antes del reset
        if (player1 != null && player2 != null) {
            // Resetear jugadores (se hace desde GameServiceImpl ahora)

            // Notificar el inicio de nueva partida
            try {
                notifyPlayer(player1, "¡Nueva partida iniciada! Coloca tus barcos.");
                notifyPlayer(player2, "¡Nueva partida iniciada! Coloca tus barcos.");
                LOGGER.info("Notificaciones enviadas para nueva partida en sesión " + sessionId);
            } catch (Exception e) {
                LOGGER.warning("Error enviando notificaciones de nueva partida: " + e.getMessage());
            }
        }

        LOGGER.info("Sesión " + sessionId + " completamente reseteada para nueva partida");
    }
}
