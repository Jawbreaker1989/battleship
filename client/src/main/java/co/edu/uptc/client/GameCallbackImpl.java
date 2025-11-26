package co.edu.uptc.client;

import co.edu.uptc.shared.interfaces.GameCallback;

import javax.swing.SwingUtilities;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

/**
 * Implementación  de callbacks RMI para recibir notificaciones del servidor
 */
public class GameCallbackImpl extends UnicastRemoteObject implements GameCallback {
    private static final Logger LOGGER = Logger.getLogger(GameCallbackImpl.class.getName());
    
    private final GameController controller;
    
    public GameCallbackImpl(GameController controller) throws RemoteException {
        super(0); // 0 = exporta en puerto dinámico disponible
        this.controller = controller;
        System.out.println("[CLIENTE] Callback exportado (puerto dinámico asignado)");
    }
    
    @Override
    public void onGameEvent(String message) throws RemoteException {
        LOGGER.info("[CALLBACK] onGameEvent: " + message);
        controller.handleGameEvent(message);
    }
    
    @Override
    public void onPlayerJoined(String playerName) throws RemoteException {
        LOGGER.info("[CALLBACK] onPlayerJoined: " + playerName);
        controller.handleGameEvent("Jugador conectado: " + playerName);
    }
    
    @Override
    public void onTurnChanged(boolean isMyTurn, String currentPlayerName) throws RemoteException {
        LOGGER.info("[CALLBACK] onTurnChanged isMyTurn=" + isMyTurn + " current=" + currentPlayerName);
        controller.handleTurnChange(isMyTurn, currentPlayerName);
    }
    
    @Override
    public void onGameEnded(String winner) throws RemoteException {
        LOGGER.info("[CALLBACK] onGameEnded winner=" + winner);
        controller.handleGameEvent("¡Juego terminado! Ganador: " + winner);
        
        // Mostrar estadísticas detalladas después de un breve retraso
        SwingUtilities.invokeLater(() -> {
            try {
                // Pequeño retraso para asegurar que la UI se haya actualizado
                Thread.sleep(1000);
                controller.showGameStatistics();
            } catch (Exception e) {
                LOGGER.warning("Error mostrando estadísticas: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void onOpponentDisconnected() throws RemoteException {
        LOGGER.info("[CALLBACK] onOpponentDisconnected");
        controller.handleGameEvent("Oponente desconectado");
    }

    @Override
    public void onAttackEvent(String attackerName, int targetX, int targetY, String result, boolean yourBoard) throws RemoteException {
        LOGGER.info("[CALLBACK] onAttackEvent " + attackerName + " -> ("+targetX+","+targetY+") = " + result + " yourBoard=" + yourBoard);
        controller.handleStructuredAttack(attackerName, targetX, targetY, result, yourBoard);
    }
    
    @Override
    public void onNewGameRequest(String requesterName) throws RemoteException {
        LOGGER.info("[CALLBACK] onNewGameRequest from " + requesterName);
        controller.handleNewGameRequest(requesterName);
    }
}
