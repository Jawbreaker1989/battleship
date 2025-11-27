package co.edu.uptc.shared.messages;

/**
 * Message sent from server to client (events)
 */
public class ServerMessage {
    private String event; // joined, playerJoined, turnChanged, attackResult, gameEnded, etc.
    private String playerId;
    private String playerName;
    private String message;
    private Boolean isMyTurn;
    private String currentPlayerName;
    private String winner;
    private String attackerName;
    private Integer targetX;
    private Integer targetY;
    private String result;
    private Boolean yourBoard;
    private String requesterName;
    private String sessionId;
    private String status; // Game status message

    public ServerMessage() {
    }

    // Factory methods for different event types
    public static ServerMessage joined(String playerId, String sessionId) {
        ServerMessage msg = new ServerMessage();
        msg.event = "joined";
        msg.playerId = playerId;
        msg.sessionId = sessionId;
        return msg;
    }

    public static ServerMessage playerJoined(String playerName) {
        ServerMessage msg = new ServerMessage();
        msg.event = "playerJoined";
        msg.playerName = playerName;
        return msg;
    }

    public static ServerMessage turnChanged(boolean isMyTurn, String currentPlayerName) {
        ServerMessage msg = new ServerMessage();
        msg.event = "turnChanged";
        msg.isMyTurn = isMyTurn;
        msg.currentPlayerName = currentPlayerName;
        return msg;
    }

    public static ServerMessage attackResult(String result) {
        ServerMessage msg = new ServerMessage();
        msg.event = "attackResult";
        msg.result = result;
        return msg;
    }

    public static ServerMessage gameEnded(String winner) {
        ServerMessage msg = new ServerMessage();
        msg.event = "gameEnded";
        msg.winner = winner;
        return msg;
    }

    public static ServerMessage opponentDisconnected() {
        ServerMessage msg = new ServerMessage();
        msg.event = "opponentDisconnected";
        return msg;
    }

    public static ServerMessage attackEvent(String attackerName, int targetX, int targetY,
            String result, boolean yourBoard) {
        ServerMessage msg = new ServerMessage();
        msg.event = "attackEvent";
        msg.attackerName = attackerName;
        msg.targetX = targetX;
        msg.targetY = targetY;
        msg.result = result;
        msg.yourBoard = yourBoard;
        return msg;
    }

    public static ServerMessage newGameRequest(String requesterName) {
        ServerMessage msg = new ServerMessage();
        msg.event = "newGameRequest";
        msg.requesterName = requesterName;
        return msg;
    }

    public static ServerMessage gameEvent(String message) {
        ServerMessage msg = new ServerMessage();
        msg.event = "gameEvent";
        msg.message = message;
        return msg;
    }

    public static ServerMessage statusUpdate(String status) {
        ServerMessage msg = new ServerMessage();
        msg.event = "statusUpdate";
        msg.status = status;
        return msg;
    }

    // Getters and Setters
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsMyTurn() {
        return isMyTurn;
    }

    public void setIsMyTurn(Boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public void setCurrentPlayerName(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getAttackerName() {
        return attackerName;
    }

    public void setAttackerName(String attackerName) {
        this.attackerName = attackerName;
    }

    public Integer getTargetX() {
        return targetX;
    }

    public void setTargetX(Integer targetX) {
        this.targetX = targetX;
    }

    public Integer getTargetY() {
        return targetY;
    }

    public void setTargetY(Integer targetY) {
        this.targetY = targetY;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Boolean getYourBoard() {
        return yourBoard;
    }

    public void setYourBoard(Boolean yourBoard) {
        this.yourBoard = yourBoard;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
