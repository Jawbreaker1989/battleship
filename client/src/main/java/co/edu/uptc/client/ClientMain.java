package co.edu.uptc.client;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Punto de entrada del cliente RMI
 * Conecta al servidor y lanza la GUI simple
 */
public class ClientMain {
    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1100;  // Corregido para coincidir con el servidor
    
    public static void main(String[] args) {
        // Obtener parámetros de conexión
        // Prioridad de origen: args > system properties > env vars > default
    String tmpHost = DEFAULT_HOST;
    String portStr = String.valueOf(DEFAULT_PORT);

    if (System.getenv("BATTLESHIP_HOST") != null) tmpHost = System.getenv("BATTLESHIP_HOST");
        if (System.getenv("BATTLESHIP_PORT") != null) portStr = System.getenv("BATTLESHIP_PORT");
    if (System.getProperty("battleship.host") != null) tmpHost = System.getProperty("battleship.host");
        if (System.getProperty("battleship.port") != null) portStr = System.getProperty("battleship.port");
    if (args.length > 0 && !args[0].isBlank()) tmpHost = args[0];
        if (args.length > 1 && !args[1].isBlank()) portStr = args[1];

    int localPort;
        try {
            localPort = Integer.parseInt(portStr.trim());
        } catch (NumberFormatException nfe) {
            localPort = DEFAULT_PORT;
            LOGGER.warning("Puerto inválido '" + portStr + "', usando " + DEFAULT_PORT);
        }
        
    final String host = tmpHost; // capturar como final para la lambda
    final int port = localPort;
    LOGGER.info("Conectando al servidor RMI en " + host + ":" + port);
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Configurar timeouts RMI
                System.setProperty("sun.rmi.transport.tcp.responseTimeout", "10000");
                System.setProperty("sun.rmi.transport.tcp.readTimeout", "10000");
                
                // IMPORTANTE: Solo configurar java.rmi.server.hostname si es conexión remota (no localhost)
                // Esto permite que el servidor sepa cómo conectar de vuelta al cliente para callbacks
                if (!host.equals("localhost") && !host.equals("127.0.0.1")) {
                    try {
                        String localIp = java.net.InetAddress.getLocalHost().getHostAddress();
                        System.setProperty("java.rmi.server.hostname", localIp);
                        LOGGER.info("Configurado hostname RMI del cliente para callbacks remoto: " + localIp);
                    } catch (Exception e) {
                        LOGGER.warning("No se pudo detectar IP local, usando configuración por defecto: " + e.getMessage());
                    }
                }
                
                // Buscar registro RMI con reintentos
                Registry registry = connectWithRetry(host, port, 5);
                
                // Crear y mostrar ventana del juego
                GameWindow gameWindow = new GameWindow(registry);
                gameWindow.setVisible(true);
                
                LOGGER.info("Cliente iniciado correctamente");
                
            } catch (Exception e) {
                LOGGER.severe("Error iniciando cliente: " + e.getMessage());
                
                // Mostrar error al usuario
                JOptionPane.showMessageDialog(null, 
                    "Error conectando al servidor:\n" + e.getMessage() + 
                    "\n\nVerifica que el servidor esté ejecutándose en " + host + ":" + port,
                    "Error de Conexión", 
                    JOptionPane.ERROR_MESSAGE);
                
                System.exit(1);
            }
        });
    }
    
    /**
     * Conecta al registro RMI con reintentos automáticos
     */
    private static Registry connectWithRetry(String host, int port, int maxRetries) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                LOGGER.info("Intento de conexión " + attempt + "/" + maxRetries + " al servidor " + host + ":" + port);
                Registry registry = LocateRegistry.getRegistry(host, port);
                
                // Verificar que el registro esté disponible
                registry.list();
                
                LOGGER.info("Conexión exitosa al servidor RMI");
                return registry;
                
            } catch (Exception e) {
                lastException = e;
                LOGGER.warning("Intento " + attempt + " falló: " + e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        // Esperar antes del siguiente intento
                        Thread.sleep(2000 * attempt); // Incrementa el tiempo de espera
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("Conexión interrumpida", ie);
                    }
                }
            }
        }
        
        throw new Exception("No se pudo conectar al servidor después de " + maxRetries + " intentos", lastException);
    }
}
