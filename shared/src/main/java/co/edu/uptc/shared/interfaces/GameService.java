package co.edu.uptc.shared.interfaces;

import co.edu.uptc.shared.model.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI principal del servicio de Batalla Naval
 * Demuestra comunicación distribuida cliente-servidor
 */
public interface GameService extends Remote {
    
    /**
     * Un jugador se une al juego distribuido
     * @param playerName Nombre del jugador
     * @param callback Callback RMI para notificaciones bidireccionales
     * @return ID único del jugador en el sistema distribuido
     * @throws RemoteException Error en comunicación RMI
     */
    String joinGame(String playerName, GameCallback callback) throws RemoteException;
    
    /**
     * Coloca un barco en el tablero - Invocación de método remoto
     * @param playerId ID del jugador
     * @param start Posición inicial del barco
     * @param end Posición final del barco
     * @return true si se colocó exitosamente
     * @throws RemoteException Error en comunicación RMI
     */
    boolean placeShip(String playerId, Position start, Position end) throws RemoteException;
    
    /**
     * Realiza un ataque - Coordinación distribuida entre jugadores
     * @param playerId ID del jugador que ataca
     * @param target Posición del ataque
     * @return Resultado del ataque (serializado)
     * @throws RemoteException Error en comunicación RMI
     */
    String attack(String playerId, Position target) throws RemoteException;
    
    /**
     * Obtiene estado del juego - Sincronización distribuida
     * @param playerId ID del jugador
     * @return Estado actual del juego distribuido
     * @throws RemoteException Error en comunicación RMI
     */
    GameStatus getGameStatus(String playerId) throws RemoteException;
    
    /**
     * Marca jugador como listo - Coordinación de inicio distribuido
     * @param playerId ID del jugador
     * @return true si el juego puede comenzar
     * @throws RemoteException Error en comunicación RMI
     */
    boolean setPlayerReady(String playerId) throws RemoteException;
    
    /**
     * Desconecta jugador del sistema distribuido
     * @param playerId ID del jugador
     * @throws RemoteException Error en comunicación RMI
     */
    void disconnectPlayer(String playerId) throws RemoteException;
    
    /**
     * Ping para verificar conectividad - Heartbeat distribuido
     * @param playerId ID del jugador
     * @return true si el jugador está conectado
     * @throws RemoteException Error en comunicación RMI
     */
    boolean ping(String playerId) throws RemoteException;
    
    /**
     * El jugador se rinde en la partida actual
     * @param playerId ID del jugador que se rinde
     * @return true si la rendición fue procesada exitosamente
     * @throws RemoteException Error en comunicación RMI
     */
    boolean surrenderGame(String playerId) throws RemoteException;
    
    /**
     * Solicita iniciar una nueva partida entre los mismos jugadores
     * @param playerId ID del jugador que solicita nueva partida
     * @return true si la solicitud fue procesada exitosamente
     * @throws RemoteException Error en comunicación RMI
     */
    boolean requestNewGame(String playerId) throws RemoteException;
    
    /**
     * Obtiene las estadísticas del jugador (victorias/derrotas)
     * @param playerId ID del jugador
     * @return String con estadísticas en formato "wins:losses"
     * @throws RemoteException Error en comunicación RMI
     */
    String getPlayerStats(String playerId) throws RemoteException;
    
    /**
     * Responde a una solicitud de nueva partida
     * @param playerId ID del jugador que responde
     * @param accepts true si acepta la revancha, false si la rechaza
     * @return true si la respuesta fue procesada exitosamente
     * @throws RemoteException Error en comunicación RMI
     */
    boolean respondToNewGameRequest(String playerId, boolean accepts) throws RemoteException;
    
    /**
     * Obtiene las estadísticas detalladas del juego actual
     * @param playerId ID del jugador
     * @return GameStats con estadísticas detalladas del juego actual
     * @throws RemoteException Error en comunicación RMI
     */
    GameStats getGameStats(String playerId) throws RemoteException;
    
    /**
     * Obtiene las estadísticas del oponente
     * @param playerId ID del jugador
     * @return GameStats del oponente o null si no se encuentra
     * @throws RemoteException Error en comunicación RMI
     */
    GameStats getOpponentStats(String playerId) throws RemoteException;
}
