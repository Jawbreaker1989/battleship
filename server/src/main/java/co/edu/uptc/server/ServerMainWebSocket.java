package co.edu.uptc.server;

import org.glassfish.tyrus.server.Server;

import java.util.logging.Logger;

/**
 * WebSocket Server Main Entry Point
 * Starts the WebSocket server on port 8080
 */
public class ServerMainWebSocket {
    private static final Logger LOGGER = Logger.getLogger(ServerMainWebSocket.class.getName());
    private static final int WEBSOCKET_PORT = 8080;
    private static final String WEBSOCKET_PATH = "/";

    public static void main(String[] args) {
        String host = "0.0.0.0"; // Listen on all interfaces

        if (args.length > 0 && !args[0].isBlank()) {
            host = args[0].trim();
        }

        System.out.println("🚀 Iniciando Servidor WebSocket de Batalla Naval...\n");

        // Create Tyrus server
        Server server = new Server(host, WEBSOCKET_PORT, WEBSOCKET_PATH, null,
                GameWebSocketServer.class);

        try {
            server.start();

            System.out.println("╔═══════════════════════════════════════════════════════╗");
            System.out.println("║      SERVIDOR BATALLA NAVAL WEBSOCKET                 ║");
            System.out.println("╠═══════════════════════════════════════════════════════╣");
            System.out.printf("║ 🌐 Host: %-46s║%n", host);
            System.out.printf("║ 🔌 Puerto: %-44d║%n", WEBSOCKET_PORT);
            System.out.println("║ 📡 Endpoint: ws://host:8080/battleship                ║");
            System.out.println("║ 🎮 Capacidad: 2 jugadores simultáneos                ║");
            System.out.println("║ 📊 Estado: Esperando conexiones...                    ║");
            System.out.println("╚═══════════════════════════════════════════════════════╝");

            System.out.println("\n⚠️  Para detener el servidor presiona Ctrl+C");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Deteniendo servidor WebSocket...");
                server.stop();
                LOGGER.info("Servidor WebSocket detenido");
            }));

            LOGGER.info("Servidor WebSocket iniciado en " + host + ":" + WEBSOCKET_PORT);

            // Keep main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("❌ Error crítico iniciando servidor: " + e.getMessage());
            LOGGER.severe("Error crítico: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
