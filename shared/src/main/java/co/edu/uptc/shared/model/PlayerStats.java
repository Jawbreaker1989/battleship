package co.edu.uptc.shared.model;

import java.io.Serializable;

/**
 * Estadísticas de un jugador en el sistema de Batalla Naval
 * Mantiene registro de victorias, derrotas y otras métricas
 */
public class PlayerStats implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String playerName;
    private final int wins;
    private final int losses;
    private final int gamesPlayed;
    private final double winRate;
    
    public PlayerStats(String playerName, int wins, int losses) {
        this.playerName = playerName;
        this.wins = wins;
        this.losses = losses;
        this.gamesPlayed = wins + losses;
        this.winRate = gamesPlayed > 0 ? (double) wins / gamesPlayed * 100 : 0.0;
    }
    
    // Getters
    public String getPlayerName() { return playerName; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getGamesPlayed() { return gamesPlayed; }
    public double getWinRate() { return winRate; }
    
    /**
     * Crea estadísticas para un jugador nuevo (sin partidas)
     */
    public static PlayerStats newPlayer(String playerName) {
        return new PlayerStats(playerName, 0, 0);
    }
    
    /**
     * Crea nuevas estadísticas añadiendo una victoria
     */
    public PlayerStats addWin() {
        return new PlayerStats(playerName, wins + 1, losses);
    }
    
    /**
     * Crea nuevas estadísticas añadiendo una derrota
     */
    public PlayerStats addLoss() {
        return new PlayerStats(playerName, wins, losses + 1);
    }
    
    /**
     * Formato para mostrar en interfaz
     */
    public String getDisplayText() {
        return String.format("%s: %d-%d (%.1f%%)", 
            playerName, wins, losses, winRate);
    }
    
    /**
     * Formato compacto para serialización
     */
    public String serialize() {
        return wins + ":" + losses;
    }
    
    /**
     * Crea PlayerStats desde formato serializado
     */
    public static PlayerStats deserialize(String playerName, String serialized) {
        try {
            String[] parts = serialized.split(":");
            int wins = Integer.parseInt(parts[0]);
            int losses = Integer.parseInt(parts[1]);
            return new PlayerStats(playerName, wins, losses);
        } catch (Exception e) {
            return newPlayer(playerName);
        }
    }
    
    @Override
    public String toString() {
        return "PlayerStats{" +
                "name='" + playerName + '\'' +
                ", wins=" + wins +
                ", losses=" + losses +
                ", games=" + gamesPlayed +
                ", winRate=" + String.format("%.1f%%", winRate) +
                '}';
    }
}