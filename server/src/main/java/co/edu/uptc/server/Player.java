package co.edu.uptc.server;

import javax.websocket.Session;
import co.edu.uptc.shared.model.Board;
import co.edu.uptc.shared.model.GameStats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Player class adapted for WebSocket
 * Uses Session instead of RMI GameCallback
 */
public class Player {
    private final String id;
    private final String name;
    private final Session session;
    private final Board board;
    private boolean ready;
    private Long readyTimestamp;
    private volatile long lastActivity;
    private final List<Integer> remainingShips;
    private int wins;
    private int losses;

    private GameStats currentGameStats;

    public Player(String id, String name, Session session) {
        this.id = id;
        this.name = name;
        this.session = session;
        this.board = new Board();
        this.ready = false;
        this.remainingShips = new ArrayList<>(Arrays.asList(5, 4, 3, 3, 2));
        this.lastActivity = System.currentTimeMillis();
        this.wins = 0;
        this.losses = 0;
        this.currentGameStats = new GameStats(name);
    }

    // Getters simples
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Session getSession() {
        return session;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isReady() {
        return ready;
    }

    public Long getReadyTimestamp() {
        return readyTimestamp;
    }

    public List<Integer> getRemainingShips() {
        return remainingShips;
    }

    public boolean allShipsPlaced() {
        return remainingShips.isEmpty();
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void touchActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    // Setters simples
    public void setReady(boolean ready) {
        this.ready = ready;
        if (ready && readyTimestamp == null) {
            readyTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Verifica si el jugador todavía puede colocar un barco de cierto tamaño
     */
    public boolean hasShipAvailable(int size) {
        return remainingShips.contains(size);
    }

    /**
     * Consume (elimina) el barco del listado restante. Retorna true si existía.
     */
    public boolean consumeShip(int size) {
        return remainingShips.remove((Integer) size);
    }

    /**
     * Devuelve un barco al pool (en caso de fallo al colocar)
     */
    public void returnShip(int size) {
        remainingShips.add(size);
    }

    // Getters y setters para estadísticas
    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getTotalGames() {
        return wins + losses;
    }

    public double getWinRate() {
        return getTotalGames() > 0 ? (double) wins / getTotalGames() * 100 : 0.0;
    }

    public void addWin() {
        wins++;
    }

    public void addLoss() {
        losses++;
    }

    /**
     * Obtiene estadísticas en formato serializado
     */
    public String getStatsString() {
        return wins + ":" + losses;
    }

    /**
     * Resetea el jugador para una nueva partida (mantiene estadísticas)
     */
    public void resetForNewGame() {
        this.board.reset();
        this.ready = false;
        this.readyTimestamp = null;
        this.remainingShips.clear();
        this.remainingShips.addAll(Arrays.asList(5, 4, 3, 3, 2));
        this.lastActivity = System.currentTimeMillis();
        // Resetear estadísticas para nueva partida
        this.currentGameStats = new GameStats(name);
    }

    /**
     * Registra un disparo realizado por este jugador
     */
    public void recordShotMade(boolean hit) {
        currentGameStats.recordShot(hit);
    }

    /**
     * Registra cuando este jugador hunde un barco del oponente
     */
    public void recordShipDestroyed(int shipSize) {
        currentGameStats.recordShipDestroyed(shipSize);
    }

    /**
     * Marca el fin del juego para este jugador
     */
    public void endGame(boolean won) {
        currentGameStats.endGame(won);
        if (won) {
            addWin();
        } else {
            addLoss();
        }
    }

    /**
     * Obtiene las estadísticas de la partida actual
     */
    public GameStats getCurrentGameStats() {
        return currentGameStats;
    }

    @Override
    public String toString() {
        return "Player{" + name + " (" + id + "), ready=" + ready +
                ", remaining=" + remainingShips + ", stats=" + wins + "-" + losses + "}";
    }
}
