package co.edu.uptc.client;

import co.edu.uptc.shared.model.Position;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Ventana principal SÚPER SIMPLE del juego Batalla Naval
 * Dos tableros lado a lado: TU FLOTA vs ENEMIGO
 */
public class GameWindow extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(GameWindow.class.getName());
    
    private final GameController controller;
    private BoardPanel myBoard;      // Mi tablero (solo para ver mis barcos)
    private BoardPanel enemyBoard;   // Tablero enemigo (para atacar)
    
    // Componentes GUI simplificados
    private JLabel statusLabel;
    private JLabel turnLabel;
    private JTextArea messageArea;
    
    // Botones de control del juego
    private JButton surrenderButton;
    private JButton newGameButton;
    private JButton statsButton;
    private JButton readyButton;
    
    // Estado GUI
    private String playerName;
    
    public GameWindow(Registry registry) throws Exception {
        this.controller = new GameController(registry);
        controller.initialize();
        controller.setGameWindow(this);
        
        // Auto-conectar con nombre automático
        autoConnect();
        
        initializeGUI();
    }
    
    private void autoConnect() {
        SwingUtilities.invokeLater(() -> {
            // Solicitar nombre del jugador
            String inputName = requestPlayerName();
            if (inputName == null || inputName.trim().isEmpty()) {
                // Si el usuario cancela o no ingresa nombre, cerrar la aplicación
                System.exit(0);
                return;
            }
            
            playerName = inputName.trim();
            
            // Conectar con el nombre ingresado
            try {
                controller.connectPlayer(playerName);
                // Actualizar la interfaz con el nombre real del jugador
                updatePlayerInfo(playerName);
            } catch (Exception ex) {
                LOGGER.severe("Error conectando con nombre '" + playerName + "': " + ex.getMessage());
                showError("Error conectando al juego: " + ex.getMessage());
                // Permitir intentar de nuevo
                autoConnect();
            }
        });
    }
    
    /**
     * Muestra un diálogo para solicitar el nombre del jugador
     * @return El nombre ingresado por el jugador, o null si cancela
     */
    private String requestPlayerName() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Título
        JLabel titleLabel = new JLabel("🚢 Bienvenido a Batalla Naval");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        panel.add(titleLabel, gbc);
        
        // Etiqueta
        JLabel nameLabel = new JLabel("Ingresa tu nombre:");
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(nameLabel, gbc);
        
        // Campo de texto
        JTextField nameField = new JTextField(15);
        nameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);
        
        // Instrucciones
        JLabel instructionLabel = new JLabel("<html><i>Mínimo 2 caracteres, máximo 20</i></html>");
        instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 10, 10, 10);
        panel.add(instructionLabel, gbc);
        
        // Configurar foco inicial en el campo de texto
        nameField.requestFocusInWindow();
        
        // Mostrar diálogo
        int result = JOptionPane.showConfirmDialog(
            null,
            panel,
            "Identificación del Jugador",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            if (name != null) {
                name = name.trim();
                // Validar longitud del nombre
                if (name.length() < 2) {
                    JOptionPane.showMessageDialog(null, 
                        "El nombre debe tener al menos 2 caracteres.", 
                        "Nombre inválido", 
                        JOptionPane.WARNING_MESSAGE);
                    return requestPlayerName(); // Intentar de nuevo
                }
                if (name.length() > 20) {
                    JOptionPane.showMessageDialog(null, 
                        "El nombre no puede tener más de 20 caracteres.", 
                        "Nombre inválido", 
                        JOptionPane.WARNING_MESSAGE);
                    return requestPlayerName(); // Intentar de nuevo
                }
                // Validar caracteres válidos (letras, números, espacios, algunos símbolos)
                if (!name.matches("[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s._-]+")) {
                    JOptionPane.showMessageDialog(null, 
                        "El nombre solo puede contener letras, números, espacios y los símbolos: . _ -", 
                        "Nombre inválido", 
                        JOptionPane.WARNING_MESSAGE);
                    return requestPlayerName(); // Intentar de nuevo
                }
            }
            return name;
        }
        
        return null; // Usuario canceló
    }
    
    private void initializeGUI() {
        setTitle("🚢 BATALLA NAVAL - Conectando...");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        
        // Panel superior - Estado del juego
        JPanel topPanel = createStatusPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Panel central - TABLEROS LADO A LADO
        JPanel centerPanel = createGameBoards();
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel derecho - Controles de barcos + Mensajes
        JPanel rightPanel = createControlPanel();
        add(rightPanel, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        
        LOGGER.info("GUI inicializada");
    }
    
    private JPanel createGameBoards() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 30, 0)); // Aumentado de 20 a 30 para más espacio entre tableros
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Aumentado el borde de 10 a 15
        
        // MI TABLERO (izquierda)
        JPanel myBoardPanel = new JPanel(new BorderLayout());
        myBoardPanel.setBorder(BorderFactory.createTitledBorder("🏠 TU FLOTA"));
        myBoard = new BoardPanel(this, true);  // true = mi tablero
        myBoardPanel.add(myBoard, BorderLayout.CENTER);
        
        // TABLERO ENEMIGO (derecha)
        JPanel enemyBoardPanel = new JPanel(new BorderLayout());
        enemyBoardPanel.setBorder(BorderFactory.createTitledBorder("🎯 ATACAR ENEMIGO"));
        enemyBoard = new BoardPanel(this, false); // false = tablero enemigo
        enemyBoardPanel.add(enemyBoard, BorderLayout.CENTER);
        
        panel.add(myBoardPanel);
        panel.add(enemyBoardPanel);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);
        
        // Panel izquierdo - Información del jugador
        JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerPanel.setBackground(Color.DARK_GRAY);
        
        JLabel playerLabel = new JLabel("👤 Jugador: ");
        playerLabel.setForeground(Color.LIGHT_GRAY);
        playerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        playerPanel.add(playerLabel);
        
        playerNameLabel = new JLabel(playerName != null ? playerName : "Conectando...");
        playerNameLabel.setForeground(Color.WHITE);
        playerNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        playerPanel.add(playerNameLabel);
        
        // Panel central - Estado del juego
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setBackground(Color.DARK_GRAY);
        
        statusLabel = new JLabel("Conectando...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        statusPanel.add(statusLabel);
        
        // Panel derecho - Información del turno
        JPanel turnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        turnPanel.setBackground(Color.DARK_GRAY);
        
        turnLabel = new JLabel("");
        turnLabel.setForeground(Color.YELLOW);
        turnLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        turnPanel.add(turnLabel);
        
        panel.add(playerPanel, BorderLayout.WEST);
        panel.add(statusPanel, BorderLayout.CENTER);
        panel.add(turnPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(350, 500)); // Aumentado de 280x400 a 350x500
        
        // Panel superior - Controles de barcos
        JPanel shipControlPanel = createShipControls();
        panel.add(shipControlPanel, BorderLayout.NORTH);
        
        // Panel inferior - Mensajes
        JPanel messagePanel = createMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createShipControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("🚢 COLOCAR BARCOS"));
        panel.setBackground(new Color(240, 248, 255));
        
        // Título
        JLabel titleLabel = new JLabel("🎯 Haz clic en TU TABLERO para colocar barcos");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        
        // Botón para cambiar orientación
        JButton orientationButton = new JButton("🔄 Horizontal");
        orientationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        orientationButton.addActionListener(e -> {
            myBoard.toggleOrientation();
            orientationButton.setText(myBoard.isHorizontal() ? "🔄 Horizontal" : "🔄 Vertical");
        });
        panel.add(orientationButton);
        panel.add(Box.createVerticalStrut(10));
        
        // Lista de barcos por colocar
        JLabel shipsLabel = new JLabel("Barcos restantes:");
        shipsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(shipsLabel);
        
        // Barcos por colocar
        JLabel ship1 = new JLabel("🚢 Portaaviones (5 casillas)");
        JLabel ship2 = new JLabel("🚢 Acorazado (4 casillas)");
        JLabel ship3 = new JLabel("🚢 Crucero (3 casillas)");
        JLabel ship4 = new JLabel("🚢 Submarino (3 casillas)");
        JLabel ship5 = new JLabel("🚢 Destructor (2 casillas)");
        
        ship1.setAlignmentX(Component.CENTER_ALIGNMENT);
        ship2.setAlignmentX(Component.CENTER_ALIGNMENT);
        ship3.setAlignmentX(Component.CENTER_ALIGNMENT);
        ship4.setAlignmentX(Component.CENTER_ALIGNMENT);
        ship5.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(ship1);
        panel.add(ship2);
        panel.add(ship3);
        panel.add(ship4);
        panel.add(ship5);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Botón para listo
        readyButton = new JButton("✅ ¡LISTO PARA JUGAR!");
        readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        readyButton.setBackground(Color.GREEN);
        readyButton.setForeground(Color.WHITE);
        readyButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        readyButton.addActionListener(e -> {
            // Verificar que todos los barcos estén colocados
            if (myBoard.allShipsPlaced()) {
                showMessage("✅ Todos los barcos colocados. Esperando oponente...");
                readyButton.setEnabled(false);
                controller.markReady();
            } else {
                showMessage("❌ Debes colocar todos los barcos primero");
            }
        });
        panel.add(readyButton);
        
        // Separador
        panel.add(Box.createVerticalStrut(20));
        
        // Panel de controles del juego
        JPanel gameControlsPanel = createGameControls();
        panel.add(gameControlsPanel);
        
        return panel;
    }
    
    /**
     * Crea el panel con controles del juego (rendirse, nueva partida, estadísticas)
     */
    private JPanel createGameControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("🎮 CONTROLES"));
        panel.setBackground(new Color(255, 248, 240));
        
        // Botón de rendirse
        this.surrenderButton = new JButton("🏳️ RENDIRSE");
        surrenderButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        surrenderButton.setBackground(new Color(255, 69, 0)); // Rojo naranja
        surrenderButton.setForeground(Color.WHITE);
        surrenderButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        surrenderButton.setEnabled(false); // Deshabilitado inicialmente
        surrenderButton.addActionListener(e -> handleSurrender());
        panel.add(surrenderButton);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Botón de nueva partida
        this.newGameButton = new JButton("🔄 NUEVA PARTIDA");
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.setBackground(new Color(70, 130, 180)); // Azul acero
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        newGameButton.setEnabled(false); // Deshabilitado inicialmente
        newGameButton.addActionListener(e -> handleNewGame());
        panel.add(newGameButton);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Botón de estadísticas
        this.statsButton = new JButton("📊 ESTADÍSTICAS");
        statsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsButton.setBackground(new Color(138, 43, 226)); // Violeta azul
        statsButton.setForeground(Color.WHITE);
        statsButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        statsButton.addActionListener(e -> showPlayerStats());
        panel.add(statsButton);
        
        return panel;
    }
    
    private JPanel createMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📢 Mensajes"));
        panel.setPreferredSize(new Dimension(320, 450)); // Aumentado de 250x400 a 320x450
        
        messageArea = new JTextArea(25, 20); // Aumentado de 20x15 a 25x20 para más líneas y columnas
        messageArea.setEditable(false);
        messageArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // Aumentado de 11 a 12 para mejor legibilidad
        messageArea.setBackground(Color.BLACK);
        messageArea.setForeground(Color.GREEN);
        messageArea.setLineWrap(true); // Añadido para envolver líneas largas
        messageArea.setWrapStyleWord(true); // Envolver por palabras completas
        
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Añadido scroll horizontal cuando sea necesario
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // === Métodos para interacción con el controlador ===
    
    public void onEnemyCellClicked(int x, int y) {
        Position pos = new Position(x, y);
        if (controller.isConnected()) {
            controller.attack(pos);
        }
    }
    
    // === Métodos para actualizar GUI desde controlador ===
    
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }
    
    public void showError(String error) {
        SwingUtilities.invokeLater(() -> {
            showMessage("ERROR: " + error);
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
        });
    }
    
    // Variable para mantener referencia al label del nombre del jugador
    private JLabel playerNameLabel;
    
    /**
     * Actualiza la información del jugador en la interfaz
     * @param name Nombre del jugador
     */
    public void updatePlayerInfo(String name) {
        SwingUtilities.invokeLater(() -> {
            this.playerName = name;
            setTitle("🚢 BATALLA NAVAL - " + name);
            
            // Actualizar el label del nombre del jugador si existe
            if (playerNameLabel != null) {
                playerNameLabel.setText(name);
            }
        });
    }
    
    public void setTurnIndicator(boolean isMyTurn) {
        SwingUtilities.invokeLater(() -> {
            if (isMyTurn) {
                turnLabel.setText("🎯 ES TU TURNO - ¡ATACA!");
                turnLabel.setForeground(Color.GREEN);
                enemyBoard.setAttackMode(true);
            } else {
                turnLabel.setText("⏳ Turno del oponente...");
                turnLabel.setForeground(Color.RED);
                enemyBoard.setAttackMode(false);
            }
        });
    }
    
    public void markEnemyAttack(Position target, String result) {
        SwingUtilities.invokeLater(() -> {
            enemyBoard.markAttack(target, result);
        });
    }
    
    // === Getters ===
    
    public String getPlayerName() {
        return playerName;
    }
    
    public BoardPanel getMyBoard() {
        return myBoard;
    }
    
    public BoardPanel getEnemyBoard() {
        return enemyBoard;
    }

    // Nuevo: exponer el controlador para que BoardPanel pueda llamar a placeShip remoto
    public GameController getController() {
        return controller;
    }
    
    // === Métodos de Control de Juego ===
    
    /**
     * Maneja la acción de rendirse con confirmación
     */
    private void handleSurrender() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que quieres rendirte?\n\n" +
            "Esto contará como una derrota en tus estadísticas.",
            "Confirmación de Rendición",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                boolean success = controller.surrenderGame();
                if (success) {
                    showMessage("🏳️ Te has rendido. Fin de la partida.");
                    surrenderButton.setEnabled(false);
                } else {
                    showError("No se pudo procesar la rendición.");
                }
            } catch (Exception ex) {
                showError("Error al rendirse: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Maneja la solicitud de nueva partida con confirmación
     */
    private void handleNewGame() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "¿Quieres jugar una nueva partida con el mismo oponente?\n\n" +
            "Esto reiniciará los tableros y comenzará un juego nuevo.",
            "Nueva Partida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                boolean success = controller.requestNewGame();
                if (success) {
                    showMessage("🔄 Solicitando nueva partida...");
                    // Reset UI elements
                    resetGameUI();
                } else {
                    showError("No se pudo solicitar nueva partida.");
                }
            } catch (Exception ex) {
                showError("Error solicitando nueva partida: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Muestra las estadísticas del jugador
     */
    private void showPlayerStats() {
        try {
            String stats = controller.getPlayerStats();
            showStatsDialog(stats);
        } catch (Exception ex) {
            showError("Error obteniendo estadísticas: " + ex.getMessage());
        }
    }
    
    /**
     * Muestra un diálogo elegante con las estadísticas del jugador
     */
    private void showStatsDialog(String statsData) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Parsear estadísticas (formato: "wins:losses")
                String[] parts = statsData.split(":");
                int wins = Integer.parseInt(parts[0]);
                int losses = Integer.parseInt(parts[1]);
                int total = wins + losses;
                double winRate = total > 0 ? (double) wins / total * 100 : 0.0;
                
                // Crear panel del diálogo
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                // Título
                JLabel titleLabel = new JLabel("📊 Estadísticas de " + playerName);
                titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
                titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
                panel.add(titleLabel, BorderLayout.NORTH);
                
                // Contenido de estadísticas
                JPanel statsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
                statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
                
                statsPanel.add(new JLabel("🏆 Victorias:"));
                JLabel winsLabel = new JLabel(String.valueOf(wins));
                winsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                winsLabel.setForeground(new Color(34, 139, 34));
                statsPanel.add(winsLabel);
                
                statsPanel.add(new JLabel("💀 Derrotas:"));
                JLabel lossesLabel = new JLabel(String.valueOf(losses));
                lossesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                lossesLabel.setForeground(new Color(220, 20, 60));
                statsPanel.add(lossesLabel);
                
                statsPanel.add(new JLabel("🎮 Partidas jugadas:"));
                JLabel totalLabel = new JLabel(String.valueOf(total));
                totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                statsPanel.add(totalLabel);
                
                statsPanel.add(new JLabel("📈 Porcentaje de victoria:"));
                JLabel winRateLabel = new JLabel(String.format("%.1f%%", winRate));
                winRateLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                winRateLabel.setForeground(winRate >= 50 ? new Color(34, 139, 34) : new Color(255, 140, 0));
                statsPanel.add(winRateLabel);
                
                // Mensaje motivacional
                String motivationalMsg = "";
                if (total == 0) {
                    motivationalMsg = "¡Comienza tu primera partida!";
                } else if (winRate >= 80) {
                    motivationalMsg = "¡Excelente estratega naval! 🌟";
                } else if (winRate >= 60) {
                    motivationalMsg = "¡Buen rendimiento! 💪";
                } else if (winRate >= 40) {
                    motivationalMsg = "Sigue mejorando 📚";
                } else {
                    motivationalMsg = "¡No te rindas! La práctica hace al maestro 🚀";
                }
                
                JLabel motivationLabel = new JLabel(motivationalMsg);
                motivationLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
                motivationLabel.setHorizontalAlignment(SwingConstants.CENTER);
                statsPanel.add(new JLabel()); // Espacio
                statsPanel.add(motivationLabel);
                
                panel.add(statsPanel, BorderLayout.CENTER);
                
                // Mostrar diálogo
                JOptionPane.showMessageDialog(
                    this,
                    panel,
                    "Estadísticas del Jugador",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
            } catch (Exception e) {
                LOGGER.warning("Error mostrando estadísticas: " + e.getMessage());
                showMessage("Estadísticas: " + statsData);
            }
        });
    }
    
    /**
     * Resetea elementos de la interfaz para una nueva partida
     */
    public void resetGameUI() {
        SwingUtilities.invokeLater(() -> {
            // Limpiar tableros
            myBoard.clearBoard();
            enemyBoard.clearBoard();
            
            // Resetear botones al estado inicial
            surrenderButton.setEnabled(false);
            newGameButton.setEnabled(false);
            readyButton.setEnabled(true); // ¡CRÍTICO! Reactivar para nueva partida
            
            // Limpiar indicadores
            turnLabel.setText("");
            updateStatus("Preparando nueva partida...");
            
            LOGGER.info("Interfaz reseteada para nueva partida - Botón Listo reactivado");
        });
    }
    
    /**
     * Actualiza el estado de los botones según la fase del juego
     */
    public void updateGameControls(boolean gameInProgress, boolean gameFinished) {
        updateGameControls(gameInProgress, gameFinished, false);
    }
    
    /**
     * Actualiza el estado de los botones según la fase del juego y el turno
     */
    public void updateGameControls(boolean gameInProgress, boolean gameFinished, boolean isMyTurn) {
        SwingUtilities.invokeLater(() -> {
            // El botón rendirse solo se activa si el juego está en progreso Y es mi turno
            surrenderButton.setEnabled(gameInProgress && isMyTurn);
            newGameButton.setEnabled(gameFinished);
        });
    }
    
    /**
     * Muestra la pantalla de victoria o derrota
     */
    public void showGameResult(boolean won, String winnerName) {
        SwingUtilities.invokeLater(() -> {
            String title = won ? "🏆 ¡VICTORIA!" : "💀 DERROTA";
            String message = won ? 
                "🎉 ¡Felicidades!\n\n¡Has ganado la partida!" :
                "😞 Has perdido\n\nEl ganador es: " + winnerName;
                
            Color bgColor = won ? new Color(34, 139, 34) : new Color(220, 20, 60);
            
            // Crear panel personalizado
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(bgColor);
            panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JLabel messageLabel = new JLabel("<html><center>" + message.replace("\n", "<br>") + "</center></html>");
            messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(Box.createVerticalStrut(20), BorderLayout.CENTER);
            panel.add(messageLabel, BorderLayout.SOUTH);
            
            JOptionPane.showMessageDialog(
                this,
                panel,
                title,
                JOptionPane.PLAIN_MESSAGE
            );
            
            // Habilitar botón de nueva partida
            updateGameControls(false, true);
        });
    }
    
    /**
     * Muestra un diálogo de solicitud de nueva partida desde el oponente
     */
    public void showNewGameRequest(String requesterName) {
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "🔄 Tu oponente " + requesterName + " quiere jugar otra partida.\n\n" +
                "¿Aceptas la revancha?\n\n" +
                "Los tableros se reiniciarán y podrán colocar sus barcos nuevamente.",
                "Solicitud de Revancha",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            boolean accepts = (result == JOptionPane.YES_OPTION);
            
            try {
                boolean success = controller.respondToNewGameRequest(accepts);
                if (success) {
                    if (accepts) {
                        showMessage("✅ Revancha aceptada. Preparando nueva partida...");
                        resetGameUI();
                    } else {
                        showMessage("❌ Revancha rechazada.");
                    }
                } else {
                    showError("Error procesando respuesta a revancha.");
                }
            } catch (Exception ex) {
                showError("Error respondiendo a revancha: " + ex.getMessage());
            }
        });
    }
}