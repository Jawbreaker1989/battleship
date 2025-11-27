package co.edu.uptc.client;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * WebSocket Client Main Entry Point
 * Connects to WebSocket server and launches GUI
 */
public class ClientMain {
    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        // Get connection parameters
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        // Check environment variables
        if (System.getenv("BATTLESHIP_HOST") != null) {
            host = System.getenv("BATTLESHIP_HOST");
        }
        if (System.getenv("BATTLESHIP_PORT") != null) {
            try {
                port = Integer.parseInt(System.getenv("BATTLESHIP_PORT"));
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid BATTLESHIP_PORT, using default");
            }
        }

        // Check system properties
        if (System.getProperty("battleship.host") != null) {
            host = System.getProperty("battleship.host");
        }
        if (System.getProperty("battleship.port") != null) {
            try {
                port = Integer.parseInt(System.getProperty("battleship.port"));
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid battleship.port, using default");
            }
        }

        // Check command line arguments
        if (args.length > 0 && !args[0].isBlank()) {
            host = args[0];
        }
        if (args.length > 1 && !args[1].isBlank()) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid port argument, using default");
            }
        }

        final String serverHost = host;
        final int serverPort = port;
        final String wsUrl = "ws://" + serverHost + ":" + serverPort + "/battleship";

        LOGGER.info("Connecting to WebSocket server: " + wsUrl);

        SwingUtilities.invokeLater(() -> {
            try {
                // Create and show game window with WebSocket
                GameWindow gameWindow = new GameWindow(wsUrl);
                gameWindow.setVisible(true);

                LOGGER.info("Client started successfully");

            } catch (Exception e) {
                LOGGER.severe("Error starting client: " + e.getMessage());
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "Error conectando al servidor:\\n" + e.getMessage() +
                                "\\n\\nVerifica que el servidor esté ejecutándose en " + wsUrl,
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);

                System.exit(1);
            }
        });
    }
}
