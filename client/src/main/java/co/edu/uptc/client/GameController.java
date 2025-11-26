package co.edu.uptc.client;

import co.edu.uptc.shared.interfaces.GameService;
import co.edu.uptc.shared.model.*;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Controlador simple que coordina la comunicación RMI y la GUI
 */
public class GameController {
    private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());
    private static final String SERVICE_NAME = "GameService";
    
    private final Registry registry;
    private GameService gameService;
    private GameCallbackImpl callback;
    private GameWindow gameWindow;
    
    private String playerId;
    private String sessionId;
    private boolean isMyTurn = false;
    
    public GameController(Registry registry) {
        this.registry = registry;
    }
    
    /**
     * Inicializa la conexión RMI y callbacks
     */
    public void initialize() throws Exception {
        // Buscar servicio RMI
        gameService = (GameService) registry.lookup(SERVICE_NAME);
        LOGGER.info("Servicio RMI encontrado: " + SERVICE_NAME);
        
        // Crear callback para recibir notificaciones
        callback = new GameCallbackImpl(this);
        LOGGER.info("Callback RMI creado");
        startStatusPolling();
    }
    
    /**
     * Conecta un jugador al juego
     */
    public void connectPlayer(String playerName) {
        // Verificar si ya está conectado
        if (playerId != null && sessionId != null) {
            SwingUtilities.invokeLater(() -> 
                gameWindow.showMessage("Ya estás conectado como: " + playerName));
            return;
        }
        
        try {
            String result = gameService.joinGame(playerName, callback);
            
            if (result.startsWith("SUCCESS:")) {
                // Parsear respuesta: "SUCCESS:playerId:sessionId"
                String[] parts = result.split(":");
                playerId = parts[1];
                sessionId = parts[2];
                
                SwingUtilities.invokeLater(() -> {
                    gameWindow.showMessage("Conectado como: " + playerName);
                    gameWindow.updateStatus("Conectado - Esperando oponente...");
                    
                    // El usuario ahora coloca los barcos manualmente
                    gameWindow.showMessage("🚢 Coloca tus barcos haciendo clic en TU TABLERO");
                });
                
                LOGGER.info("Conectado exitosamente - ID: " + playerId + ", Sesión: " + sessionId);
                
            } else {
                SwingUtilities.invokeLater(() -> 
                    gameWindow.showError("Error conectando: " + result));
            }
            
        } catch (RemoteException e) {
            LOGGER.severe("Error en RMI al conectar: " + e.getMessage());
            SwingUtilities.invokeLater(() -> 
                gameWindow.showError("Error de comunicación: " + e.getMessage()));
        }
    }
    
    /**
     * Coloca un barco en el tablero
     */
    public boolean placeShip(Position start, Position end) {
        if (playerId == null) return false;
        try {
            boolean success = gameService.placeShip(playerId, start, end);
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    gameWindow.showMessage("Barco remoto OK " + start + "-" + end);
                } else {
                    gameWindow.showMessage("Servidor rechazó el barco " + start + "-" + end);
                }
            });
            return success;
        } catch (RemoteException e) {
            LOGGER.severe("Error colocando barco: " + e.getMessage());
            SwingUtilities.invokeLater(() -> gameWindow.showError("Error comunicación: " + e.getMessage()));
            return false;
        }
    }
    
    /**
     * Realiza un ataque
     */
    public void attack(Position target) {
        if (playerId == null || !isMyTurn) {
            SwingUtilities.invokeLater(() -> 
                gameWindow.showMessage("No es tu turno"));
            return;
        }
        
        try {
            String result = gameService.attack(playerId, target);
            
            SwingUtilities.invokeLater(() -> {
                switch (result) {
                    case "HIT":
                        gameWindow.showMessage("¡Impacto en " + target + "!");
                        break;
                    case "MISS":
                        gameWindow.showMessage("Agua en " + target);
                        break;
                    case "SUNK":
                        gameWindow.showMessage("¡Barco hundido en " + target + "!");
                        break;
                    case "SUNK_AND_GAME_OVER":
                        gameWindow.showMessage("¡Último barco hundido! ¡GANASTE!");
                        break;
                    case "VICTORY":
                        gameWindow.showMessage("¡VICTORIA! Has ganado");
                        break;
                    case "NOT_YOUR_TURN":
                        gameWindow.showMessage("No es tu turno");
                        break;
                    default:
                        gameWindow.showMessage("Resultado: " + result);
                        break;
                }
            });
            
        } catch (RemoteException e) {
            LOGGER.severe("Error atacando: " + e.getMessage());
            SwingUtilities.invokeLater(() -> 
                gameWindow.showError("Error comunicación: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene estado actual del juego
     */
    public void refreshGameStatus() {
        if (playerId == null) return;
        try {
            GameStatus status = gameService.getGameStatus(playerId);
            boolean polledTurn = status.isMyTurn();
            GameStatus.GamePhase phase = status.getPhase();

            // Actualizar mensaje general / fase
            handleStatusChange(status);

            // Detectar cambio de turno vía polling (respaldo si falla callback)
            if (polledTurn != this.isMyTurn) {
                LOGGER.info("[POLL] Cambio de turno detectado (was=" + this.isMyTurn + " now=" + polledTurn + ")");
                this.isMyTurn = polledTurn;
                gameWindow.setTurnIndicator(polledTurn);
            }

            // Control de modo ataque según fase y turno
            if (phase == GameStatus.GamePhase.PLAYING) {
                gameWindow.getEnemyBoard().setAttackMode(polledTurn);
            } else {
                gameWindow.getEnemyBoard().setAttackMode(false);
            }
        } catch (RemoteException e) {
            LOGGER.severe("Error obteniendo estado: " + e.getMessage());
        }
    }
    
    // === Métodos para manejar callbacks del servidor ===
    
    public void handleGameEvent(String message) {
        SwingUtilities.invokeLater(() -> gameWindow.showMessage(message));
        
        // Activar modo ataque cuando inicie el juego
        if (message.contains("¡Juego iniciado!")) {
            SwingUtilities.invokeLater(() -> {
                boolean myTurn = message.contains(gameWindow.getPlayerName());
                this.isMyTurn = myTurn; // asegurar estado interno
                gameWindow.setTurnIndicator(myTurn);
                // Solo habilitar rendirse si es mi turno
                gameWindow.updateGameControls(true, false, myTurn);
            });
        }
        
        // Detectar que el juego ha comenzado (cuando ambos jugadores están listos)
        if (message.contains("¡Batalla iniciada!") || message.contains("¡Comienza la batalla!") || 
            message.contains("¡Ambos jugadores listos!")) {
            SwingUtilities.invokeLater(() -> {
                // Solo habilitar rendirse si es mi turno (usar estado actual)
                gameWindow.updateGameControls(true, false, this.isMyTurn);
            });
        }
        
        // Detectar fin del juego
        if (message.contains("¡Juego terminado!") || message.contains("Ganador:") || 
            message.contains("se ha rendido") || message.contains("Victoria!")) {
            SwingUtilities.invokeLater(() -> {
                // Desactivar rendirse, activar nueva partida
                gameWindow.updateGameControls(false, true);
                
                // Mostrar pantalla de resultado si es necesario
                if (message.contains("Victoria!") || message.contains("Has ganado")) {
                    gameWindow.showGameResult(true, "Tú");
                } else if (message.contains("Has perdido") || message.contains("se ha rendido")) {
                    String winner = extractWinner(message);
                    gameWindow.showGameResult(false, winner);
                }
            });
        }
        
        // Detectar inicio de nueva partida
        if (message.contains("¡Nueva partida iniciada!") || message.contains("¡Revancha aceptada!")) {
            SwingUtilities.invokeLater(() -> {
                // Resetear estado interno del controller
                resetControllerState();
                
                // Resetear completamente la interfaz para nueva partida
                gameWindow.resetGameUI();
                // El juego vuelve al estado de colocación de barcos
                gameWindow.updateStatus("Coloca tus barcos para la nueva partida");
            });
        }
        
        // Detectar solicitud de revancha enviada
        if (message.contains("📤 Solicitud de revancha enviada")) {
            SwingUtilities.invokeLater(() -> {
                // Desactivar botón Nueva Partida mientras se espera respuesta
                gameWindow.updateGameControls(false, false);
            });
        }
        
        // Detectar revancha rechazada
        if (message.contains("❌ Tu solicitud de revancha fue rechazada") || 
            message.contains("❌ Revancha rechazada")) {
            SwingUtilities.invokeLater(() -> {
                // Reactivar botón Nueva Partida si la revancha fue rechazada
                gameWindow.updateGameControls(false, true);
            });
        }
        
        // Detectar cambios de turno
        if (message.contains("Turno de:")) {
            String playerName = gameWindow.getPlayerName();
            isMyTurn = message.contains(playerName);
            
            SwingUtilities.invokeLater(() -> {
                gameWindow.setTurnIndicator(isMyTurn);
                // Solo habilitar rendirse cuando sea mi turno
                gameWindow.updateGameControls(true, false, isMyTurn);
            });
        }
    }
    
    /**
     * Extrae el nombre del ganador de un mensaje
     */
    private String extractWinner(String message) {
        // Buscar patrones como "Ganador: nombre" o "nombre ha ganado"
        if (message.contains("Ganador:")) {
            int start = message.indexOf("Ganador:") + 8;
            String remaining = message.substring(start).trim();
            return remaining.split(" ")[0];
        }
        return "Oponente";
    }

    // Manejo estructurado de ataque (nuevo callback)
    public void handleStructuredAttack(String attackerName, int x, int y, String result, boolean yourBoard) {
        SwingUtilities.invokeLater(() -> {
            Position pos = new Position(x, y);
            String you = gameWindow.getPlayerName();
            boolean iAmAttacker = attackerName.equals(you);
            // Pintar resultado en tablero correcto
            if (yourBoard) {
                // Ataque recibido: marcar en mi tablero (myBoard)
                gameWindow.getMyBoard().markAttack(pos, result);
            } else {
                // Confirmación de mi ataque o ataque del oponente mostrado en su propio tablero enemigo
                gameWindow.getEnemyBoard().markAttack(pos, result);
            }
            // Mensaje amigable
            gameWindow.showMessage((iAmAttacker ? "[TU ATAQUE] " : "[ATAQUE ENEMIGO] ") + attackerName + " -> ("+x+","+y+") = " + result);
            // Reglas de turnos clásicas: sólo cambia el turno en MISS (servidor ya debe manejarlo, aquí solo reflejamos si se recibió un turn change fuera de orden)
        });
    }
    
    public void handleStatusChange(GameStatus status) {
        SwingUtilities.invokeLater(() -> {
            gameWindow.updateStatus("Estado: " + status.getPhase());
        });
    }
    
    public void handleTurnChange(boolean isMyTurn, String currentPlayerName) {
        SwingUtilities.invokeLater(() -> {
            this.isMyTurn = isMyTurn;
            gameWindow.setTurnIndicator(isMyTurn);
            
            // Habilitar botón de rendirse solo cuando sea mi turno
            gameWindow.updateGameControls(true, false, isMyTurn);
            
            String message = isMyTurn ? "¡Tu turno!" : "Turno de " + currentPlayerName;
            gameWindow.showMessage(message);
        });
    }
    
    public void handleAttackResult(Position target, String result, String message) {
        SwingUtilities.invokeLater(() -> {
            gameWindow.markEnemyAttack(target, result);
            gameWindow.showMessage(message);
        });
    }

    /**
     * Marca al jugador como listo en el servidor (después de colocar todos los barcos).
     */
    public void markReady() {
        if (playerId == null) return;
        try {
            boolean ok = gameService.setPlayerReady(playerId);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    gameWindow.showMessage("Enviado estado LISTO al servidor");
                } else {
                    gameWindow.showMessage("No se pudo marcar listo (verifica barcos)");
                }
            });
        } catch (RemoteException e) {
            LOGGER.severe("Error marcando listo: " + e.getMessage());
            SwingUtilities.invokeLater(() -> gameWindow.showError("Error comunicación: " + e.getMessage()));
        }
    }

    // === Polling periódico de estado (fallback si algún callback se pierde) ===
    private void startStatusPolling() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1500);
                    if (playerId != null) {
                        GameStatus st = gameService.getGameStatus(playerId);
                        handleStatusChange(st);
                        // Intento de detección de degradación: si regresó a WAITING pese a haber tenido sesión
                        if (st.getPhase() == GameStatus.GamePhase.WAITING && sessionId != null) {
                            SwingUtilities.invokeLater(() -> gameWindow.showMessage("⚠️ Conexión inestable detectada. Reintentando mantener sesión..."));
                        }
                        // Enviar ping ligero para refrescar actividad
                        try { gameService.ping(playerId); } catch (Exception ignored) {}
                    }
                } catch (InterruptedException ie) {
                    return; // terminar
                } catch (Exception e) {
                    // Silencioso para no inundar logs si el juego terminó
                }
            }
        }, "status-poll");
        t.setDaemon(true);
        t.start();
    }
    
    // === Getters y Setters ===
    
    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public boolean isConnected() {
        return playerId != null;
    }
    
    /**
     * Solicita rendirse en la partida actual
     */
    public boolean surrenderGame() {
        if (!isConnected()) {
            LOGGER.warning("No se puede rendir: no conectado");
            return false;
        }
        
        try {
            boolean result = gameService.surrenderGame(playerId);
            LOGGER.info("Rendición procesada: " + result);
            return result;
        } catch (Exception e) {
            LOGGER.severe("Error al rendirse: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Solicita una nueva partida
     */
    public boolean requestNewGame() {
        if (!isConnected()) {
            LOGGER.warning("No se puede solicitar nueva partida: no conectado");
            return false;
        }
        
        try {
            boolean result = gameService.requestNewGame(playerId);
            LOGGER.info("Nueva partida solicitada: " + result);
            return result;
        } catch (Exception e) {
            LOGGER.severe("Error solicitando nueva partida: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene las estadísticas del jugador
     */
    public String getPlayerStats() {
        if (!isConnected()) {
            return "0:0"; // Sin estadísticas si no está conectado
        }
        
        try {
            String stats = gameService.getPlayerStats(playerId);
            LOGGER.info("Estadísticas obtenidas: " + stats);
            return stats;
        } catch (Exception e) {
            LOGGER.warning("Error obteniendo estadísticas: " + e.getMessage());
            return "0:0";
        }
    }
    
    /**
     * Maneja solicitud de nueva partida desde el oponente
     */
    public void handleNewGameRequest(String requesterName) {
        SwingUtilities.invokeLater(() -> {
            gameWindow.showNewGameRequest(requesterName);
        });
    }
    
    /**
     * Resetea el estado interno del controller para nueva partida
     */
    private void resetControllerState() {
        this.isMyTurn = false;
        this.sessionId = null; // Se reasignará cuando se coloquen los barcos
        
        LOGGER.info("Estado del controller reseteado para nueva partida");
    }
    
    /**
     * Responde a una solicitud de nueva partida
     */
    public boolean respondToNewGameRequest(boolean accepts) {
        if (!isConnected()) {
            LOGGER.warning("No se puede responder solicitud: no conectado");
            return false;
        }
        
        try {
            boolean result = gameService.respondToNewGameRequest(playerId, accepts);
            LOGGER.info("Respuesta a nueva partida enviada: " + (accepts ? "Aceptada" : "Rechazada"));
            return result;
        } catch (Exception e) {
            LOGGER.severe("Error respondiendo a nueva partida: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Muestra las estadísticas detalladas del juego actual
     */
    public void showGameStatistics() {
        if (!isConnected()) {
            LOGGER.warning("No se pueden obtener estadísticas: no conectado");
            return;
        }
        
        try {
            // Obtener estadísticas del jugador actual
            GameStats playerStats = gameService.getGameStats(playerId);
            if (playerStats == null) {
                LOGGER.warning("No se pudieron obtener las estadísticas del jugador");
                return;
            }
            
            // Obtener estadísticas del oponente
            GameStats opponentStats = gameService.getOpponentStats(playerId);
            if (opponentStats == null) {
                // Crear estadísticas vacías si no se encuentran
                opponentStats = new GameStats("Oponente");
                LOGGER.warning("No se pudieron obtener las estadísticas del oponente");
            }
            
            // Determinar nombres de jugadores
            String playerName = playerStats.getPlayerName();
            String opponentName = opponentStats.getPlayerName();
            
            // Mostrar diálogo de estadísticas
            GameStatsPanel.showStatsDialog(gameWindow, playerStats, opponentStats, 
                                         playerName, opponentName);
            
            LOGGER.info("Estadísticas del juego mostradas");
            
        } catch (Exception e) {
            LOGGER.severe("Error mostrando estadísticas: " + e.getMessage());
            JOptionPane.showMessageDialog(gameWindow, 
                "Error obteniendo estadísticas del juego: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
