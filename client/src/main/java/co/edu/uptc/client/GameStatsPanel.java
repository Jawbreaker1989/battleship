package co.edu.uptc.client;

import co.edu.uptc.shared.model.GameStats;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Panel elegante para mostrar estadísticas detalladas al final del juego
 * Muestra comparación entre jugadores con datos como disparos, aciertos, barcos hundidos, etc.
 */
public class GameStatsPanel extends JPanel {
    
    private GameStats playerStats;
    private GameStats opponentStats;
    private String playerName;
    private String opponentName;
    
    public GameStatsPanel(GameStats playerStats, GameStats opponentStats, 
                         String playerName, String opponentName) {
        this.playerStats = playerStats;
        this.opponentStats = opponentStats;
        this.playerName = playerName;
        this.opponentName = opponentName;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "📊 Estadísticas Detalladas de la Partida",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 16),
            new Color(41, 128, 185)
        ));
        
        // Crear tabla con datos comparativos
        JTable statsTable = createStatsTable();
        
        // Panel contenedor con scroll
        JScrollPane scrollPane = new JScrollPane(statsTable);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior con resumen
        add(createSummaryPanel(), BorderLayout.SOUTH);
    }
    
    private JTable createStatsTable() {
        // Columnas de la tabla
        String[] columnNames = {"Estadística", playerName, opponentName};
        
        // Datos de la tabla
        Object[][] data = {
            {"🎯 Disparos Realizados", playerStats.getShotsTotal(), opponentStats.getShotsTotal()},
            {"✅ Aciertos", playerStats.getShotsHit(), opponentStats.getShotsHit()},
            {"❌ Fallos", playerStats.getShotsMissed(), opponentStats.getShotsMissed()},
            {"🎯 Precisión", String.format("%.1f%%", playerStats.getAccuracy()), 
                           String.format("%.1f%%", opponentStats.getAccuracy())},
            {"⚓ Fragatas (2 celdas)", playerStats.getFrigates2Hit(), 
                                     opponentStats.getFrigates2Hit()},
            {"🚢 Submarinos (3 celdas)", playerStats.getSubmarines3Hit(), 
                                       opponentStats.getSubmarines3Hit()},
            {"🛥️ Cruceros (4 celdas)", playerStats.getCruisers4Hit(), 
                                      opponentStats.getCruisers4Hit()},
            {"🚀 Destructores (5 celdas)", playerStats.getDestroyers5Hit(), 
                                          opponentStats.getDestroyers5Hit()},
            {"💥 Total Barcos Hundidos", playerStats.getShipsDestroyed(), 
                                       opponentStats.getShipsDestroyed()},
            {"⏱️ Duración del Juego", formatDuration(playerStats.getGameDurationMs()), 
                                   formatDuration(opponentStats.getGameDurationMs())}
        };
        
        // Crear modelo de tabla
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };
        
        JTable table = new JTable(model);
        
        // Configurar apariencia de la tabla
        setupTableAppearance(table);
        
        return table;
    }
    
    private void setupTableAppearance(JTable table) {
        // Configurar header
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));
        
        // Configurar celdas
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setGridColor(new Color(189, 195, 199));
        table.setSelectionBackground(new Color(174, 214, 241));
        
        // Renderer personalizado para celdas
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
                
                // Colores alternados para filas
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(248, 249, 250));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                
                // Primera columna en negrita (nombres de estadísticas)
                if (column == 0) {
                    setFont(getFont().deriveFont(Font.BOLD));
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
                
                return c;
            }
        };
        
        // Aplicar renderer a todas las columnas
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
        
        // Ajustar ancho de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Estadística
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Jugador 1
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Jugador 2
    }
    
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        summaryPanel.setBackground(new Color(236, 240, 241));
        
        // Determinar ganador y mostrar resumen
        String winner = determineWinner();
        JLabel summaryLabel = new JLabel(winner, JLabel.CENTER);
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryLabel.setForeground(new Color(39, 174, 96));
        
        summaryPanel.add(summaryLabel, BorderLayout.CENTER);
        
        return summaryPanel;
    }
    
    private String determineWinner() {
        if (playerStats.isWinner()) {
            return String.format("🏆 ¡%s ganó la partida con %.1f%% de precisión!", 
                    playerName, playerStats.getAccuracy());
        } else if (opponentStats.isWinner()) {
            return String.format("🏆 ¡%s ganó la partida con %.1f%% de precisión!", 
                    opponentName, opponentStats.getAccuracy());
        } else {
            return "📊 Estadísticas de la partida";
        }
    }
    
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d:%02d min", minutes, seconds);
        } else {
            return String.format("%d seg", seconds);
        }
    }
    
    /**
     * Crea y muestra un diálogo con las estadísticas del juego
     */
    public static void showStatsDialog(Component parent, GameStats playerStats, GameStats opponentStats,
                                     String playerName, String opponentName) {
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Estadísticas de la Partida - Batalla Naval");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        GameStatsPanel statsPanel = new GameStatsPanel(playerStats, opponentStats, 
                                                      playerName, opponentName);
        
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Cerrar");
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        dialog.add(statsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}