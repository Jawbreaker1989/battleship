package co.edu.uptc.shared.messages;

import co.edu.uptc.shared.model.Position;

/**
 * Message sent from client to server (actions)
 */
public class ClientMessage {
    private String action; // join, placeShip, attack, ready, surrender, requestNewGame, respondNewGame,
                           // ping
    private String playerName;
    private String playerId;
    private Position start;
    private Position end;
    private Position target;
    private Boolean accepts;

    public ClientMessage() {
    }

    // Factory methods for different action types
    public static ClientMessage join(String playerName) {
        ClientMessage msg = new ClientMessage();
        msg.action = "join";
        msg.playerName = playerName;
        return msg;
    }

    public static ClientMessage placeShip(String playerId, Position start, Position end) {
        ClientMessage msg = new ClientMessage();
        msg.action = "placeShip";
        msg.playerId = playerId;
        msg.start = start;
        msg.end = end;
        return msg;
    }

    public static ClientMessage attack(String playerId, Position target) {
        ClientMessage msg = new ClientMessage();
        msg.action = "attack";
        msg.playerId = playerId;
        msg.target = target;
        return msg;
    }

    public static ClientMessage ready(String playerId) {
        ClientMessage msg = new ClientMessage();
        msg.action = "ready";
        msg.playerId = playerId;
        return msg;
    }

    public static ClientMessage surrender(String playerId) {
        ClientMessage msg = new ClientMessage();
        msg.action = "surrender";
        msg.playerId = playerId;
        return msg;
    }

    public static ClientMessage requestNewGame(String playerId) {
        ClientMessage msg = new ClientMessage();
        msg.action = "requestNewGame";
        msg.playerId = playerId;
        return msg;
    }

    public static ClientMessage respondNewGame(String playerId, boolean accepts) {
        ClientMessage msg = new ClientMessage();
        msg.action = "respondNewGame";
        msg.playerId = playerId;
        msg.accepts = accepts;
        return msg;
    }

    public static ClientMessage ping(String playerId) {
        ClientMessage msg = new ClientMessage();
        msg.action = "ping";
        msg.playerId = playerId;
        return msg;
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Position getStart() {
        return start;
    }

    public void setStart(Position start) {
        this.start = start;
    }

    public Position getEnd() {
        return end;
    }

    public void setEnd(Position end) {
        this.end = end;
    }

    public Position getTarget() {
        return target;
    }

    public void setTarget(Position target) {
        this.target = target;
    }

    public Boolean getAccepts() {
        return accepts;
    }

    public void setAccepts(Boolean accepts) {
        this.accepts = accepts;
    }
}
