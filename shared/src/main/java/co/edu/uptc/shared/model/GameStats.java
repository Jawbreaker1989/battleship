package co.edu.uptc.shared.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase para rastrear estadísticas detalladas de una partida
 * Registra todos los datos relevantes para mostrar al final del juego
 */
public class GameStats implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Estadísticas básicas
    private int shotsTotal;           // Total de disparos realizados
    private int shotsHit;             // Disparos que dieron en el blanco
    private int shotsMissed;          // Disparos que fallaron
    private int shipsDestroyed;       // Barcos completamente hundidos
    private int shipsCellsHit;        // Celdas de barcos impactadas
    
    // Estadísticas por tipo de barco
    private int destroyers5Hit;       // Destructores (5 casillas) hundidos
    private int cruisers4Hit;         // Cruceros (4 casillas) hundidos
    private int submarines3Hit;       // Submarinos (3 casillas) hundidos
    private int frigates2Hit;         // Fragatas (2 casillas) hundidos
    
    // Tiempos y metadata
    private LocalDateTime gameStartTime;
    private LocalDateTime gameEndTime;
    private long gameDurationMs;
    
    // Estado del juego
    private boolean winner;
    private String playerName;
    
    public GameStats(String playerName) {
        this.playerName = playerName;
        this.gameStartTime = LocalDateTime.now();
        resetStats();
    }
    
    /**
     * Resetea todas las estadísticas para una nueva partida
     */
    public void resetStats() {
        this.shotsTotal = 0;
        this.shotsHit = 0;
        this.shotsMissed = 0;
        this.shipsDestroyed = 0;
        this.shipsCellsHit = 0;
        this.destroyers5Hit = 0;
        this.cruisers4Hit = 0;
        this.submarines3Hit = 0;
        this.frigates2Hit = 0;
        this.gameDurationMs = 0;
        this.winner = false;
        this.gameStartTime = LocalDateTime.now();
        this.gameEndTime = null;
    }
    
    /**
     * Registra un disparo realizado
     */
    public void recordShot(boolean hit) {
        shotsTotal++;
        if (hit) {
            shotsHit++;
            shipsCellsHit++;
        } else {
            shotsMissed++;
        }
    }
    
    /**
     * Registra un barco hundido por tamaño
     */
    public void recordShipDestroyed(int shipSize) {
        shipsDestroyed++;
        switch (shipSize) {
            case 5:
                destroyers5Hit++;
                break;
            case 4:
                cruisers4Hit++;
                break;
            case 3:
                submarines3Hit++;
                break;
            case 2:
                frigates2Hit++;
                break;
        }
    }
    
    /**
     * Marca el fin del juego y calcula duración
     */
    public void endGame(boolean won) {
        this.winner = won;
        this.gameEndTime = LocalDateTime.now();
        this.gameDurationMs = java.time.Duration.between(gameStartTime, gameEndTime).toMillis();
    }
    
    /**
     * Calcula la precisión de disparo
     */
    public double getAccuracy() {
        if (shotsTotal == 0) return 0.0;
        return (double) shotsHit / shotsTotal * 100.0;
    }
    
    /**
     * Obtiene la duración del juego formateada
     */
    public String getFormattedDuration() {
        if (gameDurationMs == 0) return "En curso...";
        
        long seconds = gameDurationMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        return String.format("%d:%02d", minutes, seconds);
    }
    
    /**
     * Obtiene la fecha/hora de inicio formateada
     */
    public String getFormattedStartTime() {
        return gameStartTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    /**
     * Genera un resumen de estadísticas para mostrar
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTADÍSTICAS DE ").append(playerName.toUpperCase()).append(" ===\n");
        sb.append("🎯 Disparos totales: ").append(shotsTotal).append("\n");
        sb.append("🎯 Aciertos: ").append(shotsHit).append("\n");
        sb.append("🎯 Fallos: ").append(shotsMissed).append("\n");
        sb.append("🎯 Precisión: ").append(String.format("%.1f%%", getAccuracy())).append("\n");
        sb.append("🚢 Barcos hundidos: ").append(shipsDestroyed).append("/5\n");
        sb.append("⏱️ Duración: ").append(getFormattedDuration()).append("\n");
        sb.append("🏆 Resultado: ").append(winner ? "VICTORIA" : "DERROTA");
        return sb.toString();
    }
    
    // Getters y setters
    public int getShotsTotal() { return shotsTotal; }
    public int getShotsHit() { return shotsHit; }
    public int getShotsMissed() { return shotsMissed; }
    public int getShipsDestroyed() { return shipsDestroyed; }
    public int getShipsCellsHit() { return shipsCellsHit; }
    public int getDestroyers5Hit() { return destroyers5Hit; }
    public int getCruisers4Hit() { return cruisers4Hit; }
    public int getSubmarines3Hit() { return submarines3Hit; }
    public int getFrigates2Hit() { return frigates2Hit; }
    public boolean isWinner() { return winner; }
    public String getPlayerName() { return playerName; }
    public LocalDateTime getGameStartTime() { return gameStartTime; }
    public LocalDateTime getGameEndTime() { return gameEndTime; }
    public long getGameDurationMs() { return gameDurationMs; }
    
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}