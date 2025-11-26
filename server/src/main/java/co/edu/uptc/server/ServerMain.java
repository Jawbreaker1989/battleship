package co.edu.uptc.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * Punto de entrada del servidor distribuido
 * Demuestra configuración de RMI Registry y publicación de servicios
 */
public class ServerMain {
    private static final Logger LOGGER = Logger.getLogger(ServerMain.class.getName());
    private static final int REGISTRY_PORT = 1099;
    private static final int SERVICE_PORT = 1100;
    private static final String SERVICE_NAME = "GameService";

    public static void main(String[] args) {
        try {
            System.out.println("🚀 Iniciando Servidor de Batalla Naval Distribuido (LAN/WAN) ...\n");

            // 1. Determinar host e IP
            // Prioridad: args[0] > env BATTLESHIP_PUBLIC_IP > AZURE_PUBLIC_IP > 0.0.0.0
            // (escucha en todas las interfaces)
            // CONFIGURACIÓN PARA AZURE VM: Por defecto escucha en todas las interfaces y
            // usa IP pública de Azure
            final String AZURE_PUBLIC_IP = "68.211.112.149"; // IP pública de Azure VM

            String argHost = (args.length > 0 && !args[0].isBlank()) ? args[0].trim() : null;
            String envHost = System.getenv("BATTLESHIP_PUBLIC_IP");
            if (envHost != null && envHost.isBlank()) {
                envHost = null;
            }

            // Para Azure: usar la IP pública de Azure por defecto
            String host = (argHost != null) ? argHost : (envHost != null ? envHost : AZURE_PUBLIC_IP);

            // 2. Propiedades RMI
            // Preferir IPv4 (evita problemas con IPv6 en entornos cloud)
            System.setProperty("java.net.preferIPv4Stack", "true");

            // Configurar hostname que expondrá el stub RMI (IMPORTANTE para clientes
            // remotos)
            System.setProperty("java.rmi.server.hostname", host);
            System.setProperty("java.security.policy", "all.policy");
            System.setProperty("sun.rmi.transport.tcp.responseTimeout", "10000");
            System.setProperty("sun.rmi.transport.tcp.readTimeout", "10000");

            // 3. Crear servicio en puerto FIJO (1100)
            // Esto es CRÍTICO para Azure/Firewalls: el objeto exportado debe tener puerto
            // fijo
            GameServiceImpl gameService = new GameServiceImpl(SERVICE_PORT);
            System.out.println("✅ Servicio de juego creado en puerto fijo: " + SERVICE_PORT);

            // 4. Crear / obtener registry en puerto estándar (1099)
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(REGISTRY_PORT);
                System.out.println("✅ Registry creado en puerto " + REGISTRY_PORT);
            } catch (ExportException ee) {
                registry = LocateRegistry.getRegistry(REGISTRY_PORT);
                System.out.println("ℹ️  Registry existente reutilizado en puerto " + REGISTRY_PORT);
            }

            // 5. Publicar (rebind para idempotencia)
            registry.rebind(SERVICE_NAME, gameService);
            System.out.println("✅ Servicio publicado como '" + SERVICE_NAME + "'");

            // 6. Información
            System.out.println("\n╔═══════════════════════════════════════════════════════╗");
            System.out.println("║      SERVIDOR BATALLA NAVAL DISTRIBUIDO (OPTIMIZADO)  ║");
            System.out.println("╠═══════════════════════════════════════════════════════╣");
            System.out.printf("║ 🌐 Host/IP: %-43s║%n", host);
            System.out.printf("║ 🔌 Puerto Registry: %-35d║%n", REGISTRY_PORT);
            System.out.printf("║ 🔌 Puerto Servicio: %-35d║%n", SERVICE_PORT);
            System.out.printf("║ 📡 Servicio: %-41s║%n", SERVICE_NAME);
            System.out.println("║ 🎮 Capacidad: 2 jugadores simultáneos               ║");
            System.out.println("║ 📊 Estado: Esperando conexiones de clientes...       ║");
            System.out.println("╚═══════════════════════════════════════════════════════╝");

            System.out.println("\n🔗 Conexión cliente:");
            System.out.println("   java -cp shared/target/classes;client/target/classes co.edu.uptc.client.ClientMain "
                    + host + " " + REGISTRY_PORT);
            System.out.println("   (Nota: Asegúrate de abrir puertos " + REGISTRY_PORT + " y " + SERVICE_PORT
                    + " en Azure/Firewall)\n");

            System.out.println("⚠️  Para detener el servidor presiona Ctrl+C");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Deteniendo servidor distribuido...");
                LOGGER.info("Servidor RMI detenido correctamente");
            }));

            LOGGER.info("Servidor RMI iniciado correctamente en host " + host);

            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("❌ Error crítico iniciando servidor RMI: " + e.getMessage());
            LOGGER.severe("Error crítico en el servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String detectLanIp() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface ni = nets.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual())
                    continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    String ip = a.getHostAddress();
                    if (ip.contains(":"))
                        continue; // skip IPv6
                    if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                        return ip;
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }
}
